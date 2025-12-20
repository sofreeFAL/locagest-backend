package sn.uidt.locagest.locagest_backend.controller;

import org.springframework.web.bind.annotation.*;
import sn.uidt.locagest.locagest_backend.model.User;
import sn.uidt.locagest.locagest_backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
