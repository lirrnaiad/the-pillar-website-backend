package com.uep.pillar.repository;

import com.uep.pillar.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Find a role by its name.
     * 
     * @param name the role name (e.g., "ADMIN", "EDITOR", "WRITER")
     * @return the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if a role with the given name exists.
     * 
     * @param name the role name
     * @return true if exists
     */
    boolean existsByName(String name);
}

