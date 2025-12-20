package sn.uidt.locagest.locagest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.uidt.locagest.locagest_backend.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
