package org.innovateuk.ifs.project.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.project.resource.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * ProjectFinanceController exposes Project finance data and operations through a REST API.
 */
@RestController
@RequestMapping("/project")
public class ProjectFinanceController {

    @Autowired
    private com.worth.ifs.project.finance.transactional.ProjectFinanceService projectFinanceService;

    @RequestMapping(value = "/{projectId}/spend-profile/generate", method = POST)
    public RestResult<Void> generateSpendProfile(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.generateSpendProfile(projectId).toPostCreateResponse();
    }

    @RequestMapping(value = "/{projectId}/spend-profile/approval/{approvalType}", method = POST)
    public RestResult<Void> approveOrRejectSpendProfile(@PathVariable("projectId") final Long projectId,
                                                        @PathVariable("approvalType") final ApprovalType approvalType) {
        return projectFinanceService.approveOrRejectSpendProfile(projectId, approvalType).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/spend-profile/approval", method = GET)
    public RestResult<ApprovalType> getSpendProfileStatusByProjectId(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.getSpendProfileStatusByProjectId(projectId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/spend-profile-table")
    public RestResult<SpendProfileTableResource> getSpendProfileTable(@PathVariable("projectId") final Long projectId,
                                                                      @PathVariable("organisationId") final Long organisationId) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);

        return projectFinanceService.getSpendProfileTable(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/spend-profile-csv")
    public RestResult<SpendProfileCSVResource> getSpendProfileCSV(@PathVariable("projectId") final Long projectId,
                                                                  @PathVariable("organisationId") final Long organisationId) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.getSpendProfileCSV(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping("/{projectId}/partner-organisation/{organisationId}/spend-profile")
    public RestResult<SpendProfileResource> getSpendProfile(@PathVariable("projectId") final Long projectId,
                                                            @PathVariable("organisationId") final Long organisationId) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.getSpendProfile(projectOrganisationCompositeId).toGetResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/spend-profile", method = POST)
    public RestResult<Void> saveSpendProfile(@PathVariable("projectId") final Long projectId,
                                             @PathVariable("organisationId") final Long organisationId,
                                             @RequestBody SpendProfileTableResource table) {

        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.saveSpendProfile(projectOrganisationCompositeId, table).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/partner-organisation/{organisationId}/spend-profile/complete/{complete}", method = POST)
    public RestResult<Void> markSpendProfileCompete(@PathVariable("projectId") final Long projectId,
                                                    @PathVariable("organisationId") final Long organisationId,
                                                    @PathVariable("complete") final Boolean complete) {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(projectId, organisationId);
        return projectFinanceService.markSpendProfile(projectOrganisationCompositeId, complete).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/complete-spend-profiles-review", method = POST)
    public RestResult<Void> completeSpendProfilesReview(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.completeSpendProfilesReview(projectId).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/project-finance/totals", method = GET)
    public RestResult<List<ProjectFinanceResource>> getProjectFinanceTotals(@PathVariable("projectId") final Long projectId) {
        return projectFinanceService.getProjectFinanceTotals(projectId).toGetResponse();
    }
}
