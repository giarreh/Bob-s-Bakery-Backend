package com.booleanuk.api.repositories;

import com.booleanuk.api.models.RecipePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipePostRepository extends JpaRepository<RecipePost, Integer> {
}
