package com.eteration.simplebanking.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T object;

    public ApiResponse() {
    }

    public ApiResponse(Boolean success, String message, T object) {
        this.success = success;
        this.message = message;
        this.object = object;
    }

    public ResponseEntity<?> getResponseJson() {
        return new ResponseEntity<>(this, HttpStatus.OK);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
} 