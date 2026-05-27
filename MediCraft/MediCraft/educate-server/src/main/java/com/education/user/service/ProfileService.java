package com.education.user.service;

import com.education.entity.ChatContext;
import com.education.entity.StudentProfile;

import java.util.List;

public interface ProfileService {

    /**
     * 开始构建画像 - 创建会话
     */
    String startBuildProfile();

    /**
     * 多轮对话聊天
     */
    String chat(String sessionId, String userMessage);

    /**
     * 从对话中抽取并保存画像
     */
    StudentProfile extractAndSaveProfile(String sessionId);

    /**
     * 获取对话历史
     */
    List<ChatContext> getChatHistory(String sessionId);

    /**
     * 获取当前用户画像
     */
    StudentProfile getMyProfile();

    /**
     * 更新画像
     */
    void updateProfile(StudentProfile profile);

    /**
     * 重置画像
     */
    void resetProfile();
}