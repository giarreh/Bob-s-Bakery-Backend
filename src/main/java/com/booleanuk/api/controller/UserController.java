package com.booleanuk.api.controller;

import com.booleanuk.api.models.RecipePost;
import com.booleanuk.api.models.User;
import com.booleanuk.api.payload.response.*;
import com.booleanuk.api.repositories.RecipePostRepository;
import com.booleanuk.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "https://bobs-bakery-front-end.vercel.app/", maxAge = 3600)

@RequestMapping("/users")
public class UserController {

    @Autowired
    private RecipePostRepository recipePostRepository;

    @Autowired
    private UserRepository userRepository;

    //Get all users
    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers() {
        UserListResponse userListResponse = new UserListResponse();
        userListResponse.set(this.userRepository.findAll());
        return ResponseEntity.ok(userListResponse);
    }

    /*
    @PostMapping
    public ResponseEntity<Response<?>> createUser(@RequestBody RecipePost recipePost) {
        RecipePostResponse recipePostResponse = new RecipePostResponse();
        try {
            recipePostResponse.set(this.recipePostRepository.save(recipePost));
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse();
            error.set("Bad request");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(recipePostResponse, HttpStatus.CREATED);
    } */

    @GetMapping("/{userId}")
    public ResponseEntity<Response<?>> getUserById(@PathVariable int userId) {
        User user = this.userRepository.findById(userId).orElse(null);
        if (user == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No user with that id were found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        UserResponse userResponse = new UserResponse();
        userResponse.set(user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Response<?>> getUserByUsername(@PathVariable String username) {
        User user = this.userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No user with that username were found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        UserResponse userResponse = new UserResponse();
        userResponse.set(user);
        return ResponseEntity.ok(userResponse);
    }

    // Update a user
    @PutMapping("/{userId}")
    public ResponseEntity<Response<?>> updateUserById(@PathVariable int userId, @RequestBody User user) {
        User existingUser = this.userRepository.findById(userId).orElse(null);
        if (existingUser == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No user matching that id were found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = existingUser.getUsername().equals(currentPrincipalName);

        // Check if the current user is the owner of the post or admin
        if (!(isOwner || isAdmin)) {
            ErrorResponse error = new ErrorResponse();
            error.set("Unauthorized to delete this user");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        // Update the post
        existingUser.setUsername(user.getUsername());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRoles(user.getRoles());

        existingUser = this.userRepository.save(existingUser);
        UserResponse userResponse = new UserResponse();
        userResponse.set(existingUser);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    // Delete a post
    @DeleteMapping("/{userId}")
    public ResponseEntity<Response<?>> deleteRecipePostById(@PathVariable int userId) {
        User userToDelete = this.userRepository.findById(userId).orElse(null);
        if (userToDelete == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("No user with that id were found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the current authenticated user and check if they have the admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = userToDelete.getUsername().equals(currentPrincipalName);


        if (!(isOwner || isAdmin)) {
            ErrorResponse error = new ErrorResponse();
            error.set("Unauthorized to delete this user");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        this.userRepository.delete(userToDelete);
        UserResponse userResponse = new UserResponse();
        userResponse.set(userToDelete);
        return ResponseEntity.ok(userResponse);
    }
}

