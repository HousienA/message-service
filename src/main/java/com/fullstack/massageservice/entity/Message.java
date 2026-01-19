package com.fullstack.massageservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    private LocalDateTime sentAt;
    private boolean isRead;

    private String subject;

    // VIKTIGT: Ingen @ManyToOne till Patient längre!
    // Vi sparar bara ID:t som en siffra.
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "practitioner_id")
    private Long practitionerId;

    // Vem skickade meddelandet? "PATIENT" eller "PRACTITIONER"
    @Column(nullable = false)
    private String senderType;

    public Message() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getPractitionerId() { return practitionerId; }
    public void setPractitionerId(Long practitionerId) { this.practitionerId = practitionerId; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
