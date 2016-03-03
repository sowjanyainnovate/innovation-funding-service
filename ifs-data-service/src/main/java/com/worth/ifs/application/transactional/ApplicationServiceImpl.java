package com.worth.ifs.application.transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worth.ifs.application.constant.ApplicationStatusConstants;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.domain.ApplicationStatus;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.domain.Section;
import com.worth.ifs.application.mapper.ApplicationMapper;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.FormInputResponseFileEntryId;
import com.worth.ifs.application.resource.FormInputResponseFileEntryResource;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.file.domain.FileEntry;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.file.resource.FileEntryResourceAssembler;
import com.worth.ifs.file.transactional.FileService;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.form.repository.FormInputRepository;
import com.worth.ifs.form.repository.FormInputResponseRepository;
import com.worth.ifs.transactional.BaseTransactionalService;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.UserRoleType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.FILES_FILE_ALREADY_LINKED_TO_FORM_INPUT_RESPONSE;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static com.worth.ifs.util.CollectionFunctions.simpleMap;
import static com.worth.ifs.util.EntityLookupCallbacks.find;
import static java.util.stream.Collectors.toList;

/**
 * Transactional and secured service focused around the processing of Applications
 */
@Service
public class ApplicationServiceImpl extends BaseTransactionalService implements ApplicationService {

    private static final Log LOG = LogFactory.getLog(ApplicationServiceImpl.class);

    // TODO DW - INFUND-1555 - put into a DTO
    public static final String READY_FOR_SUBMIT = "readyForSubmit";
    public static final String PROGRESS = "progress";
    public static final String RESEARCH_PARTICIPATION = "researchParticipation";
    public static final String RESEARCH_PARTICIPATION_VALID = "researchParticipationValid";
    public static final String ALL_SECTION_COMPLETE = "allSectionComplete";

    @Autowired
    private FileService fileService;

    @Autowired
    private FormInputResponseRepository formInputResponseRepository;

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ApplicationMapper applicationMapper;

    //@Autowired
    //private ApplicationFinanceHandler applicationFinanceHandler;

    @Autowired
    private SectionService sectionService;

    @Override
    public ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(String applicationName, Long competitionId, Long userId) {

        return find(user(userId), competition(competitionId)).andOnSuccess((user, competition) -> {

           Application application = new Application();
           application.setName(applicationName);
           LocalDate currentDate = LocalDate.now();
           application.setStartDate(currentDate);

           String name = ApplicationStatusConstants.CREATED.getName();

           List<ApplicationStatus> applicationStatusList = applicationStatusRepository.findByName(name);
           ApplicationStatus applicationStatus = applicationStatusList.get(0);

           application.setApplicationStatus(applicationStatus);
           application.setDurationInMonths(3L);

           List<Role> roles = roleRepository.findByName(UserRoleType.LEADAPPLICANT.getName());
           Role role = roles.get(0);

           Organisation userOrganisation = user.getProcessRoles().get(0).getOrganisation();

           ProcessRole processRole = new ProcessRole(user, application, role, userOrganisation);

           List<ProcessRole> processRoles = new ArrayList<>();
           processRoles.add(processRole);

           application.setProcessRoles(processRoles);
           application.setCompetition(competition);

           applicationRepository.save(application);
           processRoleRepository.save(processRole);

           return serviceSuccess(applicationMapper.mapToResource(application));
       });
    }

    @Override
    public ServiceResult<Pair<File, FormInputResponseFileEntryResource>> createFormInputResponseFileUpload(FormInputResponseFileEntryResource formInputResponseFile, Supplier<InputStream> inputStreamSupplier) {

        long applicationId = formInputResponseFile.getCompoundId().getApplicationId();
        long processRoleId = formInputResponseFile.getCompoundId().getProcessRoleId();
        long formInputId = formInputResponseFile.getCompoundId().getFormInputId();

        FormInputResponse existingResponse = formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(applicationId, processRoleId, formInputId);

        if (existingResponse != null && existingResponse.getFileEntry() != null) {
            return serviceFailure(new Error(FILES_FILE_ALREADY_LINKED_TO_FORM_INPUT_RESPONSE, existingResponse.getFileEntry().getId()));
        } else {

            return fileService.createFile(formInputResponseFile.getFileEntryResource(), inputStreamSupplier).andOnSuccess(successfulFile -> {

                FileEntry fileEntry = successfulFile.getValue();

                if (existingResponse != null) {

                    existingResponse.setFileEntry(fileEntry);
                    formInputResponseRepository.save(existingResponse);
                    FormInputResponseFileEntryResource fileEntryResource = new FormInputResponseFileEntryResource(FileEntryResourceAssembler.valueOf(fileEntry), formInputResponseFile.getCompoundId());
                    return serviceSuccess(Pair.of(successfulFile.getKey(), fileEntryResource));

                }

                return find(processRole(processRoleId), () -> getFormInput(formInputId), application(applicationId)).andOnSuccess((processRole, formInput, application) -> {

                    FormInputResponse newFormInputResponse = new FormInputResponse(LocalDateTime.now(), fileEntry, processRole, formInput, application);
                    formInputResponseRepository.save(newFormInputResponse);
                    FormInputResponseFileEntryResource fileEntryResource = new FormInputResponseFileEntryResource(FileEntryResourceAssembler.valueOf(fileEntry), formInputId, applicationId, processRoleId);
                    return serviceSuccess(Pair.of(successfulFile.getKey(), fileEntryResource));
                });
            });
        }
    }

