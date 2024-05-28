package ru.cinimex.nplusone.repository;

import java.util.Optional;

import ru.cinimex.nplusone.entity.Company;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Deprecated
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @EntityGraph(value = "Company.filiales")
    Optional<Company> findById(@Param("id") Long id);
}
