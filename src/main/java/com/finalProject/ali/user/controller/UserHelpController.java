package com.finalProject.ali.user.controller;

import com.finalProject.ali.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserHelpController {

    @Autowired private UserService userService;

    @GetMapping("/find_id")
    public String findIdPage() { return "user/find_id"; }

    @PostMapping("/find_id")
    @ResponseBody
    public ResponseEntity<String> findId(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        String type = data.get("type");
        String value = data.get("value");
        String userId = userService.findId(name, type, value);

        if (userId != null) {
            int length = userId.length();
            String maskedId = (length > 4)
                    ? userId.substring(0, 4) + "*".repeat(length - 4)
                    : userId.substring(0, 1) + "*".repeat(length - 1);
            return ResponseEntity.ok(maskedId);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
    }

    @GetMapping("/reset_pw")
    public String resetPwPage() { return "user/reset_pw"; }

    @PostMapping("/send_reset_link")
    @ResponseBody
    public ResponseEntity<String> sendResetLink(@RequestBody Map<String, String> data) {
        String userId = data.get("userId");
        String email = data.get("email");
        if (userService.checkUserForReset(userId, email)) {
            userService.processForgotPassword(userId, email);
            return ResponseEntity.ok("success");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
    }

    @GetMapping("/reset_pw_confirm")
    public String confirmResetToken(@RequestParam("token") String token, Model model) {
        String userId = userService.verifyResetToken(token);
        if (userId != null) {
            model.addAttribute("userId", userId);
            model.addAttribute("token", token);
            return "user/reset_pw_form";
        }
        return "redirect:/user/login?error=invalid_token";
    }

    @PostMapping("/reset_pw_final")
    @ResponseBody
    public ResponseEntity<String> resetPwFinal(@RequestParam("token") String token,
                                               @RequestParam("userId") String userId,
                                               @RequestParam("newPassword") String newPassword) {
        boolean success = userService.resetPasswordWithToken(token, userId, newPassword);
        return success ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("fail");
    }
}