package com.finalProject.ali.common.logaspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class LogAspect {

    // com.finalProject.ali 패키지 아래의 모든 Controller를 감시하겠다!
    @Around("execution(* com.finalProject.ali..*Controller.*(..))")
    public Object logging(ProceedingJoinPoint pjp) throws Throwable {

        // 1. 요청 정보 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String methodName = pjp.getSignature().getName();

        // 2. 시작 로그
        log.info("👉 [START] {} {} | Method: {}", method, uri, methodName);

        long startTime = System.currentTimeMillis();

        // 3. 실제 메소드 실행
        Object result = pjp.proceed();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 4. 종료 로그 (실행 시간 포함)
        log.info("👈 [END] {} {} | Time: {}ms", method, uri, executionTime);

        return result;
    }
}