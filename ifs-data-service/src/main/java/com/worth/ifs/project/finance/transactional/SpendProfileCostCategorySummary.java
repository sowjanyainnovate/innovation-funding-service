package com.worth.ifs.project.finance.transactional;

import com.worth.ifs.finance.resource.cost.FinanceRowType;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_EVEN;

/**
 * Holder of summary information used to generate Spend Profiles
 */
class SpendProfileCostCategorySummary {

    private FinanceRowType category;
    private BigDecimal total;
    private BigDecimal firstMonthSpend;
    private BigDecimal otherMonthsSpend;

    SpendProfileCostCategorySummary(FinanceRowType category, BigDecimal total, long projectDurationInMonths) {
        this.category = category;
        this.total = total;

        BigDecimal durationInMonths = BigDecimal.valueOf(projectDurationInMonths);
        BigDecimal monthlyCost = total.divide(durationInMonths, 0, HALF_EVEN);
        BigDecimal remainder = total.subtract(monthlyCost.multiply(durationInMonths)).setScale(0, HALF_EVEN);

        this.firstMonthSpend = monthlyCost.add(remainder);
        this.otherMonthsSpend = monthlyCost;
    }

    public FinanceRowType getCategory() {
        return category;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getFirstMonthSpend() {
        return firstMonthSpend;
    }

    public BigDecimal getOtherMonthsSpend() {
        return otherMonthsSpend;
    }
}
