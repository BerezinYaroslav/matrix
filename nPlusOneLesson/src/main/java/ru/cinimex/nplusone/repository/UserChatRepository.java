package ru.cinimex.nplusone.repository;

import ru.cinimex.nplusone.entity.UserChat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Deprecated
@Repository
public interface UserChatRepository extends JpaRepository<UserChat, Long> {
}