    @Override
    public ServiceResult<Pair<File, FormInputResponseFileEntryResource>> updateFormInputResponseFileUpload(FormInputResponseFileEntryResource formInputResponseFile, Supplier<InputStream> inputStreamSupplier) {

        ServiceResult<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> existingFileResult =
                getFormInputResponseFileUpload(formInputResponseFile.getCompoundId());

        return existingFileResult.andOnSuccess(existingFile -> {

            FormInputResponseFileEntryResource existingFormInputResource = existingFile.getKey();

            FileEntryResource existingFileResource = existingFormInputResource.getFileEntryResource();
            FileEntryResource updatedFileDetails = formInputResponseFile.getFileEntryResource();
            FileEntryResource updatedFileDetailsWithId = new FileEntryResource(existingFileResource.getId(), updatedFileDetails.getName(), updatedFileDetails.getMediaType(), updatedFileDetails.getFilesizeBytes());

            return fileService.updateFile(updatedFileDetailsWithId, inputStreamSupplier).andOnSuccessReturn(updatedFile ->
                    Pair.of(updatedFile.getKey(), existingFormInputResource)
            );
        });
    }

    @Override
    public ServiceResult<FormInputResponse> deleteFormInputResponseFileUpload(FormInputResponseFileEntryId formInputResponseFileId) {

        ServiceResult<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> existingFileResult =
                getFormInputResponseFileUpload(formInputResponseFileId);

        return existingFileResult.andOnSuccess(existingFile -> {

            FormInputResponseFileEntryResource formInputFileEntryResource = existingFile.getKey();
            Long fileEntryId = formInputFileEntryResource.getFileEntryResource().getId();

            return fileService.deleteFile(fileEntryId).
                    andOnSuccess(deletedFile -> getFormInputResponse(formInputFileEntryResource.getCompoundId()).
                    andOnSuccess(this::unlinkFileEntryFromFormInputResponse)
            );
        });
    }

    @Override
    public ServiceResult<Pair<FormInputResponseFileEntryResource, Supplier<InputStream>>> getFormInputResponseFileUpload(FormInputResponseFileEntryId fileEntryId) {

        return getFormInputResponse(fileEntryId).
                andOnSuccess(formInputResponse -> fileService.getFileByFileEntryId(formInputResponse.getFileEntry().getId()).
                andOnSuccessReturn(inputStreamSupplier -> Pair.of(formInputResponseFileEntryResource(formInputResponse.getFileEntry(), fileEntryId), inputStreamSupplier)
        ));
    }

    private ServiceResult<FormInputResponse> unlinkFileEntryFromFormInputResponse(FormInputResponse formInputResponse) {
        formInputResponse.setFileEntry(null);
        FormInputResponse unlinkedResponse = formInputResponseRepository.save(formInputResponse);
        return serviceSuccess(unlinkedResponse);
    }

    private ServiceResult<FormInput> getFormInput(long formInputId) {
        return find(formInputRepository.findOne(formInputId), notFoundError(FormInput.class, formInputId));
    }

    private FormInputResponseFileEntryResource formInputResponseFileEntryResource(FileEntry fileEntry, FormInputResponseFileEntryId fileEntryId) {
        FileEntryResource fileEntryResource = FileEntryResourceAssembler.valueOf(fileEntry);
        return new FormInputResponseFileEntryResource(fileEntryResource, fileEntryId.getFormInputId(), fileEntryId.getApplicationId(), fileEntryId.getProcessRoleId());
    }

