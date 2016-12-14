package org.innovateuk.ifs.project.util;

import org.innovateuk.ifs.commons.rest.LocalDateResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.viewmodel.SpendProfileSummaryModel;
import org.innovateuk.ifs.project.viewmodel.SpendProfileSummaryYearModel;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMapValue;

/**
 * Component for calculating row and column totals for spend profile tables.
 */
@Component
public class SpendProfileTableCalculator {

    public Map<Long, BigDecimal> calculateRowTotal(Map<Long, List<BigDecimal>> tableData) {
        return simpleMapValue(tableData, rows ->
                rows.stream().filter(row -> row != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public List<BigDecimal> calculateMonthlyTotals(Map<Long, List<BigDecimal>> tableData, int numberOfMonths) {
        return IntStream.range(0, numberOfMonths).mapToObj(index ->
                tableData.values()
                        .stream()
                        .map(list -> list.get(index))
                        .filter(data -> data != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        ).collect(Collectors.toList());
    }

    public BigDecimal calculateTotalOfAllActualTotals(Map<Long, List<BigDecimal>> tableData) {
        return tableData.values()
                .stream()
                .map(list -> list
                        .stream()
                        .filter(row -> row != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalOfAllEligibleTotals(Map<Long, BigDecimal> eligibleCostData) {
        return eligibleCostData
                .values()
                .stream()
                .filter(row -> row != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public SpendProfileSummaryModel createSpendProfileSummary(ProjectResource project, Map<Long, List<BigDecimal>> tableData, List<LocalDateResource> months) {
        Integer startYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate())).getFiscalYear();
        Integer endYear = new FinancialYearDate(DateUtil.asDate(project.getTargetStartDate().plusMonths(project.getDurationInMonths()))).getFiscalYear();
        List<SpendProfileSummaryYearModel> years = IntStream.range(startYear, endYear + 1).
                mapToObj(
                        year -> {
                            Set<Long> keys = tableData.keySet();
                            BigDecimal totalForYear = BigDecimal.ZERO;

                            for (Long key : keys) {
                                List<BigDecimal> values = tableData.get(key);
                                for (int i = 0; i < values.size(); i++) {
                                    LocalDateResource month = months.get(i);
                                    FinancialYearDate financialYearDate = new FinancialYearDate(DateUtil.asDate(month.getLocalDate()));
                                    if (year == financialYearDate.getFiscalYear()) {
                                        BigDecimal nextValue = values.get(i);

                                        if (nextValue != null) {
                                            totalForYear = totalForYear.add(nextValue);
                                        }
                                    }
                                }
                            }
                            return new SpendProfileSummaryYearModel(year, totalForYear.toPlainString());
                        }

                ).collect(toList());

        return new SpendProfileSummaryModel(years);
    }

}
