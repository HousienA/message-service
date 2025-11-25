package com.fullstack.patientjournalbackend.service;

import com.fullstack.patientjournalbackend.dto.MessageDTO;
import com.fullstack.patientjournalbackend.entity.Message;
import com.fullstack.patientjournalbackend.entity.Patient;
import com.fullstack.patientjournalbackend.enums.Result;
import com.fullstack.patientjournalbackend.exception.ApiException;
import com.fullstack.patientjournalbackend.exception.NotFoundException;
import com.fullstack.patientjournalbackend.repository.MessageRepository;
import com.fullstack.patientjournalbackend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private PatientRepository patientRepository;

    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MessageDTO> getMessageById(Long id) {
        return messageRepository.findById(id).map(this::convertToDTO);
    }

    public List<MessageDTO> getMessagesByPatientId(Long patientId) {
        return messageRepository.findByPatientId(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getUnreadMessagesByPatientId(Long patientId) {
        return messageRepository.findByPatientIdAndIsReadFalse(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Result createMessage(MessageDTO dto) {
        if (dto.getPatientId() == null) throw new ApiException("patientId is required");
        if (dto.getSenderName() == null || dto.getSenderName().isEmpty()) {
            throw new ApiException("senderName is required");
        }
        if (dto.getSubject() == null || dto.getSubject().isEmpty()) {
            throw new ApiException("subject is required");
        }

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        Message message = convertToEntity(dto, patient);
        if (message.getSentAt() == null) message.setSentAt(LocalDateTime.now());
        if (message.getIsRead() == null) message.setIsRead(false);

        messageRepository.save(message);
        return Result.SUCCESS;
    }

    public Result updateMessage(Long id, MessageDTO dto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        message.setSenderName(dto.getSenderName());
        message.setSubject(dto.getSubject());
        message.setContent(dto.getContent());
        message.setIsRead(dto.getIsRead());

        messageRepository.save(message);
        return Result.SUCCESS;
    }

    public Result markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        message.setIsRead(true);
        messageRepository.save(message);
        return Result.SUCCESS;
    }

    public Result deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new NotFoundException("Message not found");
        }
        messageRepository.deleteById(id);
        return Result.DELETED;
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setPatientId(message.getPatient().getId());
        dto.setSenderName(message.getSenderName());
        dto.setSubject(message.getSubject());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setIsRead(message.getIsRead());
        return dto;
    }

    private Message convertToEntity(MessageDTO dto, Patient patient) {
        Message message = new Message();
        message.setPatient(patient);
        message.setSenderName(dto.getSenderName());
        message.setSubject(dto.getSubject());
        message.setContent(dto.getContent());
        message.setSentAt(dto.getSentAt());
        message.setIsRead(dto.getIsRead());
        return message;
    }
}
