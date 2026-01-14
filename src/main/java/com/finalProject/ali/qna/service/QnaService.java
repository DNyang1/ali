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
                .writerId(dto.getWriterId())
                .status("WAITING")
                .build();
        qnaRepository.save(qna);
    }

    // 2. [관리자] 답변 등록
    public void answerQna(Long qnaId, String answerContent) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        qna.registerAnswer(answerContent);
    }

    // 3. [공통] 목록 조회 (로직 변경됨!)
    // showAll이 true면 '전체 조회', false면 '내 글만 조회'
    @Transactional(readOnly = true)
    public List<QnaDTO> getQnaList(String userId, boolean showAll) {
        List<Qna> qnaList;

        if (showAll) {
            // 전체보기 (다른 사람 글도 다 보임)
            qnaList = qnaRepository.findAllByOrderByCreatedAtDesc();
        } else {
            // 내 글만 보기
            qnaList = qnaRepository.findByWriterIdOrderByCreatedAtDesc(userId);
        }

        return qnaList.stream().map(qna -> {
            QnaDTO dto = new QnaDTO();
            dto.setQnaId(qna.getQnaId());
            dto.setTitle(qna.getTitle());
            dto.setWriterId(qna.getWriterId());
            dto.setStatus(qna.getStatus());
            dto.setCreatedAt(qna.getCreatedAt());
            dto.setAnswer(qna.getAnswer());
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