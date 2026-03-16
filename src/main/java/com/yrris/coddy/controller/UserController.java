package com.yrris.coddy.controller;

import com.yrris.coddy.annotation.AuthCheck;
import com.yrris.coddy.common.ApiResponse;
import com.yrris.coddy.common.ResultUtils;
import com.yrris.coddy.model.dto.user.UserLoginRequest;
import com.yrris.coddy.model.dto.user.UserRegisterRequest;
import com.yrris.coddy.model.vo.LoginUserVO;
import com.yrris.coddy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResultUtils.success(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResultUtils.success(userService.login(request, httpServletRequest));
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.logout(httpServletRequest));
    }

    @AuthCheck
    @GetMapping("/current")
    public ApiResponse<LoginUserVO> getCurrentLoginUser(HttpServletRequest httpServletRequest) {
        return ResultUtils.success(userService.getLoginUser(httpServletRequest));
    }

    @AuthCheck(mustRole = "ADMIN")
    @GetMapping("/admin/page")
    public ApiResponse<Page<LoginUserVO>> listUsers(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ResultUtils.success(userService.listUsers(current, pageSize));
    }
}
