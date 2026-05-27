package com.education.user.service.impl;

import com.education.ai.service.AIChatService;
import com.education.entity.ChatContext;
import com.education.entity.StudentProfile;
import com.education.user.service.ChatContextService;
import com.education.user.service.ProfileService;
import com.education.user.service.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    //对话上下文管理
    @Autowired
    private ChatContextService chatContextService;

    //AI交互
    @Autowired
    private AIChatService aiChatService;

    //画像CRUD
    @Autowired
    private StudentProfileService studentProfileService;

    @Override
    public String startBuildProfile() {
        // 调用ChatContextService生成唯一sessionId
        return chatContextService.generateSessionId();
    }

    @Override
    public String chat(String sessionId, String userMessage) {
        // 委托AI服务处理对话
        return aiChatService.chatWithAI(sessionId, userMessage);
    }

    @Override
    public StudentProfile extractAndSaveProfile(String sessionId) {
        // 1. AI抽取画像 → 2. 保存/更新画像
        StudentProfile profile = aiChatService.extractProfileFromChat(sessionId);
        studentProfileService.saveOrUpdateProfile(profile);
        return profile;
    }

    @Override
    public List<ChatContext> getChatHistory(String sessionId) {
        // 从Redis获取对话历史
        return chatContextService.getChatContextFromRedis(sessionId);
    }

    @Override
    public StudentProfile getMyProfile() {
        // 获取当前用户画像
        return studentProfileService.getByUserId();
    }

    @Override
    public void updateProfile(StudentProfile profile) {
        studentProfileService.saveOrUpdateProfile(profile);
    }

    @Override
    public void resetProfile() {
        // 删除当前用户画像
        StudentProfile profile = studentProfileService.getByUserId();
        if (profile != null) {
            studentProfileService.removeById(profile.getId());
        }
    }
}