package com.finalProject.ali.qna.controller;

import com.finalProject.ali.qna.dto.QnaDTO;
import com.finalProject.ali.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/qna")
public class QnaController {

    private final QnaService qnaService;

    // 1. 질문 목록 페이지 (유저는 내꺼만, 관리자는 전부)
    @GetMapping("/list")
    public String list(Model model) {
        // 현재 로그인한 사람 ID와 권한 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        // 권한 체크: ROLE_ADMIN을 가지고 있는지?
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        // 서비스에게 명단 요청
        List<QnaDTO> qnaList = qnaService.getQnaList(userId, isAdmin);

        // 화면(HTML)에 데이터 전달
        model.addAttribute("qnaList", qnaList);

        return "admin/qna/list"; // templates/qna/list.html 로 이동
    }

    // 2. 질문 작성 페이지 이동
    @GetMapping("/write")
    public String writeForm() {
        return "admin/qna/write";
    }

    // 3. 질문 등록 처리
    @PostMapping("/write")
    public String writeAction(@ModelAttribute QnaDTO dto) {
        // 작성자 ID는 로그인 정보에서 강제로 주입 (보안)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        dto.setWriterId(userId);

        qnaService.createQna(dto);

        return "redirect:/qna/list"; // 작성 후 목록으로 튕겨주기
    }

    // 4. 질문 상세 보기 (+관리자 답변 폼)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        QnaDTO qna = qnaService.getQnaDetail(id);
        model.addAttribute("qna", qna);
        return "admin/qna/detail";
    }

    // 5. [관리자 전용] 답변 등록 처리
    @PostMapping("/answer/{id}")
    public String answerAction(@PathVariable Long id, @RequestParam String answer) {
        qnaService.answerQna(id, answer);
        return "redirect:/qna/detail/" + id; // 답변 달고 다시 그 글로 이동
    }
}