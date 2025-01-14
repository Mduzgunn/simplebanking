package com.eteration.simplebanking.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LogMessage implements Serializable {
    private String level;
    private String message;
    private String className;
    private String methodName;
    private LocalDateTime timestamp;
    private String stackTrace;

    public LogMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public LogMessage(String level, String message, String className, String methodName, String stackTrace) {
        this();
        this.level = level;
        this.message = message;
        this.className = className;
        this.methodName = methodName;
        this.stackTrace = stackTrace;
    }

    // Getters and Setters
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
} 
