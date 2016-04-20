package com.worth.ifs.user.controller;

import java.util.ArrayList;
import java.util.List;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.commons.error.Error;
import com.worth.ifs.token.domain.Token;
import com.worth.ifs.user.domain.User;
import com.worth.ifs.user.resource.UserResource;

import org.junit.Test;

import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.service.ServiceResult.serviceFailure;
import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.token.domain.TokenType.VERIFY_EMAIL_ADDRESS;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static com.worth.ifs.user.controller.UserController.URL_PASSWORD_RESET;
import static com.worth.ifs.user.controller.UserController.URL_VERIFY_EMAIL;
import static java.util.Optional.of;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends BaseControllerMockMVCTest<UserController> {

    @Override
    protected UserController supplyControllerUnderTest() {
        return new UserController();
    }

    @Test
    public void userControllerShouldReturnAllUsers() throws Exception {
        UserResource testUser1 = newUserResource().withId(1L).withFirstName("test").withLastName("User1").withEmail("email1@email.nl").build();
        UserResource testUser2 = newUserResource().withId(2L).withFirstName("test").withLastName("User2").withEmail("email2@email.nl").build();
        UserResource testUser3 = newUserResource().withId(3L).withFirstName("test").withLastName("User3").withEmail("email3@email.nl").build();

        List<UserResource> users = new ArrayList<>();
        users.add(testUser1);
        users.add(testUser2);
        users.add(testUser3);

        when(userServiceMock.findAll()).thenReturn(serviceSuccess(users));
        mockMvc.perform(get("/user/findAll/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0]id", is((Number) testUser1.getId().intValue())))
                .andExpect(jsonPath("[0]firstName", is(testUser1.getFirstName())))
                .andExpect(jsonPath("[0]lastName", is(testUser1.getLastName())))
                .andExpect(jsonPath("[0]imageUrl", is(testUser1.getImageUrl())))
                .andExpect(jsonPath("[0]uid", is(testUser1.getUid())))
                .andExpect(jsonPath("[1]id", is((Number) testUser2.getId().intValue())))
                .andExpect(jsonPath("[1]firstName", is(testUser2.getFirstName())))
                .andExpect(jsonPath("[1]lastName", is(testUser2.getLastName())))
                .andExpect(jsonPath("[1]imageUrl", is(testUser2.getImageUrl())))
                .andExpect(jsonPath("[1]uid", is(testUser2.getUid())))
                .andExpect(jsonPath("[2]id", is((Number) testUser3.getId().intValue())))
                .andExpect(jsonPath("[2]firstName", is(testUser3.getFirstName())))
                .andExpect(jsonPath("[2]lastName", is(testUser3.getLastName())))
                .andExpect(jsonPath("[2]imageUrl", is(testUser3.getImageUrl())))
                .andExpect(jsonPath("[2]uid", is(testUser3.getUid())))
                .andDo(document("user/get-all-users"));
    }

    @Test
    public void userControllerShouldReturnUserById() throws Exception {
        UserResource testUser1 = newUserResource().withId(1L).withFirstName("test").withLastName("User1").withEmail("email1@email.nl").build();

        when(userServiceMock.getUserById(testUser1.getId())).thenReturn(serviceSuccess(testUser1));
        mockMvc.perform(get("/user/id/" + testUser1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((Number) testUser1.getId().intValue())))
                .andExpect(jsonPath("firstName", is(testUser1.getFirstName())))
                .andExpect(jsonPath("lastName", is(testUser1.getLastName())))
                .andExpect(jsonPath("imageUrl", is(testUser1.getImageUrl())))
                .andExpect(jsonPath("uid", is(testUser1.getUid())))
                .andDo(document("user/get-user"));
    }

    @Test
    public void updatePassword() throws Exception {
        final String password = "Passw0rd";
        final String hash = "bf5b6392-1e08-4acc-b667-f0a16d6744de";
        when(userServiceMock.changePassword(hash, password)).thenReturn(serviceSuccess(null));
        mockMvc.perform(post("/user/" + URL_PASSWORD_RESET + "/{hash}", hash).content(password))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("user/update-password",
                    pathParameters(
                        parameterWithName("hash").description("The hash to validate the legitimacy of the request")
                    )
                ));
    }


    @Test
    public void verifyEmail() throws Exception {
        final String hash = "8eda60ad3441ee883cc95417e2abaa036c308dd9eb19468fcc8597fb4cb167c32a7e5daf5e237385";
        final Long userId = 1L;
        final Token token = new Token(VERIFY_EMAIL_ADDRESS, User.class.getName(), userId, hash, null);
        when(tokenServiceMock.getTokenByHash(hash)).thenReturn(of(token));
        when(registrationServiceMock.activateUser(1L)).thenReturn(serviceSuccess());
        mockMvc.perform(get("/user/" + URL_VERIFY_EMAIL + "/{hash}", hash))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(document("user/verify-email",
                                pathParameters(
                                        parameterWithName("hash").description("The hash to validate the legitimacy of the request")
                                ))
                );
    }

    @Test
    public void updatePasswordTokenNotFound() throws Exception {
        final String password = "Passw0rd";
        final String hash = "bf5b6392-1e08-4acc-b667-f0a16d6744de";
        final Error error = notFoundError(Token.class, hash);
        when(userServiceMock.changePassword(hash, password)).thenReturn(serviceFailure(error));
        mockMvc.perform(post("/user/" + URL_PASSWORD_RESET + "/" + hash).content(password))
                .andExpect(status().isNotFound())
                .andExpect(contentError(error))
                .andDo(document("user/update-password-token-not-found"));
    }

    @Test
    public void userControllerShouldReturnUserByUid() throws Exception {
        UserResource testUser1 = newUserResource().withUID("aebr34-ab345g-234gae-agewg").withId(1L).withFirstName("test").withLastName("User1").withEmail("email1@email.nl").build();

        when(userServiceMock.getUserResourceByUid(testUser1.getUid())).thenReturn(serviceSuccess(testUser1));

        mockMvc.perform(get("/user/uid/" + testUser1.getUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is((Number) testUser1.getId().intValue())))
                .andExpect(jsonPath("firstName", is(testUser1.getFirstName())))
                .andExpect(jsonPath("lastName", is(testUser1.getLastName())))
                .andExpect(jsonPath("imageUrl", is(testUser1.getImageUrl())))
                .andExpect(jsonPath("uid", is(testUser1.getUid())))
                .andDo(document("user/get-user-by-token"));
    }

    @Test
    public void userControllerShouldReturnListOfSingleUserWhenFoundByEmail() throws Exception {
        User user = new User();
        user.setEmail("testemail@email.email");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setPhoneNumber("testPhoneNumber");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setTitle("Mr");

        when(userServiceMock.findByEmail(user.getEmail())).thenReturn(serviceFailure(notFoundError(User.class, user.getEmail())));

        mockMvc.perform(get("/user/findByEmail/" + user.getEmail() + "/", "json")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userControllerShouldReturnEmptyListWhenNoUserIsFoundByEmail() throws Exception {

        String email = "testemail@email.com";

        when(userServiceMock.findByEmail(email)).thenReturn(serviceFailure(notFoundError(User.class, email)));

        mockMvc.perform(get("/user/findByEmail/" + email + "/", "json")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}