package ru.cinimex.nplusone.repository;

import java.util.List;

import ru.cinimex.nplusone.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Deprecated
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

/* Одно из решений проблемы n+1 при получаении всех user, добавить "fetch" в запрос -

@Query("SELECT u FROM User u JOIN FETCH u.company ")
List<User> findAll();

в таком случае запрос будет выглядеть следующим образом:

    Hibernate:
    select
        user0_.id as id1_1_0_,
        company1_.id as id1_0_1_,
        user0_.company_id as company_5_1_0_,
        user0_.department as departme2_1_0_,
        user0_.full_name as full_nam3_1_0_,
        user0_.login as login4_1_0_,
        company1_.created_at as created_2_0_1_,
        company1_.name as name3_0_1_
    from
        user_table user0_
    inner join
        company company1_
            on user0_.company_id=company1_.id

ВМЕСТО

     Hibernate:
    select
        user0_.id as id1_1_,
        user0_.company_id as company_5_1_,
        user0_.department as departme2_1_,
        user0_.full_name as full_nam3_1_,
        user0_.login as login4_1_
    from
        user_table user0_
Hibernate:
    select
        company0_.id as id1_0_0_,
        company0_.created_at as created_2_0_0_,
        company0_.name as name3_0_0_
    from
        company company0_
    where
        company0_.id=?
Hibernate:
    select
        company0_.id as id1_0_0_,
        company0_.created_at as created_2_0_0_,
        company0_.name as name3_0_0_
    from
        company company0_
    where
        company0_.id=?
     */


    /*
     @EntityGraph(value = "User.company")
     List<User> findAll();

     при таком решении запрос будет выглядеть следующим образом:

     Hibernate:
     select
         user0_.id as id1_1_0_,
         company1_.id as id1_0_1_,
         user0_.company_id as company_5_1_0_,
         user0_.department as departme2_1_0_,
         user0_.full_name as full_nam3_1_0_,
         user0_.login as login4_1_0_,
         company1_.created_at as created_2_0_1_,
         company1_.name as name3_0_1_
     from
         user_table user0_
     left outer join
         company company1_
             on user0_.company_id=company1_.id
     */
    @EntityGraph(value = "User.company")
    List<User> findAll();
}
