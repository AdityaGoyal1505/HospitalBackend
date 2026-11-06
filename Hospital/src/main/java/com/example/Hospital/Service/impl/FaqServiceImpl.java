package com.example.Hospital.service.impl;

import com.example.Hospital.dto.FaqRequestDTO;
import com.example.Hospital.dto.FaqResponseDTO;
import com.example.Hospital.dto.FaqUpdateRequestDTO;
import com.example.Hospital.model.Faq;
import com.example.Hospital.exception.ResourceNotFoundException;
import com.example.Hospital.repository.FaqRepository;
import com.example.Hospital.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;

    private FaqResponseDTO toResponse(Faq faq) {
        FaqResponseDTO dto = new FaqResponseDTO();
        dto.setFaqId(faq.getFaqId());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        dto.setCategory(faq.getCategory());
        dto.setActive(faq.isActive());
        dto.setCreatedAt(faq.getCreatedAt());
        dto.setUpdatedAt(faq.getUpdatedAt());
        return dto;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Faq findOrThrow(Long faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with id " + faqId));
    }

    @Override
    @Transactional
    public FaqResponseDTO create(FaqRequestDTO request) {
        String question = request.getQuestion().trim();
        if (faqRepository.existsByQuestionIgnoreCase(question)) {
            throw new IllegalArgumentException("A FAQ with this question already exists");
        }

        Faq faq = new Faq();
        faq.setQuestion(question);
        faq.setAnswer(request.getAnswer().trim());
        faq.setCategory(clean(request.getCategory()));
        if (request.getActive() != null) {
            faq.setActive(request.getActive());
        }
        return toResponse(faqRepository.save(faq));
    }

    @Override
    @Transactional(readOnly = true)
    public FaqResponseDTO getById(Long faqId) {
        return toResponse(findOrThrow(faqId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FaqResponseDTO> getAllActive(String category, Pageable pageable) {
        String cleanCategory = clean(category);

        Page<Faq> result = (cleanCategory == null)
                ? faqRepository.findByActiveTrue(pageable)
                : faqRepository.findByActiveTrueAndCategoryIgnoreCase(cleanCategory, pageable);

        return result.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FaqResponseDTO> getAllForAdmin(Pageable pageable) {
        return faqRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public FaqResponseDTO update(Long faqId, FaqUpdateRequestDTO request) {
        Faq faq = findOrThrow(faqId);

        boolean nothingSent = request.getQuestion() == null
                && request.getAnswer() == null
                && request.getCategory() == null;
        if (nothingSent) {
            throw new IllegalArgumentException("Send at least one field to update");
        }

        if (request.getQuestion() != null) {
            String question = request.getQuestion().trim();

            if (faqRepository.existsByQuestionIgnoreCaseAndFaqIdNot(question, faqId)) {
                throw new IllegalArgumentException("A FAQ with this question already exists");
            }
            faq.setQuestion(question);
        }

        if (request.getAnswer() != null) {
            faq.setAnswer(request.getAnswer().trim());
        }

        if (request.getCategory() != null) {
            faq.setCategory(clean(request.getCategory()));
        }

        return toResponse(faqRepository.save(faq));
    }

    @Override
    @Transactional
    public void deactivate(Long faqId) {
        Faq faq = findOrThrow(faqId);
        faq.setActive(false);
        faqRepository.save(faq);
    }

    @Override
    @Transactional
    public void activate(Long faqId) {
        Faq faq = findOrThrow(faqId);
        faq.setActive(true);
        faqRepository.save(faq);
    }

    @Override
    @Transactional
    public void delete(Long faqId) {
        Faq faq = findOrThrow(faqId);
        faqRepository.delete(faq);
    }
}

