package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.repository.PublicationIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicationIssueService {

    private final PublicationIssueRepository publicationIssueRepository;
    private final SlugService slugService;

    @Transactional(readOnly = true)
    public PublicationIssue findById(Long id) {
        return publicationIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PublicationIssue", id));
    }

    @Transactional
    public PublicationIssue create(String title, String description, String coverUrl) {
        String slug = slugService.generateUniqueSlug(title, PublicationIssue.class, null);
        PublicationIssue issue = PublicationIssue.builder()
                .title(title)
                .slug(slug)
                .description(description)
                .coverUrl(coverUrl)
                .build();
        return publicationIssueRepository.save(issue);
    }

    /**
     * Update publication issue fields.
     * 
     * Note: This method uses null to mean "no change" rather than "clear the field".
     * Passing null for a parameter will leave that field unchanged.
     * To intentionally clear a field, pass an empty string for String fields.
     * 
     * @param id the ID of the publication issue to update
     * @param title new title, or null to leave unchanged
     * @param description new description, or null to leave unchanged
     * @param coverUrl new cover URL, or null to leave unchanged
     * @return the updated publication issue
     */
    @Transactional
    public PublicationIssue update(Long id, String title, String description, String coverUrl) {
        PublicationIssue existing = findById(id);
        if (title != null && !title.equals(existing.getTitle())) {
            existing.setTitle(title);
            existing.setSlug(slugService.generateUniqueSlug(title, PublicationIssue.class, id));
        }
        if (description != null) existing.setDescription(description);
        if (coverUrl != null) existing.setCoverUrl(coverUrl);
        return publicationIssueRepository.save(existing);
    }

    @Transactional
    public PublicationIssue publish(Long id, LocalDateTime publishedAt) {
        PublicationIssue existing = findById(id);
        existing.setPublishedAt(publishedAt != null ? publishedAt : LocalDateTime.now());
        return publicationIssueRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        PublicationIssue existing = findById(id);
        publicationIssueRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public Page<PublicationIssue> listPublished(Pageable pageable) {
        return publicationIssueRepository.findAllPublished(pageable);
    }

    @Transactional(readOnly = true)
    public List<PublicationIssue> listPublished() {
        return publicationIssueRepository.findAllPublished();
    }
}
