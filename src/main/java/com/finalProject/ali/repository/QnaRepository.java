package com.finalProject.ali.repository;

import com.finalProject.ali.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// <Entity 클래스, PK 타입>
public interface QnaRepository extends JpaRepository<Qna, Long> {

    // 1. [유저용] 내가 쓴 질문 목록 조회 (최신순 정렬)
    // SQL: SELECT * FROM qna WHERE writer_id = ? ORDER BY created_at DESC
    List<Qna> findByWriterIdOrderByCreatedAtDesc(String writerId);

    // 2. [관리자용] 답변 대기중인 질문 목록 조회 (상태별 + 최신순)
    // SQL: SELECT * FROM qna WHERE status = ? ORDER BY created_at DESC
    List<Qna> findByStatusOrderByCreatedAtDesc(String status);

    // 3. [관리자용] 전체 질문 목록 조회 (최신순)
    // SQL: SELECT * FROM qna ORDER BY created_at DESC
    List<Qna> findAllByOrderByCreatedAtDesc();
}