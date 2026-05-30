package com.education.user.service.impl;

import com.education.resource.service.AIChatService;
import com.education.entity.ChatContext;
import com.education.entity.StudentProfile;
import com.education.user.service.ChatContextService;
import com.education.user.service.ProfileService;
import com.education.user.service.StudentProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//画像业务入口
@Service
public class ProfileServiceImpl implements ProfileService {

    //对话上下文管理（对话存储）
    @Autowired
    private ChatContextService chatContextService;

    //AI交互
    @Autowired
    private AIChatService aiChatService;

    //画像CRUD（画像存储）
    @Autowired
    private StudentProfileService studentProfileService;

    //开启画像构建：生成唯一sessionId（前端用这个ID持续对话）
    @Override
    public String startBuildProfile() {
        return chatContextService.generateSessionId();
    }

    //对话入口：前端传sessionId+用户消息，直接转发给AI服务
    @Override
    public String chat(String sessionId, String userMessage) {
        // 委托AI服务处理对话
        return aiChatService.chatWithAI(sessionId, userMessage);
    }

    //抽取并保存画像：用户输完成后调用
    @Override
    public StudentProfile extractAndSaveProfile(String sessionId) {
        StudentProfile profile = aiChatService.extractProfileFromChat(sessionId);
        studentProfileService.saveOrUpdateProfile(profile);
        return profile;
    }

    // 获取历史对话（给前端展示）
    @Override
    public List<ChatContext> getChatHistory(String sessionId) {
        // 从Redis获取对话历史
        return chatContextService.getChatContextFromRedis(sessionId);
    }

    // 获取当前用户已保存的画像
    @Override
    public StudentProfile getMyProfile() {
        return studentProfileService.getByUserId();
    }

    // 手动更新画像
    @Override
    public void updateProfile(StudentProfile profile) {
        studentProfileService.saveOrUpdateProfile(profile);
    }

    // 重置画像（删除）
    @Override
    public void resetProfile() {
        // 删除当前用户画像
        StudentProfile profile = studentProfileService.getByUserId();
        if (profile != null) {
            studentProfileService.removeById(profile.getId());
        }
    }
}