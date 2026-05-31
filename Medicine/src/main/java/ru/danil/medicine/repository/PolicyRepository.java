package ru.danil.medicine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.danil.medicine.model.Policy;

import java.util.UUID;

public interface PolicyRepository extends JpaRepository<Policy, UUID> {
}
