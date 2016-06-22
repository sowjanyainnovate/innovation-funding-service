package com.worth.ifs.service.competitionsetup.sectionupdaters;

import static java.util.Arrays.asList;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worth.ifs.competition.resource.CollaborationLevel;
import com.worth.ifs.competition.resource.LeadApplicantType;
import com.worth.ifs.application.service.CompetitionService;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.controller.form.competitionsetup.CompetitionSetupForm;
import com.worth.ifs.controller.form.competitionsetup.EligibilityForm;
import com.worth.ifs.controller.form.enumerable.ResearchParticipationAmount;

/**
 * Competition setup section saver for the eligibility section.
 */
@Service
public class EligibilitySectionSaver implements CompetitionSetupSectionSaver {

	@Autowired
	private CompetitionService competitionService;
	
	@Override
	public CompetitionSetupSection sectionToSave() {
		return CompetitionSetupSection.ELIGIBILITY;
	}

	@Override
	public void saveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
		
		EligibilityForm eligibilityForm = (EligibilityForm) competitionSetupForm;
		
		competition.setResearchCategories(new HashSet<Long>(asList(eligibilityForm.getResearchCategoryId())));
		
		ResearchParticipationAmount amount = ResearchParticipationAmount.fromId(eligibilityForm.getResearchParticipationAmountId());
		if(amount != null) {
			competition.setMaxResearchRatio(amount.getAmount());
		}
		
		if("yes".equals(eligibilityForm.getMultipleStream())) {
			competition.setMultiStream(true);
		} else {
			competition.setMultiStream(false);
		}

		CollaborationLevel level = CollaborationLevel.fromCode(eligibilityForm.getSingleOrCollaborative());
		competition.setCollaborationLevel(level);
		
		LeadApplicantType type = LeadApplicantType.fromCode(eligibilityForm.getLeadApplicantType());
		competition.setLeadApplicantType(type);
		
		competitionService.update(competition);
	}
	
	@Override
	public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
		return EligibilityForm.class.equals(clazz);
	}

}
