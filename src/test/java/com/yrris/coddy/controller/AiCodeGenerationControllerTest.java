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
class AiCodeGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateShouldRequireLogin() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "prompt", "Build a modern portfolio homepage",
                "codeGenType", "HTML_SINGLE"
        );

        mockMvc.perform(post("/ai/codegen/generate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void generateShouldReturnFilesWhenLoggedIn() throws Exception {
        Cookie sessionCookie = loginAsNormalUser();

        Map<String, Object> requestBody = Map.of(
                "prompt", "Build a modern portfolio homepage",
                "codeGenType", "HTML_SINGLE"
        );

        mockMvc.perform(post("/ai/codegen/generate")
                        .cookie(sessionCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.codeGenType").value("HTML_SINGLE"))
                .andExpect(jsonPath("$.data.files['index.html']").exists())
                .andExpect(jsonPath("$.data.outputDir").isString());
    }

    @Test
    void streamShouldReturnForbiddenForInvalidMode() throws Exception {
        Cookie sessionCookie = loginAsNormalUser();

        Map<String, Object> requestBody = Map.of(
                "prompt", "Build a startup homepage",
                "codeGenType", "INVALID_MODE"
        );

        mockMvc.perform(post("/ai/codegen/stream")
                        .cookie(sessionCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }

    private Cookie loginAsNormalUser() throws Exception {
        String email = "user_" + UUID.randomUUID() + "@example.com";
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "password", "Passw0rd!",
                "checkPassword", "Passw0rd!",
                "displayName", "generator-tester"
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
        return sessionCookie;
    }
}
