package com.fullstack.massageservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.massageservice.entity.Message;
import com.fullstack.massageservice.repository.MessageRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public MessageService(MessageRepository messageRepository,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.messageRepository = messageRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void queueMessage(Message message) {
        try {
            // Convert Object -> JSON String
            String jsonMessage = objectMapper.writeValueAsString(message);
            System.out.println("Sending JSON to Kafka: " + jsonMessage);
            kafkaTemplate.send("messages", jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "messages", groupId = "messaging-group")
    public void consumeMessage(String messageJson) {
        try {
            System.out.println("Received raw JSON from Kafka: " + messageJson);
            // Convert JSON String -> Message Object
            Message message = objectMapper.readValue(messageJson, Message.class);

            messageRepository.save(message);
            System.out.println("Successfully saved message ID: " + message.getId());
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse message: " + e.getMessage());
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