package com.finalProject.ali.image.service;

import org.springframework.beans.factory.annotation.Value; // lombok.Value가 아닌 이 패키지여야 합니다!
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    // 1. properties에서 경로 주입 (하드코딩 제거)
    @Value("${file.upload.path}")
    private String uploadPath;

    public String uploadImage(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) return null;

        // 2. 물리적 저장 경로 생성 (예: D:/upload/ali_uploads/profiles/)
        String fullPath = uploadPath + folderName + "/";
        File folder = new File(fullPath);
        if (!folder.exists()) folder.mkdirs();

        // 3. 파일명 중복 방지
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String savedName = UUID.randomUUID().toString() + extension;

        try {
            // 4. 실제 파일 저장
            file.transferTo(new File(fullPath + savedName));

            // 5. DB에 저장할 웹 경로 반환 (예: /upload/profiles/uuid.jpg)
            return "/upload/" + folderName + "/" + savedName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteActualFile(String webPath) {
        if (webPath == null || webPath.isEmpty()) return;

        // 6. 웹 경로를 물리 경로로 변환하여 삭제
        String relativePath = webPath.replace("/upload/", "");
        File file = new File(uploadPath + relativePath);

        if (file.exists()) {
            file.delete();
        }
    }
}