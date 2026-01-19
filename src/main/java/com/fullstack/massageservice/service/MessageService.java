package com.fullstack.massageservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // NY
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // NY
import com.fullstack.massageservice.entity.Message;
import com.fullstack.massageservice.repository.MessageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // KONFIGURERA OBJECTMAPPER
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Fixar datum-felet
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public MessageService(MessageRepository messageRepository,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.messageRepository = messageRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void queueMessage(Message message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            System.out.println("Sending JSON to Kafka: " + jsonMessage);
            kafkaTemplate.send("messages", jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Kasta felet vidare så vi ser 500 error i frontend om det skiter sig,
            // istället för att svälja det och returnera 202 Accepted.
            throw new RuntimeException("Kunde inte serialisera meddelande", e);
        }
    }

    @KafkaListener(topics = "messages", groupId = "messaging-group")
    public void consumeMessage(String messageJson) {
        try {
            System.out.println("CONSUMER: Received raw JSON: " + messageJson);

            Message message = objectMapper.readValue(messageJson, Message.class);

            // Sätt alltid server-tid
            message.setSentAt(LocalDateTime.now());

            if (message.getSenderType() == null || message.getSenderType().isEmpty()) {
                message.setSenderType("UNKNOWN");
            }

            if (message.getSubject() == null || message.getSubject().isEmpty()) {
                message.setSubject("No Subject");
            }

            if (message.getPatientId() == null) {
                System.err.println("CONSUMER ERROR: Skipping message because patientId is NULL");
                return;
            }

            message.setId(null);
            Message savedMsg = messageRepository.save(message);
            System.out.println("CONSUMER SUCCESS: Saved message ID: " + savedMsg.getId());

        } catch (Exception e) {
            System.err.println("CONSUMER CRASHED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Standard Read Methods ---

    public List<Message> getMessagesForPatient(Long patientId) {
        return messageRepository.findByPatientId(patientId);
    }

    public List<Message> getMessagesForPractitioner(Long practitionerId) {
        return messageRepository.findByPractitionerId(practitionerId);
    }

    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(msg -> {
            msg.setRead(true);
            messageRepository.save(msg);
        });
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }
}