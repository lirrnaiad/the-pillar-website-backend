package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Tag;
import com.uep.pillar.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final SlugService slugService;

    @Transactional(readOnly = true)
    public Tag findById(Integer id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    @Transactional(readOnly = true)
    public Optional<Tag> findBySlug(String slug) {
        return tagRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    @Transactional
    public Tag create(String name) {
        String slug = slugService.generateUniqueSlug(name, Tag.class, null);
        Tag tag = Tag.builder()
                .name(name)
                .slug(slug)
                .build();
        return tagRepository.save(tag);
    }

    @Transactional
    public Tag update(Integer id, String name) {
        Tag existing = findById(id);
        if (name != null && !name.equals(existing.getName())) {
            existing.setName(name);
            existing.setSlug(slugService.generateUniqueSlug(name, Tag.class, id.longValue()));
        }
        return tagRepository.save(existing);
    }

    @Transactional
    public void delete(Integer id) {
        Tag existing = findById(id);
        tagRepository.delete(existing);
    }

    /**
     * Find existing tags by names or create them if missing.
     * Uses proper transaction handling to minimize race conditions.
     */
    @Transactional
    public List<Tag> findOrCreateByNames(Collection<String> names) {
        if (names == null || names.isEmpty()) return Collections.emptyList();
        
        List<Tag> existing = tagRepository.findByNameIn(names);
        Set<String> foundNames = new HashSet<>();
        for (Tag t : existing) {
            if (t.getName() != null) foundNames.add(t.getName());
        }
        
        List<Tag> toCreate = new ArrayList<>();
        for (String name : names) {
            if (!foundNames.contains(name)) {
                String slug = slugService.generateUniqueSlug(name, Tag.class, null);
                toCreate.add(Tag.builder().name(name).slug(slug).build());
            }
        }
        
        // Create a new list to return instead of modifying the repository result
        List<Tag> result = new ArrayList<>(existing);
        if (!toCreate.isEmpty()) {
            try {
                result.addAll(tagRepository.saveAll(toCreate));
            } catch (Exception e) {
                // Handle potential unique constraint violations from race conditions
                // Re-query to get the complete, updated list
                result = tagRepository.findByNameIn(names);
            }
        }
        return result;
    }
}
