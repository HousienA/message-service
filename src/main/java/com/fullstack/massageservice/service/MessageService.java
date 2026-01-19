package com.fullstack.massageservice.service;

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
    private final KafkaTemplate<String, Message> kafkaTemplate; // New dependency

    public MessageService(MessageRepository messageRepository, KafkaTemplate<String, Message> kafkaTemplate) {
        this.messageRepository = messageRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * PRODUCER: Sends the message to Kafka.
     * This is what the Controller calls.
     */
    public void queueMessage(Message message) {
        // We assume valid patientId etc.
        System.out.println("Sending message to Kafka topic 'messages'...");
        kafkaTemplate.send("messages", message);
    }

    /**
     * CONSUMER: Listens to Kafka and saves to DB.
     * This runs automatically in the background.
     */
    @KafkaListener(topics = "messages", groupId = "messaging-group")
    public void consumeMessage(Message message) {
        System.out.println("Received message from Kafka. Saving to DB...");
        messageRepository.save(message);
    }


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