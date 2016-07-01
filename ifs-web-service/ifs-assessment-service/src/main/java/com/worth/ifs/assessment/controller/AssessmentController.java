package com.worth.ifs.assessment.controller;

import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.QuestionResource;
import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.application.service.QuestionService;
import com.worth.ifs.assessment.resource.AssessmentFeedbackResource;
import com.worth.ifs.assessment.service.AssessmentFeedbackService;
import com.worth.ifs.assessment.service.AssessmentService;
import com.worth.ifs.assessment.viewmodel.AssessmentSummaryViewModel;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.user.resource.ProcessRoleResource;
import com.worth.ifs.user.service.ProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Exposes CRUD operations through a REST API to manage assessment related web data operations.
 */
@Controller
@RequestMapping("/assessment/summary")
public class AssessmentController {

    @Autowired
    private AssessmentFeedbackService assessmentFeedbackService;
    @Autowired
    private AssessmentService assessmentService;
    @Autowired
    private ProcessRoleService  processRoleService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(value= "/{assessmentId}",
                    method = RequestMethod.GET)
    public String getSummary(
            @PathVariable("assessmentId") final Long assessmentId,
            final Model model) throws ExecutionException, InterruptedException {
            model.addAttribute("model", populateModel(assessmentId));
            return "assessor-application-summary";
    }

    private AssessmentSummaryViewModel populateModel(Long assessmentId) throws ExecutionException, InterruptedException {
        List<AssessmentFeedbackResource> listOfAssessmentFeedback = assessmentFeedbackService.getAllAssessmentFeedback(assessmentId);
        List<QuestionResource> listOfQuestionResource = assessmentService.getAllQuestionsById(assessmentId);

        ProcessRoleResource processRole = processRoleService.getById(assessmentService.getById(assessmentId).getProcessRole()).get();
        ApplicationResource application = applicationService.getById(processRole.getApplication());
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        return new AssessmentSummaryViewModel(listOfQuestionResource,listOfAssessmentFeedback,application,competition);

    }


}
