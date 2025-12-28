package com.uep.pillar.resolver.mutation;

import com.uep.pillar.dto.CreatePublicationIssueInput;
import com.uep.pillar.dto.UpdatePublicationIssueInput;
import com.uep.pillar.model.PublicationIssue;
import com.uep.pillar.service.PublicationIssueService;
import com.uep.pillar.service.UserService;
import org.springframework.stereotype.Component;

/**
 * GraphQL Mutation Resolver for PublicationIssue mutations.
 * Handles publication issue creation, updates, and deletion.
 */
@Component
public class PublicationIssueMutationResolver extends BaseMutationResolver {

    private final PublicationIssueService publicationIssueService;

    // UserService is required by BaseMutationResolver for getCurrentUser()
    public PublicationIssueMutationResolver(PublicationIssueService publicationIssueService, UserService userService) {
        super(userService);
        this.publicationIssueService = publicationIssueService;
    }

    /**
     * Create a new publication issue.
     *
     * @param input publication issue creation input
     * @return the created publication issue
     */
    public PublicationIssue createPublicationIssue(CreatePublicationIssueInput input) {
        // Note: Slug from input is ignored; service auto-generates from title
        PublicationIssue issue = publicationIssueService.create(
            input.getTitle(),
            input.getDescription(),
            input.getCoverUrl()
        );

        // If publishedAt is provided, publish the issue
        if (input.getPublishedAt() != null) {
            issue = publicationIssueService.publish(issue.getId(), input.getPublishedAt());
        }

        return issue;
    }

    /**
     * Update an existing publication issue.
     *
     * @param input publication issue update input
     * @return the updated publication issue
     */
    public PublicationIssue updatePublicationIssue(UpdatePublicationIssueInput input) {
        Long id = parseLongId(input.getId(), "PublicationIssue ID");
        
        // Note: Slug from input is ignored; service auto-updates if title changes
        PublicationIssue issue = publicationIssueService.update(
            id,
            input.getTitle(),
            input.getDescription(),
            input.getCoverUrl()
        );

        // Handle publishedAt if provided
        if (input.getPublishedAt() != null) {
            issue = publicationIssueService.publish(id, input.getPublishedAt());
        }

        return issue;
    }

    /**
     * Delete a publication issue.
     *
     * @param id the publication issue ID
     * @return true on success
     */
    public Boolean deletePublicationIssue(String id) {
        Long issueId = parseLongId(id, "PublicationIssue ID");
        publicationIssueService.delete(issueId);
        return true;
    }
}
