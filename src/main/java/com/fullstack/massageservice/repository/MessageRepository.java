package com.fullstack.massageservice.repository;

import com.fullstack.massageservice.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Vi söker nu på ID-siffran, inte objektet
    List<Message> findByPatientId(Long patientId);
    List<Message> findByPractitionerId(Long practitionerId);

}