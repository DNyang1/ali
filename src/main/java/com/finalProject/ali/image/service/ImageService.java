package com.finalProject.ali.image.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {

    // 실제 파일이 물리적으로 저장될 루트 경로
    private final String rootPath = "D:/upload/ali_uploads/";

    /**
     * @param file 업로드된 파일 객체
     * @param category 폴더 구분 (profiles, products 등)
     * @return DB에 저장할 웹 경로 문자열
     */
    public String uploadImage(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) return null;

        // 1. 저장할 폴더 생성 (C:/upload/ali_uploads/profiles/)
        String saveDir = rootPath + category + "/";
        File folder = new File(saveDir);
        if (!folder.exists()) folder.mkdirs();

        // 2. 파일명 중복 방지 (UUID 사용)
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String savedName = UUID.randomUUID().toString() + extension;

        // 3. 실제 하드디스크에 파일 저장
        try {
            file.transferTo(new File(saveDir + savedName));

            // 4. 브라우저에서 접근 가능한 웹 경로 반환 (/upload/profiles/uuid.jpg)
            return "/upload/" + category + "/" + savedName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteActualFile(String webPath) {
        if (webPath == null || webPath.isEmpty()) return;
        // 1. 웹 경로(/upload/...)를 물리적 경로(C:/upload/ali_uploads/...)로 변환
        // "/upload/" 문자열을 제거하고 rootPath와 합침
        String relativePath = webPath.replace("/upload/", "");
        File file = new File(rootPath + relativePath);

        // 2. 파일이 존재하면 삭제
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("파일 삭제 성공: " + file.getPath());
            } else {
                System.out.println("파일 삭제 실패: " + file.getPath());
            }
        }
    }
}