    private ServiceResult<FormInputResponse> getFormInputResponse(FormInputResponseFileEntryId fileEntry) {
        Error formInputResponseNotFoundError = notFoundError(FormInputResponse.class, fileEntry.getApplicationId(), fileEntry.getProcessRoleId(), fileEntry.getFormInputId());
        return find(formInputResponseRepository.findByApplicationIdAndUpdatedByIdAndFormInputId(
                fileEntry.getApplicationId(),
                fileEntry.getProcessRoleId(),
                fileEntry.getFormInputId()),
                formInputResponseNotFoundError);
    }

    @Override
    public ServiceResult<ApplicationResource> getApplicationById(final Long id) {
        return getApplication(id).andOnSuccessReturn(applicationMapper::mapToResource);
    }

    @Override
    public ServiceResult<List<ApplicationResource>> findAll() {
        ServiceResult<List<ApplicationResource>> res = serviceSuccess(applicationsToResources(applicationRepository.findAll()));
        return res;
    }

    @Override
    public ServiceResult<List<ApplicationResource>> findByUserId(final Long userId) {
        return getUser(userId).andOnSuccessReturn(user -> {
            List<ProcessRole> roles = processRoleRepository.findByUser(user);
            List<Application> applications = simpleMap(roles, ProcessRole::getApplication);
            return applicationsToResources(applications);
        });
    }

    @Override
    public ServiceResult<ApplicationResource> saveApplicationDetails(final Long id, ApplicationResource application) {

        return getApplication(id).andOnSuccessReturn(existingApplication -> {

            existingApplication.setName(application.getName());
            existingApplication.setDurationInMonths(application.getDurationInMonths());
            existingApplication.setStartDate(application.getStartDate());
            Application savedApplication = applicationRepository.save(existingApplication);
            return applicationMapper.mapToResource(savedApplication);
        });
    }

    // TODO DW - INFUND-1555 - try to remove ObjectNode usage
    @Override
    public ServiceResult<ObjectNode> getProgressPercentageNodeByApplicationId(final Long applicationId) {

        return getProgressPercentageByApplicationId(applicationId).andOnSuccessReturn(percentage -> {

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("completedPercentage", percentage);
            return node;
        });
    }

    @Override
    public ServiceResult<ApplicationResource> updateApplicationStatus(final Long id,
                                                         final Long statusId) {
        return find(application(id), applicationStatus(statusId)).andOnSuccess((application, applicationStatus) -> {

            application.setApplicationStatus(applicationStatus);
            applicationRepository.save(application);
            return serviceSuccess(applicationMapper.mapToResource(application));
        });
    }


    @Override
    public ServiceResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(final Long competitionId,
                                                                             final Long userId,
                                                                             final UserRoleType role) {

        List<Application> allApps = applicationRepository.findAll();
        List<Application> filtered = simpleFilter(allApps, app -> app.getCompetition().getId().equals(competitionId) &&
                applicationContainsUserRole(app.getProcessRoles(), userId, role));
        List<ApplicationResource> filteredResource = applicationsToResources(filtered);
        return serviceSuccess(filteredResource);
    }

    private static boolean applicationContainsUserRole(List<ProcessRole> roles, final Long userId, UserRoleType role) {
        boolean contains = false;
        int i = 0;
        while (!contains && i < roles.size()) {
            contains = roles.get(i).getUser().getId().equals(userId) && roles.get(i).getRole().getName().equals(role.getName());
            i++;
        }

        return contains;
    }

    @Override
    public ServiceResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(
            final Long competitionId,
            final Long userId,
            final String applicationName) {

        return find(user(userId), competition(competitionId)).andOnSuccess((user, competition) -> {

           Application application = new Application();
           application.setName(applicationName);
           LocalDate currentDate = LocalDate.now();
           application.setStartDate(currentDate);

           String name = ApplicationStatusConstants.CREATED.getName();

           List<ApplicationStatus> applicationStatusList = applicationStatusRepository.findByName(name);
           ApplicationStatus applicationStatus = applicationStatusList.get(0);

           application.setApplicationStatus(applicationStatus);
           application.setDurationInMonths(3L);

           List<Role> roles = roleRepository.findByName("leadapplicant");
           Role role = roles.get(0);

           Organisation userOrganisation = user.getOrganisations().get(0);

           ProcessRole processRole = new ProcessRole(user, application, role, userOrganisation);

           List<ProcessRole> processRoles = new ArrayList<>();
           processRoles.add(processRole);

           application.setProcessRoles(processRoles);
           application.setCompetition(competition);

           Application createdApplication = applicationRepository.save(application);
           processRoleRepository.save(processRole);

           return serviceSuccess(applicationMapper.mapToResource(createdApplication));
        });
    }

