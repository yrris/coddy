package com.yrris.coddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginCurrentLogoutFlowShouldWork() throws Exception {
        String email = "user_" + UUID.randomUUID() + "@example.com";
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "password", "Passw0rd!",
                "checkPassword", "Passw0rd!",
                "displayName", "tester"
        );

        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Map<String, Object> loginBody = Map.of(
                "email", email,
                "password", "Passw0rd!"
        );

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.email").value(email))
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(sessionCookie);

        mockMvc.perform(get("/user/current").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(post("/user/logout").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/user/current").cookie(sessionCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void duplicateRegisterShouldReturnConflict() throws Exception {
        String email = "user_" + UUID.randomUUID() + "@example.com";
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "password", "Passw0rd!",
                "checkPassword", "Passw0rd!",
                "displayName", "tester"
        );

        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900));
    }

    @Test
    void unauthenticatedCurrentUserShouldReturn401() throws Exception {
        mockMvc.perform(get("/user/current"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void unauthenticatedAdminPageShouldReturn401() throws Exception {
        mockMvc.perform(get("/user/admin/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void nonAdminAccessAdminPageShouldReturnForbidden() throws Exception {
        String email = "user_" + UUID.randomUUID() + "@example.com";
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "password", "Passw0rd!",
                "checkPassword", "Passw0rd!",
                "displayName", "tester"
        );

        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Map<String, Object> loginBody = Map.of(
                "email", email,
                "password", "Passw0rd!"
        );

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(sessionCookie);

        mockMvc.perform(get("/user/admin/page").cookie(sessionCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300));
    }
}
