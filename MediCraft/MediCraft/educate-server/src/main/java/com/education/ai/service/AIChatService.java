package com.education.ai.service;

import com.education.entity.StudentProfile;

public interface AIChatService {
    String chatWithAI(String sessionId, String userMessage);
    StudentProfile extractProfileFromChat(String sessionId);
}