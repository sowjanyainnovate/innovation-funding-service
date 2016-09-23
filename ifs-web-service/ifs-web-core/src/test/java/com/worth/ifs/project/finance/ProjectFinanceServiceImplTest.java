package com.worth.ifs.project.finance;

import com.worth.ifs.application.service.ApplicationService;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.finance.service.ProjectFinanceRestService;
import com.worth.ifs.project.resource.SpendProfileTableResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static com.worth.ifs.commons.rest.RestResult.restSuccess;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectFinanceServiceImplTest {

    @InjectMocks
    private ProjectFinanceServiceImpl service;

    @Mock
    private ProjectFinanceRestService projectFinanceRestService;

    @Mock
    private ApplicationService applicationService;

    @Test
    public void saveSpendProfile() {

        Long projectId = 1L;
        Long organisationId = 1L;

        SpendProfileTableResource table = new SpendProfileTableResource();

        when(projectFinanceRestService.saveSpendProfile(projectId, organisationId, table)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.saveSpendProfile(projectId, organisationId, table);

        assertTrue(result.isSuccess());

    }

    @Test
    public void markSpendProfile() {

        Long projectId = 1L;
        Long organisationId = 1L;
        Boolean complete = true;

        when(projectFinanceRestService.markSpendProfile(projectId, organisationId, complete)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.markSpendProfile(projectId, organisationId, complete);

        assertTrue(result.isSuccess());

    }

}