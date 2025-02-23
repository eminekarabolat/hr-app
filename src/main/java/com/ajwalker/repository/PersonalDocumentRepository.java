package com.ajwalker.repository;

import com.ajwalker.entity.PersonalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PersonalDocumentRepository extends JpaRepository<PersonalDocument, Long> {
	Optional<PersonalDocument> findByUserId(Long userId);

    @Query("SELECT P FROM PersonalDocument P WHERE P.userId IN(?1)")
    List<PersonalDocument> findAllByUserIds(List<Long> userIds);


    @Query("SELECT p FROM PersonalDocument p WHERE p.userId = :userId")
    PersonalDocument findByListUserIdInfo(@Param("userId") Long id);
	
	PersonalDocument findByFirstNameAndLastNameAndEmail(String firstName, String lastName, String email);
	
}