package org.innovateuk.ifs.assessment.transactional;

import org.innovateuk.ifs.category.mapper.InnovationAreaMapper;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.domain.CompetitionParticipant;
import org.innovateuk.ifs.invite.repository.CompetitionParticipantRepository;
import org.innovateuk.ifs.invite.resource.CompetitionInviteResource;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.RoleResource;
import org.innovateuk.ifs.user.transactional.RegistrationService;
import org.innovateuk.ifs.user.transactional.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.user.resource.UserRoleType.ASSESSOR;

@Service
public class AssessorServiceImpl implements AssessorService {

    @Autowired
    private CompetitionInviteService competitionInviteService;

    @Autowired
    private RegistrationService userRegistrationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CompetitionParticipantRepository competitionParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InnovationAreaMapper innovationAreaMapper;

    @Override
    public ServiceResult<Void> registerAssessorByHash(String inviteHash, UserRegistrationResource userRegistrationResource) {

        // TODO: Handle failures gracefully and hand them back to the webservice
        return retrieveInvite(inviteHash).andOnSuccess(inviteResource -> {
            userRegistrationResource.setEmail(inviteResource.getEmail());
            return getAssessorRoleResource().andOnSuccess(assessorRole -> {
                userRegistrationResource.setRoles(singletonList(assessorRole));
                return createUser(userRegistrationResource).andOnSuccessReturnVoid(created -> {
                    assignCompetitionParticipantsToUser(created);
                    created.addInnovationArea(innovationAreaMapper.mapToDomain(inviteResource.getInnovationArea()));
                    userRepository.save(created);
                });
            });
        });
    }

    private ServiceResult<CompetitionInviteResource> retrieveInvite(String inviteHash) {
        return competitionInviteService.getInvite(inviteHash);
    }

    private void assignCompetitionParticipantsToUser(User user) {
        List<CompetitionParticipant> competitionParticipants = competitionParticipantRepository.getByInviteEmail(user.getEmail());
        competitionParticipants.forEach(competitionParticipant -> competitionParticipant.setUser(user));
        competitionParticipantRepository.save(competitionParticipants);
    }

    private ServiceResult<RoleResource> getAssessorRoleResource() {
        return roleService.findByUserRoleType(ASSESSOR);
    }

    private ServiceResult<User> createUser(UserRegistrationResource userRegistrationResource) {
        return userRegistrationService.createUser(userRegistrationResource).andOnSuccess(
                created -> userRegistrationService.activateUser(created.getId()).andOnSuccessReturn(result -> userRepository.findOne(created.getId())));
    }
}
