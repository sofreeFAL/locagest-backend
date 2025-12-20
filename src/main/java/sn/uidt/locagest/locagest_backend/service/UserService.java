package sn.uidt.locagest.locagest_backend.service;

import org.springframework.stereotype.Service;
import sn.uidt.locagest.locagest_backend.model.User;
import sn.uidt.locagest.locagest_backend.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
