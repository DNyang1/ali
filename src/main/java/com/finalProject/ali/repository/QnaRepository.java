package com.finalProject.ali.repository;

import com.finalProject.ali.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 1. 관리자용: 답변 안 달린 것만 가져오기 (WAITING 상태인 것 최신순)
    List<Qna> findByStatusOrderByCreatedAtDesc(String status);

    // 2. 유저용: 내가 쓴 QnA만 가져오기
    List<Qna> findByWriterIdOrderByCreatedAtDesc(String writerId);

    // (기본 findAll(), findById(), save() 등은 이미 들어있음)
}