package com.education.resource.service;

import com.education.entity.StudentProfile;

public interface AIChatService {
    String chatWithAI(String sessionId, String userMessage);
    StudentProfile extractProfileFromChat(String sessionId);
}