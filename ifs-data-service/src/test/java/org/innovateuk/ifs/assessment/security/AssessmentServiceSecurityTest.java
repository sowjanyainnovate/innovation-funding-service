package org.innovateuk.ifs.assessment.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.assessment.resource.*;
import org.innovateuk.ifs.assessment.transactional.AssessmentService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.method.P;

import java.util.List;

import static org.innovateuk.ifs.assessment.builder.ApplicationRejectionResourceBuilder.newApplicationRejectionResource;
import static org.innovateuk.ifs.assessment.builder.AssessmentFundingDecisionResourceBuilder.newAssessmentFundingDecisionResource;
import static org.innovateuk.ifs.assessment.builder.AssessmentSubmissionsResourceBuilder.newAssessmentSubmissionsResource;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static java.util.Arrays.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class AssessmentServiceSecurityTest extends BaseServiceSecurityTest<AssessmentService> {

    private AssessmentPermissionRules assessmentPermissionRules;
    private AssessmentLookupStrategy assessmentLookupStrategy;

    @Override
    protected Class<? extends AssessmentService> getClassUnderTest() {
        return TestAssessmentService.class;
    }

    @Before
    public void setUp() throws Exception {
        assessmentPermissionRules = getMockPermissionRulesBean(AssessmentPermissionRules.class);
        assessmentLookupStrategy = getMockPermissionEntityLookupStrategiesBean(AssessmentLookupStrategy.class);
    }

    private static Long ID_TO_FIND = 1L;
    private static int ARRAY_SIZE_FOR_POST_FILTER_TESTS = 2;

    @Test
    public void findById() {
        AssessmentResource assessmentResource = newAssessmentResource().with(id(ID_TO_FIND)).build();

        assertAccessDenied(
                () -> classUnderTest.findById(ID_TO_FIND),
                () -> verify(assessmentPermissionRules).userCanReadAssessment(eq(assessmentResource), isA(UserResource.class))
        );
    }

    @Test
    public void findByUserAndCompetition() {
        long userId = 3L;
        long competitionId = 1L;

        classUnderTest.findByUserAndCompetition(userId, competitionId);
        verify(assessmentPermissionRules, times(ARRAY_SIZE_FOR_POST_FILTER_TESTS)).userCanReadAssessmentOnDashboard(isA(AssessmentResource.class), isA(UserResource.class));
    }

    @Test
    public void getTotalScore() {
        Long assessmentId = 1L;
        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().withId(assessmentId).build());
        assertAccessDenied(
                () -> classUnderTest.getTotalScore(assessmentId),
                () -> verify(assessmentPermissionRules).userCanReadAssessmentScore(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void recommend() {
        Long assessmentId = 1L;
        AssessmentFundingDecisionResource assessmentFundingDecision = newAssessmentFundingDecisionResource().build();
        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().withId(assessmentId).build());
        assertAccessDenied(
                () -> classUnderTest.recommend(assessmentId, assessmentFundingDecision),
                () -> verify(assessmentPermissionRules).userCanUpdateAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void rejectInvitation() {
        Long assessmentId = 1L;
        ApplicationRejectionResource applicationRejection = newApplicationRejectionResource().build();
        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().withId(assessmentId).build());
        assertAccessDenied(
                () -> classUnderTest.rejectInvitation(assessmentId, applicationRejection),
                () -> verify(assessmentPermissionRules).userCanUpdateAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void accept() {
        Long assessmentId = 1L;
        when(assessmentLookupStrategy.getAssessmentResource(assessmentId)).thenReturn(newAssessmentResource().withId(assessmentId).build());
        assertAccessDenied(
                () -> classUnderTest.acceptInvitation(assessmentId),
                () -> verify(assessmentPermissionRules).userCanUpdateAssessment(isA(AssessmentResource.class), isA(UserResource.class))
        );
    }

    @Test
    public void submitAssessments() throws Exception {
        AssessmentSubmissionsResource assessmentSubmissions = newAssessmentSubmissionsResource().withAssessmentIds(asList(1L, 2L)).build();

        assertAccessDenied(
                () -> classUnderTest.submitAssessments(assessmentSubmissions),
                () -> verify(assessmentPermissionRules).userCanSubmitAssessments(isA(AssessmentSubmissionsResource.class), isA(UserResource.class))
        );
    }

    public static class TestAssessmentService implements AssessmentService {
        @Override
        public ServiceResult<AssessmentResource> findById(Long id) {
            return serviceSuccess(newAssessmentResource().with(id(ID_TO_FIND)).build());
        }

        @Override
        public ServiceResult<List<AssessmentResource>> findByUserAndCompetition(Long userId, Long competitionId) {
            return serviceSuccess(newAssessmentResource().build(ARRAY_SIZE_FOR_POST_FILTER_TESTS));
        }

        @Override
        public ServiceResult<AssessmentTotalScoreResource> getTotalScore(Long assessmentId) {
            return null;
        }

        @Override
        public ServiceResult<Void> recommend(@P("assessmentId") Long assessmentId, AssessmentFundingDecisionResource assessmentFundingDecision) {
            return null;
        }

        @Override
        public ServiceResult<Void> rejectInvitation(@P("assessmentId") Long assessmentId, ApplicationRejectionResource applicationRejection) {
            return null;
        }

        @Override
        public ServiceResult<Void> acceptInvitation(@P("assessmentId") Long assessmentId) {
            return null;
        }

        @Override
        public ServiceResult<Void> submitAssessments(@P("assessmentSubmissions") AssessmentSubmissionsResource assessmentSubmissionsResource) {
            return null;
        }
    }
}
