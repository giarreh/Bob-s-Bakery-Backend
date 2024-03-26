package com.booleanuk.api.repositories;

import com.booleanuk.api.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByRecipePostId(int recipePostId);

    Optional<Review> findByIdAndRecipePostId(int id, int recipePostId);
}
