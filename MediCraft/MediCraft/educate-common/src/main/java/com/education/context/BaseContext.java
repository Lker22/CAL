package com.education.context;

public class BaseContext {
    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();

    public static void setCurrentId(Long userId) {
        currentId.set(userId);
    }

    public static Long getCurrentId() {
        return currentId.get();
    }

    public static void removeCurrentId() {
        currentId.remove();
    }
}