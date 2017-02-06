package org.innovateuk.ifs.competition.controller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationFundingDecisionService;
import org.innovateuk.ifs.util.MapFunctions;

/**
 * This controller handles restful calls from javascript to autosave funding decisions.
 */
@RestController
@PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin')")
public class FundingDecisionRestController {

	private static final Log LOG = LogFactory.getLog(FundingDecisionRestController.class);
	
	@Autowired
	private ApplicationFundingDecisionService applicationFundingDecisionService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/funding/{competitionId}")
	public Map<String, Object> saveFundingDecision(@PathVariable("competitionId") Long competitionId, @RequestParam("applicationId") Long applicationId, @RequestParam("fundingDecision") String decision) {
		
		Map<Long, FundingDecision> applicationIdToFundingDecision = MapFunctions.asMap(applicationId, applicationFundingDecisionService.fundingDecisionForString(decision));
		
		try {
			applicationFundingDecisionService.saveApplicationFundingDecisionData(competitionId, applicationIdToFundingDecision);
		} catch (RuntimeException e) {
			LOG.error("exception thrown when saving application funding decision data", e);
			return MapFunctions.asMap("success", "false");
		}
		return MapFunctions.asMap("success", "true");
    }

}
