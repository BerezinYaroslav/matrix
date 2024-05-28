package ru.cinimex.nplusone.repository;

import java.util.Optional;

import ru.cinimex.nplusone.entity.Filial;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Deprecated
@Repository
public interface FilialRepository extends JpaRepository<Filial, Long> {

    //@Query("Select f FROM Filial f LEFT JOIN f.company c WHERE f.id = :id")
    Optional<Filial> findById(@Param("id") Long id);
}
