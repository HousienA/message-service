package com.fullstack.patientjournalbackend.controller;

import com.fullstack.patientjournalbackend.dto.MessageDTO;
import com.fullstack.patientjournalbackend.enums.Result;
import com.fullstack.patientjournalbackend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/messaging")
@CrossOrigin(origins = "http://localhost:5173")
public class MessagingController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable Long id) {
        Optional<MessageDTO> dto = messageService.getMessageById(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("patient/{patientId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(messageService.getMessagesByPatientId(patientId));
    }

    @GetMapping("patient/{patientId}/unread")
    public ResponseEntity<List<MessageDTO>> getUnreadMessagesByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(messageService.getUnreadMessagesByPatientId(patientId));
    }

    @PostMapping
    public ResponseEntity<String> createMessage(@Valid @RequestBody MessageDTO dto) {
        Result result = messageService.createMessage(dto);
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok("Message created successfully");
            default -> ResponseEntity.internalServerError().body("Failed to create message");
        };
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateMessage(@PathVariable Long id, @Valid @RequestBody MessageDTO dto) {
        Result result = messageService.updateMessage(id, dto);
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok("Message updated successfully");
            default -> ResponseEntity.internalServerError().body("Failed to update message");
        };
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        Result result = messageService.markAsRead(id);
        return switch (result) {
            case SUCCESS -> ResponseEntity.ok("Message marked as read");
            default -> ResponseEntity.internalServerError().body("Failed to mark message as read");
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
        Result result = messageService.deleteMessage(id);
        return switch (result) {
            case DELETED -> ResponseEntity.ok("Message deleted successfully");
            default -> ResponseEntity.internalServerError().body("Failed to delete message");
        };
    }
}
