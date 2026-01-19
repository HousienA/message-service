package com.fullstack.massageservice.controller;


import com.fullstack.massageservice.entity.Message;
import com.fullstack.massageservice.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessagingController {

    private final MessageService messageService;

    public MessagingController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Object> sendMessage(@RequestBody Message message) {
        // Send to Kafka queue
        messageService.queueMessage(message);

        // 2. Return a Map. Spring will automatically convert this to {"status": "queued"} JSON
        return ResponseEntity.accepted().body(java.util.Map.of(
                "status", "queued",
                "message", "Message queued for delivery"
        ));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Message>> getForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(messageService.getMessagesForPatient(patientId));
    }

    @GetMapping("/practitioner/{practitionerId}")
    public ResponseEntity<List<Message>> getForPractitioner(@PathVariable Long practitionerId) {
        return ResponseEntity.ok(messageService.getMessagesForPractitioner(practitionerId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}