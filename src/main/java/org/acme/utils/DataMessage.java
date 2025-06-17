package org.acme.utils;

public class DataMessage {
    private String originalRequest;
    private String messageId;
    private String content;
    private String timestamp;

    public DataMessage() {
    }

    public DataMessage(String originalRequest, String messageId, String content, String timestamp) {
        this.originalRequest = originalRequest;
        this.messageId = messageId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
