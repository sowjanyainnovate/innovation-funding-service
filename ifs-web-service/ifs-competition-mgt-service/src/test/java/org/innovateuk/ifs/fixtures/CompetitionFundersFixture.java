package org.innovateuk.ifs.fixtures;

import org.innovateuk.ifs.competition.resource.CompetitionFunderResource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CompetitionFundersFixture {

    public static List<CompetitionFunderResource> getTestCoFunders() {
        List<CompetitionFunderResource> returnList = new ArrayList<>();
        CompetitionFunderResource coFunder1 = new CompetitionFunderResource();
        coFunder1.setId(1L);
        coFunder1.setFunder("coFunder1");
        coFunder1.setFunderBudget(BigInteger.valueOf(1));
        coFunder1.setCoFunder(true);
        coFunder1.setCompetitionId(1L);
        returnList.add(coFunder1);

        CompetitionFunderResource coFunder2 = new CompetitionFunderResource();
        coFunder2.setId(2L);
        coFunder2.setFunder("coFunder2");
        coFunder2.setFunderBudget(BigInteger.valueOf(2));
        coFunder2.setCoFunder(true);
        coFunder2.setCompetitionId(1L);
        returnList.add(coFunder2);

        return returnList;
    }
}
