package com.yrris.coddy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrris.coddy.model.entity.AppUser;
import com.yrris.coddy.model.enums.UserRoleEnum;
import com.yrris.coddy.repository.AppUserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void userCrudFlowShouldWork() throws Exception {
        Cookie userCookie = registerAndLogin(false);

        Long appId = createApp(userCookie, "Build a task tracking app for internship schedule");

        mockMvc.perform(get("/app/get/vo").param("id", String.valueOf(appId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(appId));

        mockMvc.perform(post("/app/my/list/page/vo")
                        .cookie(userCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "pageNum", 1,
                                "pageSize", 10,
                                "sortField", "createTime",
                                "sortOrder", "desc"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").exists());

        mockMvc.perform(post("/app/update")
                        .cookie(userCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "id", appId,
                                "appName", "Updated Internship Tracker"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/app/delete")
                        .cookie(userCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", appId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/app/get/vo").param("id", String.valueOf(appId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void adminEndpointsShouldManageAllApps() throws Exception {
        Cookie userCookie = registerAndLogin(false);
        Long appId = createApp(userCookie, "Build a visual portfolio site");

        Cookie adminCookie = registerAndLogin(true);

        mockMvc.perform(post("/app/admin/update")
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "id", appId,
                                "appName", "Featured Portfolio",
                                "cover", "https://picsum.photos/seed/coddy/640/360",
                                "priority", 99
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/app/good/list/page/vo")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "pageNum", 1,
                                "pageSize", 10,
                                "appName", "Featured"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").exists());

        mockMvc.perform(post("/app/admin/list/page/vo")
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "pageNum", 1,
                                "pageSize", 20,
                                "appName", "Featured",
                                "priority", 99
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(appId));

        mockMvc.perform(get("/app/admin/get/vo")
                        .cookie(adminCookie)
                        .param("id", String.valueOf(appId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.appName").value("Featured Portfolio"));

        mockMvc.perform(post("/app/admin/delete")
                        .cookie(adminCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("id", appId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deployShouldReturnPublicUrl() throws Exception {
        Cookie userCookie = registerAndLogin(false);
        Long appId = createApp(userCookie, "Build a deployable app");

        Path sourceDir = Path.of(System.getProperty("user.dir"), "tmp", "code_output", "html_multi_" + appId);
        Files.createDirectories(sourceDir);
        Files.writeString(
                sourceDir.resolve("index.html"),
                "<!doctype html><html><body><h1>deploy</h1></body></html>",
                StandardCharsets.UTF_8
        );

        mockMvc.perform(post("/app/deploy")
                        .cookie(userCookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("appId", appId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isString());
    }

    private Long createApp(Cookie cookie, String initPrompt) throws Exception {
        MvcResult addResult = mockMvc.perform(post("/app/add")
                        .cookie(cookie)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "initPrompt", initPrompt,
                                "codeGenType", "HTML_MULTI"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(addResult.getResponse().getContentAsString());
        return jsonNode.path("data").asLong();
    }

    private Cookie registerAndLogin(boolean adminRole) throws Exception {
        String email = "app_user_" + UUID.randomUUID() + "@example.com";
        Map<String, Object> registerBody = Map.of(
                "email", email,
                "password", "Passw0rd!",
                "checkPassword", "Passw0rd!",
                "displayName", adminRole ? "admin-user" : "normal-user"
        );

        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        if (adminRole) {
            Optional<AppUser> userOptional = appUserRepository.findByEmailAndIsDeletedFalse(email);
            assertTrue(userOptional.isPresent());
            AppUser user = userOptional.get();
            user.setUserRole(UserRoleEnum.ADMIN);
            appUserRepository.save(user);
        }

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "Passw0rd!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        assertNotNull(sessionCookie);
        return sessionCookie;
    }
}
