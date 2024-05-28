package ru.cinimex.nplusone.repository;

import ru.cinimex.nplusone.entity.Chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Deprecated
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
}
