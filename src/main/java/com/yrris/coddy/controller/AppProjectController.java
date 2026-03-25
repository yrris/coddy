package com.yrris.coddy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrris.coddy.annotation.AuthCheck;
import com.yrris.coddy.common.ApiResponse;
import com.yrris.coddy.common.ResultUtils;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.dto.app.*;
import com.yrris.coddy.model.dto.chat.ChatHistoryQueryRequest;
import com.yrris.coddy.model.dto.common.DeleteRequest;
import com.yrris.coddy.model.vo.AppVO;
import com.yrris.coddy.model.vo.ChatHistoryVO;
import com.yrris.coddy.model.vo.LoginUserVO;
import com.yrris.coddy.model.vo.PageVO;
import com.yrris.coddy.service.AppProjectService;
import com.yrris.coddy.service.ChatHistoryService;
import com.yrris.coddy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppProjectController {

    private final AppProjectService appProjectService;

    private final ChatHistoryService chatHistoryService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    public AppProjectController(
            AppProjectService appProjectService,
            ChatHistoryService chatHistoryService,
            UserService userService,
            ObjectMapper objectMapper
    ) {
        this.appProjectService = appProjectService;
        this.chatHistoryService = chatHistoryService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/add")
    @AuthCheck
    public ApiResponse<Long> addApp(@Valid @RequestBody AppAddRequest request, HttpServletRequest httpServletRequest) {
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(appProjectService.addApp(request, loginUser));
    }

    @PostMapping("/update")
    @AuthCheck
    public ApiResponse<Boolean> updateApp(
            @Valid @RequestBody AppUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(appProjectService.updateApp(request, loginUser));
    }

    @PostMapping("/delete")
    @AuthCheck
    public ApiResponse<Boolean> deleteApp(
            @Valid @RequestBody DeleteRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(appProjectService.deleteApp(request.getId(), loginUser));
    }

    @GetMapping("/get/vo")
    public ApiResponse<AppVO> getAppById(@RequestParam Long id) {
        return ResultUtils.success(appProjectService.getAppVO(id));
    }

    @PostMapping("/my/list/page/vo")
    @AuthCheck
    public ApiResponse<PageVO<AppVO>> listMyApps(
            @RequestBody(required = false) AppQueryRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(appProjectService.listMyApps(request, loginUser));
    }

    @PostMapping("/good/list/page/vo")
    public ApiResponse<PageVO<AppVO>> listGoodApps(@RequestBody(required = false) AppQueryRequest request) {
        return ResultUtils.success(appProjectService.listGoodApps(request));
    }

    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = "ADMIN")
    public ApiResponse<Boolean> deleteAppByAdmin(@Valid @RequestBody DeleteRequest request) {
        return ResultUtils.success(appProjectService.deleteAppByAdmin(request.getId()));
    }

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = "ADMIN")
    public ApiResponse<Boolean> updateAppByAdmin(@Valid @RequestBody AppAdminUpdateRequest request) {
        return ResultUtils.success(appProjectService.updateAppByAdmin(request));
    }

    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = "ADMIN")
    public ApiResponse<PageVO<AppVO>> listAppsByAdmin(@RequestBody(required = false) AppQueryRequest request) {
        return ResultUtils.success(appProjectService.listAppsByAdmin(request));
    }

    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = "ADMIN")
    public ApiResponse<AppVO> getAppByIdByAdmin(@RequestParam Long id) {
        return ResultUtils.success(appProjectService.getAppVOForAdmin(id));
    }

    @PostMapping("/admin/chat/list/page/vo")
    @AuthCheck(mustRole = "ADMIN")
    public ApiResponse<PageVO<ChatHistoryVO>> listChatHistoryByAdmin(
            @RequestBody(required = false) ChatHistoryQueryRequest request
    ) {
        return ResultUtils.success(chatHistoryService.listByAdmin(request));
    }

    @PostMapping("/admin/chat/delete")
    @AuthCheck(mustRole = "ADMIN")
    public ApiResponse<Boolean> deleteChatHistoryByAdmin(@Valid @RequestBody DeleteRequest request) {
        return ResultUtils.success(chatHistoryService.deleteByAdmin(request.getId()));
    }

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @AuthCheck
    public Flux<ServerSentEvent<String>> chatToGenCode(
            @RequestParam Long appId,
            @RequestParam String message,
            HttpServletRequest httpServletRequest
    ) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid app id");
        }
        if (!StringUtils.hasText(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Message is required");
        }

        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        Flux<String> contentFlux = appProjectService.chatToGenCode(appId, message, loginUser);
        return contentFlux
                .map(chunk -> {
                    try {
                        String payload = objectMapper.writeValueAsString(Map.of("d", chunk));
                        return ServerSentEvent.<String>builder().data(payload).build();
                    } catch (JsonProcessingException e) {
                        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to encode stream chunk");
                    }
                })
                .concatWith(Mono.just(ServerSentEvent.<String>builder().event("done").data("").build()));
    }

    @GetMapping("/{appId}/chat/history")
    @AuthCheck
    public ApiResponse<List<ChatHistoryVO>> getChatHistory(
            @PathVariable Long appId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid app id");
        }
        List<ChatHistoryVO> history = chatHistoryService.listByProject(appId, cursorId, pageSize);
        return ResultUtils.success(history);
    }

    @PostMapping("/deploy")
    @AuthCheck
    public ApiResponse<String> deployApp(
            @Valid @RequestBody AppDeployRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(appProjectService.deployApp(request.getAppId(), loginUser));
    }
}
