package com.booleanuk.api.controller;

import com.booleanuk.api.models.RecipePost;
import com.booleanuk.api.models.Review;
import com.booleanuk.api.models.User;
import com.booleanuk.api.payload.response.*;
import com.booleanuk.api.repositories.RecipePostRepository;
import com.booleanuk.api.repositories.ReviewRepository;
import com.booleanuk.api.repositories.RecipePostRepository; // Assuming this exists for handling Post entity
import com.booleanuk.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "https://bobs-bakery-front-end.vercel.app/", maxAge = 3600)

@RequestMapping("/posts/{postId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipePostRepository recipePostRepository; // Assuming you have a PostRepository

    @GetMapping
    public ResponseEntity<Response<?>> getAllReviewsForPost(@PathVariable int postId) {
        if (!recipePostRepository.existsById(postId)) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        ReviewListResponse reviewListResponse = new ReviewListResponse();
        reviewListResponse.set(reviewRepository.findByRecipePostId(postId));
        return ResponseEntity.ok(reviewListResponse);
    }

    @PostMapping
    public ResponseEntity<Response<?>> createReview(@PathVariable int postId, @RequestBody Review review) {
        // Extract username from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        RecipePost recipePost = this.recipePostRepository.findById(postId).orElse(null);
        if (recipePost == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        try {
            // Find the user by username
            User user = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));

            // Set the user to the recipe post
            review.setUser(user);
            review.setRecipePost(recipePost);
            Review savedReviewEntity = reviewRepository.save(review);
            ReviewResponse savedReview = new ReviewResponse();
            savedReview.set(savedReviewEntity);
            return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Could not create the review, please check all required fields");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Response<?>> getReviewByIdAndPostId(@PathVariable int postId, @PathVariable int reviewId) {
        if (!recipePostRepository.existsById(postId)) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        Review review = reviewRepository.findByIdAndRecipePostId(reviewId, postId).orElse(null);
        if (review == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post or review with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.set(review);
        return ResponseEntity.ok(reviewResponse);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Response<?>> updateReviewByIdAndRecipePostId(@PathVariable int postId, @PathVariable int reviewId, @RequestBody Review reviewDetails) {
        if (!this.recipePostRepository.existsById(postId)) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        Review existingReview = this.reviewRepository.findByIdAndRecipePostId(reviewId, postId).orElse(null);
        if (existingReview == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post or review with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        if (!existingReview.getUser().getUsername().equals(currentPrincipalName)) {
            ErrorResponse error = new ErrorResponse();
            error.set("Unauthorized to update this review");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        existingReview.setMessage(reviewDetails.getMessage());
        existingReview.setRating(reviewDetails.getRating());
        existingReview.setCreatedAt(reviewDetails.getCreatedAt());
        existingReview.setUpdatedAt(reviewDetails.getUpdatedAt());

        try {
            existingReview = this.reviewRepository.save(existingReview);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Could not create the recipe post, please check all required fields");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.set(existingReview);
        return ResponseEntity.ok(reviewResponse);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Response<?>> deleteReviewByIdAndRecipePostId(@PathVariable int postId, @PathVariable int reviewId) {
        if (!this.recipePostRepository.existsById(postId)) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        Review reviewToDelete = this.reviewRepository.findByIdAndRecipePostId(reviewId, postId).orElse(null);
        if (reviewToDelete == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No recipe post or review with that id found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the current authenticated user and check if they have the admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = reviewToDelete.getUser().getUsername().equals(currentPrincipalName);


        if (!(isOwner || isAdmin)) {
            ErrorResponse error = new ErrorResponse();
            error.set("Unauthorized to delete this review");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        this.reviewRepository.delete(reviewToDelete);
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.set(reviewToDelete);
        return ResponseEntity.ok(reviewResponse);
    }
}