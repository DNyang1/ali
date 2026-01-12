package com.finalProject.ali.qna.service;

import com.finalProject.ali.entity.Qna;
import com.finalProject.ali.qna.dto.QnaDTO;
import com.finalProject.ali.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QnaService {

    private final QnaRepository qnaRepository;

    // 1. [유저] 질문 등록
    public void createQna(QnaDTO dto) {
        Qna qna = Qna.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .writerId(dto.getWriterId()) // 로그인한 사용자 ID
                .status("WAITING")
                .build();

        qnaRepository.save(qna); // INSERT 자동 실행
    }

    // 2. [관리자] 답변 등록 (JPA의 꽃: Dirty Checking)
    public void answerQna(Long qnaId, String answerContent) {
        // (1) DB에서 꺼내온다
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        // (2) 자바 객체만 수정한다 (Setter 같은 메소드 호출)
        qna.registerAnswer(answerContent);

        // (3) 끝! qnaRepository.save() 안 불러도,
        //     @Transactional이 끝나면서 변경된 걸 감지하고 알아서 UPDATE 쿼리 날림.
    }

    // 3. [공통] 목록 조회 (Entity -> DTO 변환)
    @Transactional(readOnly = true)
    public List<QnaDTO> getQnaList(String userId, boolean isAdmin) {
        List<Qna> qnaList;

        if (isAdmin) {
            // 관리자는 모든 질문을 다 본다 (혹은 답변 대기중인 것만)
            qnaList = qnaRepository.findAll();
        } else {
            // 유저는 내 질문만 본다
            qnaList = qnaRepository.findByWriterIdOrderByCreatedAtDesc(userId);
        }

        // Entity 리스트를 DTO 리스트로 변환 (Java Stream 문법)
        return qnaList.stream().map(qna -> {
            QnaDTO dto = new QnaDTO();
            dto.setQnaId(qna.getQnaId());
            dto.setTitle(qna.getTitle());
            dto.setWriterId(qna.getWriterId());
            dto.setStatus(qna.getStatus());
            dto.setCreatedAt(qna.getCreatedAt());
            dto.setAnswer(qna.getAnswer()); // 답변도 같이 담아서 보냄
            return dto;
        }).collect(Collectors.toList());
    }

    // 4. [공통] 상세 조회
    @Transactional(readOnly = true)
    public QnaDTO getQnaDetail(Long qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        QnaDTO dto = new QnaDTO();
        dto.setQnaId(qna.getQnaId());
        dto.setTitle(qna.getTitle());
        dto.setContent(qna.getContent());
        dto.setWriterId(qna.getWriterId());
        dto.setAnswer(qna.getAnswer());
        dto.setAnsweredAt(qna.getAnsweredAt());
        dto.setStatus(qna.getStatus());
        dto.setCreatedAt(qna.getCreatedAt());
        return dto;
    }
}