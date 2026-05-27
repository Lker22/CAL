package com.education.user.controller;

import com.education.result.Result;
import com.education.user.service.UserService;
import com.education.vo.LoginVO;
import com.education.vo.PasswordVO;
import com.education.vo.RegisterVO;
import com.education.vo.UserInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @valid 判空处理校验vo
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterVO vo){
        log.info("用户注册：{}", vo.getUsername());
        return userService.register(vo);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginVO vo){
        log.info("用户登录：{}", vo.getUsername());
        return userService.login(vo);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getCurrentUserInfo() {
        return userService.getCurrentUserInfo();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody UserInfoVO userInfoVO) {
        return userService.updateUserInfo(userInfoVO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody PasswordVO passwordVO) {
        return userService.updatePassword(passwordVO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<?> logout() {
        return userService.logout();
    }



}