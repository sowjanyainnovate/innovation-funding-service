package com.worth.ifs.project.grantofferletter.controller;

import com.worth.ifs.commons.service.FailingOrSucceedingResult;
import com.worth.ifs.controller.ValidationHandler;
import com.worth.ifs.file.controller.viewmodel.FileDetailsViewModel;
import com.worth.ifs.file.resource.FileEntryResource;
import com.worth.ifs.project.ProjectService;
import com.worth.ifs.project.grantofferletter.form.ProjectGrantOfferLetterForm;
import com.worth.ifs.project.grantofferletter.viewmodel.ProjectGrantOfferLetterViewModel;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.function.Supplier;

import static com.worth.ifs.controller.FileUploadControllerUtils.getMultipartFileBytes;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Controller for the grant offer letter
 **/
@Controller
@RequestMapping("/project/{projectId}/offer")
public class ProjectGrantOfferLetterController {

    private static final String FORM_ATTR = "form";
    public static final String BASE_DIR = "project";
    public static final String TEMPLATE_NAME = "grant-offer-letter";

    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = GET)
    public String viewGrantOfferLetterPage(@PathVariable("projectId") Long projectId, Model model,
                                           @ModelAttribute("loggedInUser") UserResource loggedInUser) {
        ProjectGrantOfferLetterForm form = new ProjectGrantOfferLetterForm();

        return createGrantOfferLetterPage(projectId, model, loggedInUser, form);
    }

    @RequestMapping(params = "uploadSignedGrantOfferLetterClicked", method = POST)
    public String uploadGrantOfferLetterFile(
            @PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) ProjectGrantOfferLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "grantOfferLetter", form, () -> {

            MultipartFile signedGrantOfferLetter = form.getSignedGrantOfferLetter();

            return projectService.addSignedGrantOfferLetter(projectId, signedGrantOfferLetter.getContentType(), signedGrantOfferLetter.getSize(),
                    signedGrantOfferLetter.getOriginalFilename(), getMultipartFileBytes(signedGrantOfferLetter));
        });
    }

    @RequestMapping(params = "uploadGeneratedOfferLetterClicked", method = POST)
    public String uploadGeneratedGrantOfferLetterFile(
            @PathVariable("projectId") final Long projectId,
            @ModelAttribute(FORM_ATTR) ProjectGrantOfferLetterForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @ModelAttribute("loggedInUser") UserResource loggedInUser) {

        return performActionOrBindErrorsToField(projectId, validationHandler, model, loggedInUser, "exploitationPlan", form, () -> {

            MultipartFile grantOfferLetter = form.getGrantOfferLetter();

            return projectService.addGeneratedGrantOfferLetter(projectId, grantOfferLetter.getContentType(), grantOfferLetter.getSize(),
                    grantOfferLetter.getOriginalFilename(), getMultipartFileBytes(grantOfferLetter));
        });
    }

    private String createGrantOfferLetterPage(Long projectId, Model model, UserResource loggedInUser, ProjectGrantOfferLetterForm form) {
        ProjectGrantOfferLetterViewModel viewModel = populateGrantOfferLetterViewModel(projectId, loggedInUser);
        model.addAttribute("model", viewModel);
        model.addAttribute("form", form);
        return BASE_DIR + "/" + TEMPLATE_NAME;
    }

    private ProjectGrantOfferLetterViewModel populateGrantOfferLetterViewModel(Long projectId, UserResource loggedInUser) {
        ProjectResource project = projectService.getById(projectId);
        boolean leadPartner = projectService.isUserLeadPartner(projectId, loggedInUser.getId());

        Optional<FileEntryResource> signedGrantOfferLetterFile = projectService.getGrantOfferLetterFileDetails(projectId);

        Optional<FileEntryResource> grantOfferFileDetails = projectService.getGeneratedGrantOfferFileDetails(projectId);

        Optional<FileEntryResource> additionalContractFile = projectService.getAdditionalContractFileDetails(projectId);

        return new ProjectGrantOfferLetterViewModel(projectId, project.getName(),
                leadPartner, grantOfferFileDetails.map(FileDetailsViewModel::new).orElse(null),
                signedGrantOfferLetterFile.map(FileDetailsViewModel::new).orElse(null),
                additionalContractFile.map(FileDetailsViewModel::new).orElse(null),
                project.getOfferSubmittedDate(), project.isOfferRejected());
    }

    private String performActionOrBindErrorsToField(Long projectId, ValidationHandler validationHandler, Model model, UserResource loggedInUser, String fieldName, ProjectGrantOfferLetterForm form, Supplier<FailingOrSucceedingResult<?, ?>> actionFn) {

        Supplier<String> successView = () -> redirectToGrantOfferLetterPage(projectId);
        Supplier<String> failureView = () -> createGrantOfferLetterPage(projectId, model, loggedInUser, form);

        return validationHandler.performActionOrBindErrorsToField(fieldName, failureView, successView, actionFn);
    }

    private String redirectToGrantOfferLetterPage(Long projectId) {
        return "redirect:/project/" + projectId + "/grant-offer";
    }


}
