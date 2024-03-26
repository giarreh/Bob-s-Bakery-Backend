package com.booleanuk.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "recipe_posts")
public class RecipePost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column
    private String title;

    @Column
    private String description;

    @Column(length = 3000)
    private List<String> ingredients;

    @Column(length = 3000)
    private List<String> instructions;

    @Column
    private String category;

    @Column(name = "baking_time")
    private int bakingTime;

    @Column
    private int calories;

    @Column
    private String difficulty;

    @Column
    private List<String> tags;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "image_url", length = 1500)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false )
    @JsonIncludeProperties(value = {"id", "firstName", "lastName"})
    private User user;

    @OneToMany(mappedBy = "recipePost")
    @JsonIgnoreProperties(value = {"id", "recipePost"})
    public List<Review> reviews;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now(); // Optionally set the same as createdAt
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }


    public RecipePost(String title, String description, List<String> ingredients, List<String> instructions,
                      String category, int bakingTime, int calories, String difficulty,
                      List<String> tags, LocalDate createdAt, LocalDate updatedAt) {
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.category = category;
        this.bakingTime = bakingTime;
        this.calories = calories;
        this.difficulty = difficulty;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