    @Override
    public ServiceResult<ApplicationResource> findByProcessRole(final Long id){

        return getProcessRole(id).andOnSuccessReturn(processRole ->
            applicationMapper.mapToResource(processRole.getApplication())
        );
    }

    // TODO DW - INFUND-1555 - try to remove the usage of ObjectNode
    @Override
    public ServiceResult<ObjectNode> applicationReadyForSubmit(Long id) {

        return find(application(id), () -> getProgressPercentageByApplicationId(id)).andOnSuccess((application, progressPercentage) ->

            sectionService.childSectionsAreCompleteForAllOrganisations(null, id, null).andOnSuccessReturn(allSectionsComplete -> {

                Competition competition = application.getCompetition();
                BigDecimal researchParticipation = BigDecimal.ZERO; //applicationFinanceHandler.getResearchParticipationPercentage(id);

                boolean readyForSubmit = false;
                if (allSectionsComplete &&
                        progressPercentage.compareTo(BigDecimal.valueOf(100)) == 0 &&
                        researchParticipation.compareTo(BigDecimal.valueOf(competition.getMaxResearchRatio())) == -1) {
                    readyForSubmit = true;
                }

                ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();
                node.put(READY_FOR_SUBMIT, readyForSubmit);
                node.put(PROGRESS, progressPercentage);
                node.put(RESEARCH_PARTICIPATION, researchParticipation);
                node.put(RESEARCH_PARTICIPATION_VALID, (researchParticipation.compareTo(BigDecimal.valueOf(competition.getMaxResearchRatio())) == -1));
                node.put(ALL_SECTION_COMPLETE, allSectionsComplete);
                return node;
            })
        );
    }

    // TODO DW - INFUND-1555 - deal with rest results
    private ServiceResult<BigDecimal> getProgressPercentageByApplicationId(final Long applicationId) {
        return getApplication(applicationId).andOnSuccessReturn(application -> {

            List<Section> sections = application.getCompetition().getSections();

            List<Question> questions = sections.stream()
                    .flatMap(section -> section.getQuestions().stream())
                    .filter(Question::isMarkAsCompletedEnabled)
                    .collect(toList());

            List<ProcessRole> processRoles = application.getProcessRoles();

            Set<Organisation> organisations = processRoles.stream()
                    .filter(p -> p.getRole().getName().equals(UserRoleType.LEADAPPLICANT.getName()) || p.getRole().getName().equals(UserRoleType.APPLICANT.getName()) || p.getRole().getName().equals(UserRoleType.COLLABORATOR.getName()))
                    .map(ProcessRole::getOrganisation).collect(Collectors.toSet());

            Long countMultipleStatusQuestionsCompleted = organisations.stream()
                    .mapToLong(org -> questions.stream()
                            .filter(Question::getMarkAsCompletedEnabled)
                            .filter(q -> q.hasMultipleStatuses() && questionService.isMarkedAsComplete(q, applicationId, org.getId()).getSuccessObject()).count())
                    .sum();

            Long countSingleStatusQuestionsCompleted = questions.stream()
                    .filter(Question::getMarkAsCompletedEnabled)
                    .filter(q -> !q.hasMultipleStatuses() && questionService.isMarkedAsComplete(q, applicationId, 0L).getSuccessObject()).count();

            Long countCompleted = countMultipleStatusQuestionsCompleted + countSingleStatusQuestionsCompleted;

            Long totalMultipleStatusQuestions = questions.stream().filter(Question::hasMultipleStatuses).count() * organisations.size();
            Long totalSingleStatusQuestions = questions.stream().filter(q -> !q.hasMultipleStatuses()).count();

            Long totalQuestions = totalMultipleStatusQuestions + totalSingleStatusQuestions;
            LOG.info("Total questions" + totalQuestions);
            LOG.info("Total completed questions" + countCompleted);

            if (questions.isEmpty()) {
                return BigDecimal.ZERO;
            } else if (totalQuestions.compareTo(countCompleted) == 0){
                return BigDecimal.valueOf(100).setScale(2); // make sure there is no result like 100.0000000001 because of rounding issues.
            } else {
                BigDecimal result = BigDecimal.valueOf(100.00).setScale(10, BigDecimal.ROUND_HALF_DOWN);
                result = result.divide(new BigDecimal(totalQuestions.toString()), 10, BigDecimal.ROUND_HALF_UP);
                result = result.multiply(new BigDecimal(countCompleted.toString()));
//                (100.0 / totalQuestions) * countCompleted);
                return result;
            }
        });
    }

    private List<ApplicationResource> applicationsToResources(List<Application> filtered) {
        return simpleMap(filtered, application -> applicationMapper.mapToResource(application));
    }
}
