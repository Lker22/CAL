package com.education.user.controller;

import com.education.entity.StudentProfile;
import com.education.result.Result;
import com.education.user.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    // 1. 启动画像构建：生成sessionId
    @PostMapping("/build/start")
    public Result start() {
        String sessionId = profileService.startBuildProfile();
        return Result.success(sessionId);
    }

    // 2. 多轮对话：接收sessionId和用户消息，返回AI回复（完成时抽取画像）
    @PostMapping("/build/chat")
    public Result chat(@RequestBody Map<String, String> params) {
        String sessionId = params.get("context");
        String message = params.get("message");

        String aiReply = profileService.chat(sessionId, message);
        Map<String, Object> map = new HashMap<>();
        map.put("aiReply", aiReply);

        // 触发画像抽取条件：AI回复含"信息收集完成" 或 用户发"完成"
        if (aiReply.contains("信息收集完成") || message.contains("完成")) {
            StudentProfile profile = profileService.extractAndSaveProfile(sessionId);
            map.put("profile", profile);// 返回抽取的画像
        }
        return Result.success(map);
    }

    // 3. 获取对话历史
    @GetMapping("/chat/history")
    public Result history(@RequestParam String context) {
        return Result.success(profileService.getChatHistory(context));
    }

    // 4. 查画像
    @GetMapping
    public Result getProfile() {
        return Result.success(profileService.getMyProfile());
    }

    // 5. 改画像
    @PutMapping
    public Result updateProfile(@RequestBody StudentProfile profile) {
        profileService.updateProfile(profile);
        return Result.success(null);
    }

    //6.重置画像
    @PostMapping("/reset")
    public Result reset() {
        profileService.resetProfile();
        return Result.success(null);
    }
}