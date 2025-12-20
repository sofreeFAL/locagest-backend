package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
