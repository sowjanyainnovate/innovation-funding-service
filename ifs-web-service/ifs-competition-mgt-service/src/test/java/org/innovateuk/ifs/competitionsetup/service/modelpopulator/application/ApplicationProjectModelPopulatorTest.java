package org.innovateuk.ifs.competitionsetup.service.modelpopulator.application;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.form.application.ApplicationFinanceForm;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationProjectModelPopulatorTest {

	@InjectMocks
	private ApplicationProjectModelPopulator populator;

	@Mock
	private CompetitionService competitionService;

	@Mock
	private SectionService sectionService;
	
	@Test
	public void testSectionToPopulateModel() {
		CompetitionSetupSubsection result = populator.sectionToPopulateModel();
		
		assertEquals(CompetitionSetupSubsection.PROJECT_DETAILS, result);
	}
	
	@Test
	public void testPopulateModel() {
		Model model = new ExtendedModelMap();
		CompetitionResource competitionResource = newCompetitionResource()
				.withCompetitionCode("code")
				.withName("name")
				.withId(8L)
				.withResearchCategories(CollectionFunctions.asLinkedSet(2L, 3L))
				.build();
		populator.populateModel(model, competitionResource, Optional.empty());
		assertEquals(0, model.asMap().size());
	}
}
