package ru.cinimex.nplusone.service;

import java.util.List;

import ru.cinimex.nplusone.entity.Classroom;
import ru.cinimex.nplusone.repository.ClassroomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    private final ClassroomRepository repository;

    public List<Classroom> findAllClassrooms() {
        return repository.findAll();
    }
}
