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

    // 1. 고객센터 메인 (내 문의 내역 + 글쓰기 버튼)
    @GetMapping("/list")
    public String list(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName(); // 로그인한 유저 ID

        // 유저는 "내 문의"만 봐야 함 (isAdmin = false)
        List<QnaDTO> qnaList = qnaService.getQnaList(userId, false);
        model.addAttribute("qnaList", qnaList);

        return "qna/list"; // 👈 templates/inquiry/a_list.html (새로 만들 디자인)
    }

    // 2. 문의 작성 페이지
    @GetMapping("/write")
    public String writeForm() {
        return "qna/write"; // 👈 templates/inquiry/write.html
    }

    // 3. 문의 등록 처리
    @PostMapping("/write")
    public String writeAction(@ModelAttribute QnaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        dto.setWriterId(auth.getName()); // 작성자 강제 주입

        qnaService.createQna(dto);
        return "redirect:/qna/list";
    }

    // 4. 문의 상세 보기
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        QnaDTO qna = qnaService.getQnaDetail(id);

        // 보안 체크: 내 글이 아니면 튕겨내기 (관리자는 AdminController로 들어감)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!qna.getWriterId().equals(auth.getName())) {
            return "redirect:/qna/list";
        }

        model.addAttribute("qna", qna);
        return "qna/detail"; // 👈 templates/inquiry/a_detail.html
    }
}