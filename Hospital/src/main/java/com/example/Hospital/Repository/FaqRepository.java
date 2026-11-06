package com.example.Hospital.repository;


import com.example.Hospital.model.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    Page<Faq> findByActiveTrue(Pageable pageable);

    Page<Faq> findByActiveTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    boolean existsByQuestionIgnoreCase(String question);

    boolean existsByQuestionIgnoreCaseAndFaqIdNot(String question, Long faqId);
}
