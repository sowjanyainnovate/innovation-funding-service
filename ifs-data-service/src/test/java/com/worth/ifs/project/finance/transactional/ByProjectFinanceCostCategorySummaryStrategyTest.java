package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.finance.resource.ProjectFinanceResource;
import com.worth.ifs.finance.resource.category.FinanceRowCostCategory;
import com.worth.ifs.finance.resource.cost.FinanceRowType;
import com.worth.ifs.project.finance.domain.CostCategory;
import com.worth.ifs.project.finance.domain.CostCategoryGroup;
import com.worth.ifs.project.finance.domain.CostCategoryType;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.resource.OrganisationResource;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.finance.builder.DefaultCostCategoryBuilder.newDefaultCostCategory;
import static com.worth.ifs.finance.builder.LabourCostBuilder.newLabourCost;
import static com.worth.ifs.finance.builder.LabourCostCategoryBuilder.newLabourCostCategory;
import static com.worth.ifs.finance.builder.MaterialsCostBuilder.newMaterials;
import static com.worth.ifs.finance.builder.OtherCostBuilder.newOtherCost;
import static com.worth.ifs.finance.builder.ProjectFinanceResourceBuilder.newProjectFinanceResource;
import static com.worth.ifs.finance.resource.category.LabourCostCategory.WORKING_DAYS_PER_YEAR;
import static com.worth.ifs.finance.resource.cost.AcademicCostCategoryGenerator.*;
import static com.worth.ifs.project.builder.CostCategoryBuilder.newCostCategory;
import static com.worth.ifs.project.builder.CostCategoryGroupBuilder.newCostCategoryGroup;
import static com.worth.ifs.project.builder.CostCategoryTypeBuilder.newCostCategoryType;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.user.builder.OrganisationResourceBuilder.newOrganisationResource;
import static com.worth.ifs.util.CollectionFunctions.simpleFindFirst;
import static com.worth.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ByProjectFinanceCostCategorySummaryStrategyTest extends BaseServiceUnitTest<ByProjectFinanceCostCategorySummaryStrategy> {

    @Mock
    private CostCategoryTypeStrategy costCategoryTypeStrategyMock;

    @Test
    public void testGenerateSpendProfileForIndustrialOrganisation() {

        ProjectResource project = newProjectResource().
                withDuration(10L).
                build();

        OrganisationResource organisation = newOrganisationResource().build();

        Map<FinanceRowType, FinanceRowCostCategory> finances = asMap(
                FinanceRowType.LABOUR, newLabourCostCategory().
                        withCosts(
                            newLabourCost().
                                    withGrossAnnualSalary(new BigDecimal("10000.23"), new BigDecimal("5100.11"), BigDecimal.ZERO).
                                    withDescription("Developers", "Testers", WORKING_DAYS_PER_YEAR).
                                    withLabourDays(100, 120, 250).
                                    build(3)).
                        build(),
                FinanceRowType.MATERIALS, newDefaultCostCategory().withCosts(
                        newMaterials().
                                withCost(new BigDecimal("33.33"), new BigDecimal("98.51")).
                                withQuantity(1, 2).
                                build(2)).
                        build());

        finances.forEach((type, category) -> category.calculateTotal());

        ProjectFinanceResource projectFinance = newProjectFinanceResource().
                withFinanceOrganisationDetails(finances).
                build();

        List<CostCategory> costCategories = newCostCategory().
                withName("Labour", "Materials").
                build(2);

        CostCategoryGroup costCategoryGroup = newCostCategoryGroup().
                withCostCategories(costCategories).
                build();

        CostCategoryType costCategoryType = newCostCategoryType().withCostCategoryGroup(costCategoryGroup).build();

        when(projectServiceMock.getProjectById(project.getId())).thenReturn(serviceSuccess(project));
        when(organisationServiceMock.findById(organisation.getId())).thenReturn(serviceSuccess(organisation));
        when(financeRowServiceMock.financeChecksDetails(project.getId(), organisation.getId())).thenReturn(serviceSuccess(projectFinance));
        when(organisationFinanceDelegateMock.isUsingJesFinances(organisation.getOrganisationTypeName())).thenReturn(false);
        when(costCategoryTypeStrategyMock.getOrCreateCostCategoryTypeForSpendProfile(project.getId(), organisation.getId())).thenReturn(serviceSuccess(costCategoryType));

        ServiceResult<SpendProfileCostCategorySummaries> result = service.getCostCategorySummaries(project.getId(), organisation.getId());
        assertTrue(result.isSuccess());

        SpendProfileCostCategorySummaries summaries = result.getSuccessObject();
        assertEquals(costCategoryType, summaries.getCostCategoryType());
        assertEquals(2, summaries.getCosts().size());

        SpendProfileCostCategorySummary summary1 = simpleFindFirst(summaries.getCosts(),
                s -> s.getCategory().equals(costCategories.get(0))).get();

        SpendProfileCostCategorySummary summary2 = simpleFindFirst(summaries.getCosts(),
                s -> s.getCategory().equals(costCategories.get(1))).get();


        assertEquals(new BigDecimal("6448.14480"), summary1.getTotal());
        assertEquals(new BigDecimal("643"), summary1.getFirstMonthSpend());
        assertEquals(new BigDecimal("645"), summary1.getOtherMonthsSpend());

        assertEquals(new BigDecimal("230.35"), summary2.getTotal());
        assertEquals(new BigDecimal("23"), summary2.getFirstMonthSpend());
        assertEquals(new BigDecimal("23"), summary2.getOtherMonthsSpend());
    }

    @Test
    public void testGenerateSpendProfileForAcademicOrganisation() {

        ProjectResource project = newProjectResource().
                withDuration(10L).
                build();

        OrganisationResource organisation = newOrganisationResource().build();

        Map<FinanceRowType, FinanceRowCostCategory> finances = asMap(
                FinanceRowType.LABOUR, newLabourCostCategory().withCosts(
                        newLabourCost().
                                withGrossAnnualSalary(new BigDecimal("10000.23"), new BigDecimal("5100.11"), new BigDecimal("600.11"), BigDecimal.ZERO).
                                withDescription("Developers", "Testers", "Something else", WORKING_DAYS_PER_YEAR).
                                withLabourDays(100, 120, 120, 250).
                                withName(DIRECTLY_INCURRED_STAFF.getFinanceRowName(), DIRECTLY_INCURRED_STAFF.getFinanceRowName(), INDIRECT_COSTS_STAFF.getFinanceRowName()).
                                build(4)).
                        build(),
                FinanceRowType.OTHER_COSTS, newDefaultCostCategory().withCosts(
                        newOtherCost().
                                withCost(new BigDecimal("33.33"), new BigDecimal("98.51")).
                                withName(DIRECTLY_INCURRED_OTHER_COSTS.getFinanceRowName(), INDIRECT_COSTS_OTHER_COSTS.getFinanceRowName()).
                                build(2)).
                        build());

        finances.forEach((type, category) -> category.calculateTotal());

        ProjectFinanceResource projectFinance = newProjectFinanceResource().
                withFinanceOrganisationDetails(finances).
                build();

        List<CostCategory> costCategories = newCostCategory().
                withName(DIRECTLY_INCURRED_STAFF.getName(),
                        INDIRECT_COSTS_STAFF.getName(),
                        DIRECTLY_INCURRED_OTHER_COSTS.getName(),
                        INDIRECT_COSTS_OTHER_COSTS.getName()).
                withLabel(DIRECTLY_INCURRED_STAFF.getLabel(),
                        INDIRECT_COSTS_STAFF.getLabel(),
                        DIRECTLY_INCURRED_OTHER_COSTS.getLabel(),
                        INDIRECT_COSTS_OTHER_COSTS.getLabel()).
                build(4);

        CostCategoryGroup costCategoryGroup = newCostCategoryGroup().
                withCostCategories(costCategories).
                build();

        CostCategoryType costCategoryType = newCostCategoryType().withCostCategoryGroup(costCategoryGroup).build();

        when(projectServiceMock.getProjectById(project.getId())).thenReturn(serviceSuccess(project));
        when(organisationServiceMock.findById(organisation.getId())).thenReturn(serviceSuccess(organisation));
        when(financeRowServiceMock.financeChecksDetails(project.getId(), organisation.getId())).thenReturn(serviceSuccess(projectFinance));
        when(organisationFinanceDelegateMock.isUsingJesFinances(organisation.getOrganisationTypeName())).thenReturn(true);
        when(costCategoryTypeStrategyMock.getOrCreateCostCategoryTypeForSpendProfile(project.getId(), organisation.getId())).thenReturn(serviceSuccess(costCategoryType));

        ServiceResult<SpendProfileCostCategorySummaries> result = service.getCostCategorySummaries(project.getId(), organisation.getId());
        assertTrue(result.isSuccess());

        SpendProfileCostCategorySummaries summaries = result.getSuccessObject();
        assertEquals(costCategoryType, summaries.getCostCategoryType());
        assertEquals(4, summaries.getCosts().size());

        SpendProfileCostCategorySummary summary1 = simpleFindFirst(summaries.getCosts(),
                s -> s.getCategory().equals(costCategories.get(0))).get();

        SpendProfileCostCategorySummary summary2 = simpleFindFirst(summaries.getCosts(),
                s -> s.getCategory().equals(costCategories.get(1))).get();

        SpendProfileCostCategorySummary summary3 = simpleFindFirst(summaries.getCosts(),
                s -> s.getCategory().equals(costCategories.get(2))).get();

        SpendProfileCostCategorySummary summary4 = simpleFindFirst(summaries.getCosts(),
                s -> s.getCategory().equals(costCategories.get(3))).get();

        assertEquals(new BigDecimal("6448.14480"), summary1.getTotal());
        assertEquals(new BigDecimal("643"), summary1.getFirstMonthSpend());
        assertEquals(new BigDecimal("645"), summary1.getOtherMonthsSpend());

        assertEquals(new BigDecimal("288.05280"), summary2.getTotal());
        assertEquals(new BigDecimal("27"), summary2.getFirstMonthSpend());
        assertEquals(new BigDecimal("29"), summary2.getOtherMonthsSpend());

        assertEquals(new BigDecimal("33.33"), summary3.getTotal());
        assertEquals(new BigDecimal("6"), summary3.getFirstMonthSpend());
        assertEquals(new BigDecimal("3"), summary3.getOtherMonthsSpend());

        assertEquals(new BigDecimal("98.51"), summary4.getTotal());
        assertEquals(new BigDecimal("9"), summary4.getFirstMonthSpend());
        assertEquals(new BigDecimal("10"), summary4.getOtherMonthsSpend());
    }

    @Override
    protected ByProjectFinanceCostCategorySummaryStrategy supplyServiceUnderTest() {
        return new ByProjectFinanceCostCategorySummaryStrategy();
    }
}
