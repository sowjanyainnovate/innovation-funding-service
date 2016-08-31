package com.worth.ifs.competitionsetup.service;

import com.worth.ifs.application.service.MilestoneService;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.competition.resource.MilestoneResource;
import com.worth.ifs.competition.resource.MilestoneType;
import com.worth.ifs.competitionsetup.model.MilestoneEntry;
import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CompetitionSetupMilestoneServiceImpl implements CompetitionSetupMilestoneService {

    @Autowired
    MilestoneService milestoneService;

    @Override
    public List<MilestoneResource> createMilestonesForCompetition(Long competitionId) {
        List<MilestoneResource> newMilestones = new ArrayList<>();
        Stream.of(MilestoneType.values()).forEach(type -> {
            MilestoneResource newMilestone = milestoneService.create(type, competitionId);
            newMilestone.setType(type);
            newMilestones.add(newMilestone);
        });
        return newMilestones;
    }

    @Override
    public List<Error> updateMilestonesForCompetition(List<MilestoneResource> milestones, LinkedMap<String, MilestoneEntry> milestoneEntries, Long competitionId) {
        List<MilestoneResource> updatedMilestones = new ArrayList();

        milestones.forEach(milestoneResource -> {
            MilestoneEntry milestoneWithUpdate = milestoneEntries.getOrDefault(milestoneResource.getType().name(), null);

            if(milestoneWithUpdate != null) {
                LocalDateTime temp = getMilestoneDate(milestoneWithUpdate.getDay(), milestoneWithUpdate.getMonth(), milestoneWithUpdate.getYear());
                if (temp != null) {
                    milestoneResource.setDate(temp);
                    updatedMilestones.add(milestoneResource);
                }
            }
        });

        return milestoneService.update(updatedMilestones, competitionId);
    }

    private LocalDateTime getMilestoneDate(Integer day, Integer month, Integer year){
        if (day != null && month != null && year != null){
            return LocalDateTime.of(year, month, day, 0, 0);
        } else {
            return null;
        }
    }

    @Override
    public List<Error> validateMilestoneDates(LinkedMap<String, MilestoneEntry> milestonesFormEntries) {
        List<Error> errors =  new ArrayList<>();
        milestonesFormEntries.values().forEach(milestone -> {

            Integer day = milestone.getDay();
            Integer month = milestone.getMonth();
            Integer year = milestone.getYear();

            if(day == null || month == null || year == null || !isMilestoneDateValid(day, month, year)) {
                if(errors.isEmpty()) {
                    errors.add(new Error("error.milestone.invalid", "Please enter the valid date(s)", HttpStatus.BAD_REQUEST));
                }
            }
        });
        return errors;
    }

    private Boolean isMilestoneDateValid(Integer day, Integer month, Integer year) {
        try{
            LocalDateTime.of(year, month, day, 0,0);
            return true;
        }
        catch(DateTimeException dte){
            return false;
        }
    }
}