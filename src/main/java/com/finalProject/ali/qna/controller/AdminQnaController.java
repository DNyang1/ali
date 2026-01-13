package com.finalProject.ali.qna.controller;

import com.finalProject.ali.qna.dto.QnaDTO;
import com.finalProject.ali.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/qna") // 👈 주소 체계 분리
public class AdminQnaController {

    private final QnaService qnaService;

    // 관리자용 전체 목록
    @GetMapping("/list")
    public String list(Model model) {
        // 관리자는 "전체"를 봐야 함 (isAdmin = true, userId는 상관없음)
        List<QnaDTO> qnaList = qnaService.getQnaList("admin", true);
        model.addAttribute("qnaList", qnaList);

        return "admin/qna/a_list";
    }

    // 관리자용 상세 보기 (답변 달기용)
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        QnaDTO qna = qnaService.getQnaDetail(id);
        model.addAttribute("qna", qna);
        return "admin/qna/a_detail";
    }

    // 답변 등록 액션
    @PostMapping("/answer/{id}")
    public String answerAction(@PathVariable Long id, @RequestParam String answer) {
        qnaService.answerQna(id, answer);
        return "redirect:/admin/qna/detail/" + id;
    }
}