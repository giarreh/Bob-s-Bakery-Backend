package com.booleanuk.api.controller;

import com.booleanuk.api.models.RecipePost;
import com.booleanuk.api.models.User;
import com.booleanuk.api.payload.response.ErrorResponse;
import com.booleanuk.api.payload.response.RecipePostListResponse;
import com.booleanuk.api.payload.response.RecipePostResponse;
import com.booleanuk.api.payload.response.Response;
import com.booleanuk.api.repositories.RecipePostRepository;
import com.booleanuk.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "https://bobs-bakery-front-end.vercel.app/", maxAge = 3600)

@RequestMapping("/posts")
public class RecipePostController {

    @Autowired
    private RecipePostRepository recipePostRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<RecipePostListResponse> getAllRecipePosts() {
        RecipePostListResponse recipePostListResponse = new RecipePostListResponse();
        recipePostListResponse.set(this.recipePostRepository.findAll());
        return ResponseEntity.ok(recipePostListResponse);
    }

    @PostMapping
    public ResponseEntity<Response<?>> createRecipePost(@RequestBody RecipePost recipePost) {
        RecipePostResponse recipePostResponse = new RecipePostResponse();

        // Extract username from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        try {
            // Find the user by username
            User user = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

            // Set the user to the recipe post
            recipePost.setUser(user);

            // Save the recipe post
            RecipePost savedRecipePost = recipePostRepository.save(recipePost);
            recipePostResponse.set(savedRecipePost);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Could not create the recipe post, please check all required fields");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(recipePostResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Response<?>> getRecipePostById(@PathVariable int postId) {
        RecipePost recipePost = this.recipePostRepository.findById(postId).orElse(null);
        if (recipePost == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        RecipePostResponse recipePostResponse = new RecipePostResponse();
        recipePostResponse.set(recipePost);
        return ResponseEntity.ok(recipePostResponse);
    }

    // Update a post
    @PutMapping("/{postId}")
    public ResponseEntity<Response<?>> updateRecipePostById(@PathVariable int postId, @RequestBody RecipePost recipePost) {
        RecipePost existingRecipePost = this.recipePostRepository.findById(postId).orElse(null);
        if (existingRecipePost == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        // Check if the current user is the owner of the post
        if (!existingRecipePost.getUser().getUsername().equals(currentPrincipalName)) {
            ErrorResponse error = new ErrorResponse();
            error.set("Unauthorized to update this post");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        // Update the post
        existingRecipePost.setTitle(recipePost.getTitle());
        existingRecipePost.setDescription(recipePost.getDescription());
        existingRecipePost.setIngredients(recipePost.getIngredients());
        existingRecipePost.setInstructions(recipePost.getInstructions());
        existingRecipePost.setCategory(recipePost.getCategory());
        existingRecipePost.setBakingTime(recipePost.getBakingTime());
        existingRecipePost.setCalories(recipePost.getCalories());
        existingRecipePost.setDifficulty(recipePost.getDifficulty());
        existingRecipePost.setTags(recipePost.getTags());
        existingRecipePost.setCreatedAt(recipePost.getCreatedAt());
        existingRecipePost.setUpdatedAt(recipePost.getUpdatedAt());
        existingRecipePost.setImageUrl(recipePost.getImageUrl());

        try {
            existingRecipePost = this.recipePostRepository.save(existingRecipePost);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Could not create the recipe post, please check all required fields");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        RecipePostResponse recipePostResponse = new RecipePostResponse();
        recipePostResponse.set(existingRecipePost);
        return new ResponseEntity<>(recipePostResponse, HttpStatus.OK);
    }

    // Delete a post
    @DeleteMapping("/{postId}")
    public ResponseEntity<Response<?>> deleteRecipePostById(@PathVariable int postId) {
        RecipePost recipePostToDelete = this.recipePostRepository.findById(postId).orElse(null);
        if (recipePostToDelete == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the current authenticated user and check if they have the admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = recipePostToDelete.getUser().getUsername().equals(currentPrincipalName);


        if (!(isOwner || isAdmin)) {
            ErrorResponse error = new ErrorResponse();
            error.set("Unauthorized to delete this post");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        this.recipePostRepository.delete(recipePostToDelete);
        RecipePostResponse recipePostResponse = new RecipePostResponse();
        recipePostResponse.set(recipePostToDelete);
        return ResponseEntity.ok(recipePostResponse);
    }
}


    /*
    @PostMapping("/{postId}/like")
    public ResponseEntity<Response<?>> likePost(@PathVariable int postId) {
        RecipePost recipePost = recipePostRepository.findById(postId).orElse(null);
        if (recipePost == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        // Assuming like is a toggle action
        recipePost.setLike(!recipePost.isLike());
        recipePostRepository.save(recipePost);
        RecipePostResponse recipePostResponse = new RecipePostResponse();
        recipePostResponse.set(recipePost);
        return ResponseEntity.ok(recipePostResponse);
    }
    @PostMapping("/{postId}/comment")
    public ResponseEntity<Response<?>> commentOnPost(@PathVariable int postId, @RequestBody String comment) {
        RecipePost recipePost = recipePostRepository.findById(postId).orElse(null);
        if (recipePost == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        recipePost.setComment(comment);
        recipePostRepository.save(recipePost);
        RecipePostResponse recipePostResponse = new RecipePostResponse();
        recipePostResponse.set(recipePost);
        return ResponseEntity.ok(recipePostResponse);
    } */
