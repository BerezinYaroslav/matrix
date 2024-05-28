package ru.cinimex.nplusone.repository;

import java.util.List;

import ru.cinimex.nplusone.entity.Classroom;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @EntityGraph(attributePaths = {"students"})
    List<Classroom> findAll();
}
