package ru.practicum.explorewithme.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.users.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}