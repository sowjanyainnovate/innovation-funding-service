package org.innovateuk.ifs.competitionsetup.service.modelpopulator.application;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.service.modelpopulator.CompetitionSetupSubsectionModelPopulator;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

/**
 * populates the model for the initial details competition setup section.
 */
@Service
public class ApplicationQuestionModelPopulator implements CompetitionSetupSubsectionModelPopulator {

    @Override
	public CompetitionSetupSubsection sectionToPopulateModel() {
		return CompetitionSetupSubsection.QUESTIONS;
	}

	@Override
	public void populateModel(Model model, CompetitionResource competitionResource, Optional<Long> objectId) {
    }
}
