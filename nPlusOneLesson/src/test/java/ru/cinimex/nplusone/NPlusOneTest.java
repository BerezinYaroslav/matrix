package ru.cinimex.nplusone;

import java.util.List;

import ru.cinimex.nplusone.entity.Classroom;
import ru.cinimex.nplusone.repository.ClassroomRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NPlusOneTest {
    @Autowired
    private ClassroomRepository classroomRepository;

    /**
     * Hibernate interaction console output:
     * Hibernate:
     * select
     * classroom0_.id as id1_1_0_,
     * students1_.id as id1_4_1_,
     * classroom0_.name as name2_1_0_,
     * students1_.address as address2_4_1_,
     * students1_.classroom_id as classroo4_4_1_,
     * students1_.name as name3_4_1_,
     * students1_.classroom_id as classroo4_4_0__,
     * students1_.id as id1_4_0__
     * from
     * classroom classroom0_
     * left outer join
     * student students1_
     * on classroom0_.id=students1_.classroom_id
     */
    @Test
    void findAllTest() {
        List<Classroom> classrooms = classroomRepository.findAll();

        Assertions.assertEquals(classrooms.get(0).getStudents().size(), 2);
        Assertions.assertEquals(classrooms.get(1).getStudents().size(), 1);
    }


// Deprecated:
//    @Test
//    void createEntities() {
//        createCompanyAndUsers();
//        createFilialesAndCompanies();
//    }
//
//    @Test
//    @Transactional
//    void findCompanyByIdTest() {
//        companyRepository.findById(1L);
//        System.out.println();
//    }
//
//    @Test
//    void findUserByIdTest() {
//        userRepository.findById(3L);
//        System.out.println();
//    }
//
//
//    private void createCompanyAndUsers() {
//        Company company = createCompany("yandex");
//        Chat chat1 = createChat("chat1");
//        Chat chat2 = createChat("chat2");
//        Chat chat3 = createChat("chat3");
//
//        createUserChats(chat1, createUser("ivan", "Ivanov Ivan", createCompany("google"), Department.QA));
//        createUserChats(chat2, createUser("roman", "Romanov Roman", company, Department.BACKEND));
//        createUserChats(chat3, createUser("vadim", "Vadimov Vadim", company, Department.FRONTEND));
//        createUserChats(chat2, createUser("oleg", "Olegov Oleg", company, Department.BACKEND));
//        createUserChats(chat3, createUser("petr", "Petrov Petr", company, Department.FRONTEND));
//        createUserChats(chat2, createUser("dima", "Dimov Dima", company, Department.BACKEND));
//        createUserChats(chat3, createUser("victor", "Voctorov Victor", company, Department.FRONTEND));
//        createUserChats(chat2, createUser("denis", "Denisov Denis", company, Department.BACKEND));
//        createUserChats(chat3, createUser("vova", "Vovin Vova", company, Department.FRONTEND));
//        createUserChats(chat2, createUser("gosha", "Goshin Gosha", company, Department.BACKEND));
//    }
//
//    private void createFilialesAndCompanies() {
//        Company company = createCompany("yandex");
//
//        createFilial("Spb", 001L, company);
//        createFilial("Msk", 002L, company);
//        createFilial("Uhta", 003L, company);
//        createFilial("Milan", 004L, company);
//    }
//
//    private User createUser(String login, String fullName, Company company, Department department) {
//        User user = User.builder()
//                .login(login)
//                .fullName(fullName)
//                .company(company)
//                .department(department)
//                .build();
//       return userRepository.save(user);
//    }
//
//    private Company createCompany(String name) {
//        Company company = Company.builder()
//                .name(name)
//                .createdAt(LocalDateTime.now())
//                .build();
//        return companyRepository.save(company);
//    }
//
//    private Filial createFilial(String name, Long code, Company company) {
//        Filial filial = Filial.builder()
//                .name(name)
//                .code(code)
//                .company(company)
//                .build();
//        return filialRepository.save(filial);
//    }
//
//    private Chat createChat(String name){
//      Chat chat = Chat.builder()
//              .chatName(name)
//              .build();
//      return chatRepository.save(chat);
//    }
//
//    private UserChat createUserChats (Chat chat, User user){
//        UserChat userChat = UserChat.builder()
//                .chat(chat)
//                .user(user)
//                .createdAt(LocalDateTime.now())
//                .build();
//        return userChatRepository.save(userChat);
//    }
}
