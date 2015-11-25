package com.worth.ifs.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.form.domain.FormInput;
import com.worth.ifs.form.domain.FormInputResponse;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.User;
import org.junit.Test;
import org.springframework.http.MediaType;

import static com.worth.ifs.BuilderAmendFunctions.id;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.form.builder.FormInputBuilder.newFormInput;
import static com.worth.ifs.form.builder.FormInputResponseBuilder.newFormInputResponse;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.builder.UserBuilder.newUser;
import static com.worth.ifs.user.domain.UserRoleType.APPLICANT;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FormInputResponseControllerTest extends BaseControllerMockMVCTest<FormInputResponseController> {

    @Override
    protected FormInputResponseController supplyControllerUnderTest() {
        return new FormInputResponseController();
    }

    @Test
    public void testFindResponsesByApplication() throws Exception {

        ProcessRole[] consortiumsProcessRoles = newProcessRole().buildArray(2, ProcessRole.class);
        Application application = newApplication().withId(123L).withProcessRoles(consortiumsProcessRoles).build();
        FormInput formInput = newFormInput().build();

        FormInputResponse[] consortiumResponses = newFormInputResponse().withFormInputs(formInput).buildArray(2, FormInputResponse.class);

        when(applicationRepositoryMock.findOne(123L)).thenReturn(application);
        when(formInputResponseRepository.findByUpdatedById(consortiumsProcessRoles[0].getId())).thenReturn(asList(consortiumResponses[0]));
        when(formInputResponseRepository.findByUpdatedById(consortiumsProcessRoles[1].getId())).thenReturn(asList(consortiumResponses[1]));

        mockMvc.perform(post("/forminputresponse/findResponsesByApplication/123")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writeValueAsString(consortiumResponses)));
    }

    @Test
    public void testSaveQuestionResponse() throws Exception {

        Application application = newApplication().withId(456L).build();
        User user = newUser().with(id(123L)).build();
        ProcessRole applicantProcessRole = newProcessRole().withRole(newRole().withType(APPLICANT).build()).build();

        FormInput formInput = newFormInput().with(id(789L)).build();

        when(formInputRepository.findOne(789L)).thenReturn(formInput);
        when(userRepositoryMock.findOne(123L)).thenReturn(user);
        when(applicationRepositoryMock.findOne(456L)).thenReturn(application);
        when(processRoleRepositoryMock.findByUserAndApplication(user, application)).thenReturn(asList(applicantProcessRole));

        String contentString = "{\"userId\":123,\"applicationId\":456,\"formInputId\":789,\"value\":\"\"}";
        mockMvc.perform(post("/forminputresponse/saveQuestionResponse/")
                    .content(contentString)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}