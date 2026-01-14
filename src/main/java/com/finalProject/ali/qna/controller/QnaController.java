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

    // 1. 고객센터 메인 (필터 추가)
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "true") boolean showAll, Model model) { // 기본값: 전체보기(true)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        // showAll 값에 따라 서비스 호출 (true: 전체, false: 내글)
        List<QnaDTO> qnaList = qnaService.getQnaList(userId, showAll);

        model.addAttribute("qnaList", qnaList);
        model.addAttribute("showAll", showAll); // 현재 어떤 모드인지 화면에 알려줌 (버튼 색상용)

        return "qna/list";
    }

    // 2. 문의 작성 페이지
    @GetMapping("/write")
    public String writeForm() {
        return "qna/write";
    }

    // 3. 문의 등록 처리
    @PostMapping("/write")
    public String writeAction(@ModelAttribute QnaDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        dto.setWriterId(auth.getName());

        qnaService.createQna(dto);
        return "redirect:/qna/list";
    }

    // 4. 문의 상세 보기
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        QnaDTO qna = qnaService.getQnaDetail(id);

        // 🚨 중요: 공개 게시판이므로 "내 글 아니면 튕겨내기" 로직 삭제함!
        // 이제 다른 사람 글도 클릭해서 내용을 볼 수 있습니다.

        model.addAttribute("qna", qna);
        return "qna/detail";
    }
}