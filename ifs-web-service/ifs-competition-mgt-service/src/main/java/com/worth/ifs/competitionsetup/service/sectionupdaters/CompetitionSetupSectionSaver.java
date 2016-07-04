package com.worth.ifs.competitionsetup.service.sectionupdaters;

import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.competition.resource.CompetitionSetupSection;
import com.worth.ifs.competitionsetup.form.CompetitionSetupForm;

public interface CompetitionSetupSectionSaver {

	CompetitionSetupSection sectionToSave();
	
	boolean supportsForm(Class<? extends CompetitionSetupForm> clazz);
	
	void saveSection(CompetitionResource competitionResource, CompetitionSetupForm competitionSetupForm);
}