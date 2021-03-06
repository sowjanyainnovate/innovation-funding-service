package org.innovateuk.ifs.finance.handler.item;

import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.FinanceRow;
import org.innovateuk.ifs.finance.domain.ProjectFinanceRow;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.OtherCost;

/**
 * Handles the other costs, i.e. converts the costs to be stored into the database
 * or for sending it over.
 */
public class OtherCostHandler extends FinanceRowHandler<OtherCost> {
    public static final String COST_KEY = "other-cost";

    @Override
    public ApplicationFinanceRow toCost(OtherCost otherCost) {
        return new ApplicationFinanceRow(otherCost.getId(), COST_KEY , "", otherCost.getDescription(), 0, otherCost.getCost(), null, null);
    }

    @Override
    public ProjectFinanceRow toProjectCost(OtherCost otherCost) {
        return new ProjectFinanceRow(otherCost.getId(), COST_KEY , "", otherCost.getDescription(), 0, otherCost.getCost(), null, null);
    }

    @Override
    public FinanceRowItem toCostItem(ApplicationFinanceRow cost) {
        return buildRowItem(cost);
    }

    @Override
    public FinanceRowItem toCostItem(ProjectFinanceRow cost) {
        return buildRowItem(cost);
    }

    private FinanceRowItem buildRowItem(FinanceRow cost){
        return new OtherCost(cost.getId(),cost.getDescription(), cost.getCost());
    }
}
