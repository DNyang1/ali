package com.finalProject.ali.repository;

import com.finalProject.ali.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// <User, String> 뜻: User 엔티티를 다룰 거고, PK(user_id)는 String 타입이다.
public interface UserRepository extends JpaRepository<User, String> {

    // 1. 회원가입/수정 (save) -> 이미 만들어져 있음
    // 2. 아이디로 조회 (findById) -> 이미 만들어져 있음

    // 3. 커스텀 조회: 이름과 이메일로 회원 찾기 (아이디 찾기 기능용)
    // SQL: select * from users where name = ? and email = ?
    Optional<User> findByNameAndEmail(String name, String email);


    // 5. 아이디 중복 체크 (이미 존재하는지?)
    boolean existsByUserId(String userId);
}