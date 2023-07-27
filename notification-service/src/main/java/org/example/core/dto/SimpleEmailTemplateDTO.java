package org.example.core.dto;

import java.net.URI;

public class SimpleEmailTemplateDTO {

    private String to;

    private String subject;

    private String text;

    private URI replyTo;


    public SimpleEmailTemplateDTO() {
    }

    public SimpleEmailTemplateDTO(String to, String subject, String text, URI replyTo) {
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.replyTo = replyTo;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public URI getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(URI replyTo) {
        this.replyTo = replyTo;
    }
}
