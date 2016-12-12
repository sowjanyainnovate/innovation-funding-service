package com.worth.ifs.project.status.controller;

import com.worth.ifs.project.constant.ProjectActivityStates;

/**
 * Class for checking the access permissions
 */
public class ProjectStatusPermission {
    private Boolean canAccessCompaniesHouse;
    private Boolean canAccessProjectDetails;
    private Boolean canAccessMonitoringOfficer;
    private Boolean canAccessBankDetails;
    private Boolean canAccessFinanceChecks;
    private Boolean canAccessSpendProfile;
    private Boolean canAccessOtherDocuments;
    private Boolean canAccessGrantOfferLetter;
    private Boolean canAccessGrantOfferLetterSend;
    private ProjectActivityStates grantOfferLetterActivityState;

    public ProjectStatusPermission(Boolean canAccessCompaniesHouse, Boolean canAccessProjectDetails,
                                   Boolean canAccessMonitoringOfficer, Boolean canAccessBankDetails,
                                   Boolean canAccessFinanceChecks, Boolean canAccessSpendProfile,
                                   Boolean canAccessOtherDocuments, Boolean canAccessGrantOfferLetter,
                                   Boolean canAccessGrantOfferLetterSend, ProjectActivityStates grantOfferLetterActivityState) {
        this.canAccessCompaniesHouse = canAccessCompaniesHouse;
        this.canAccessProjectDetails = canAccessProjectDetails;
        this.canAccessMonitoringOfficer = canAccessMonitoringOfficer;
        this.canAccessBankDetails = canAccessBankDetails;
        this.canAccessFinanceChecks = canAccessFinanceChecks;
        this.canAccessSpendProfile = canAccessSpendProfile;
        this.canAccessOtherDocuments = canAccessOtherDocuments;
        this.canAccessGrantOfferLetter = canAccessGrantOfferLetter;
        this.canAccessGrantOfferLetterSend = canAccessGrantOfferLetterSend;
        this.grantOfferLetterActivityState = grantOfferLetterActivityState;
    }

    public Boolean getCanAccessCompaniesHouse() {
        return canAccessCompaniesHouse;
    }

    public Boolean getCanAccessProjectDetails() {
        return canAccessProjectDetails;
    }

    public Boolean getCanAccessMonitoringOfficer() {
        return canAccessMonitoringOfficer;
    }

    public Boolean getCanAccessBankDetails() {
        return canAccessBankDetails;
    }

    public Boolean getCanAccessFinanceChecks() {
        return canAccessFinanceChecks;
    }

    public Boolean getCanAccessSpendProfile() {
        return canAccessSpendProfile;
    }

    public Boolean getCanAccessOtherDocuments() {
        return canAccessOtherDocuments;
    }

    public Boolean getCanAccessGrantOfferLetter() { return canAccessGrantOfferLetter; }

    public Boolean getCanAccessGrantOfferLetterSend() { return canAccessGrantOfferLetterSend; }

    public ProjectActivityStates getGrantOfferLetterActivityStatus() { return grantOfferLetterActivityState; }
}
