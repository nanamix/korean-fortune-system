package com.fortune.service;

import com.fortune.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 📧 이메일 발송 서비스
 * 
 * <p>운세 결과를 이메일로 발송하는 서비스입니다.</p>
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:fortune@jyha.net}")
    private String fromEmail;

    @Value("${app.fortune.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * 사주팔자 결과를 이메일로 발송
     * 
     * @param toEmail 수신자 이메일
     * @param sajuResult 사주팔자 결과
     * @param recipientName 수신자 이름
     */
    @Async
    public void sendSajuResult(String toEmail, SajuResult sajuResult, String recipientName) {
        if (!emailEnabled) {
            log.warn("이메일 발송이 비활성화되어 있습니다.");
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("recipientName", recipientName);
            model.put("sajuResult", sajuResult);
            model.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

            String subject = "🔮 " + recipientName + "님의 사주팔자 결과";
            String htmlContent = generateSajuEmailTemplate(model);

            sendEmail(toEmail, subject, htmlContent);
            log.info("사주팔자 결과 이메일 발송 완료: {}", toEmail);

        } catch (Exception e) {
            log.error("사주팔자 결과 이메일 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 일일운세 결과를 이메일로 발송
     * 
     * @param toEmail 수신자 이메일
     * @param dailyResult 일일운세 결과
     * @param recipientName 수신자 이름
     */
    @Async
    public void sendDailyFortune(String toEmail, DailyFortuneResult dailyResult, String recipientName) {
        if (!emailEnabled) {
            log.warn("이메일 발송이 비활성화되어 있습니다.");
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("recipientName", recipientName);
            model.put("dailyResult", dailyResult);
            model.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

            String subject = "📅 " + recipientName + "님의 " + dailyResult.getDate().format(DateTimeFormatter.ofPattern("MM월 dd일")) + " 운세";
            String htmlContent = generateDailyFortuneEmailTemplate(model);

            sendEmail(toEmail, subject, htmlContent);
            log.info("일일운세 결과 이메일 발송 완료: {}", toEmail);

        } catch (Exception e) {
            log.error("일일운세 결과 이메일 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 토정비결 결과를 이메일로 발송
     * 
     * @param toEmail 수신자 이메일
     * @param tojeongResult 토정비결 결과
     * @param recipientName 수신자 이름
     */
    @Async
    public void sendTojeongResult(String toEmail, TojeongResult tojeongResult, String recipientName) {
        if (!emailEnabled) {
            log.warn("이메일 발송이 비활성화되어 있습니다.");
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("recipientName", recipientName);
            model.put("tojeongResult", tojeongResult);
            model.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

            String subject = "📜 " + recipientName + "님의 " + tojeongResult.getTargetYear() + "년 토정비결";
            String htmlContent = generateTojeongEmailTemplate(model);

            sendEmail(toEmail, subject, htmlContent);
            log.info("토정비결 결과 이메일 발송 완료: {}", toEmail);

        } catch (Exception e) {
            log.error("토정비결 결과 이메일 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 별자리 운세 결과를 이메일로 발송
     * 
     * @param toEmail 수신자 이메일
     * @param zodiacResult 별자리 운세 결과
     * @param recipientName 수신자 이름
     */
    @Async
    public void sendZodiacFortune(String toEmail, ZodiacFortuneResult zodiacResult, String recipientName) {
        if (!emailEnabled) {
            log.warn("이메일 발송이 비활성화되어 있습니다.");
            return;
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("recipientName", recipientName);
            model.put("zodiacResult", zodiacResult);
            model.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

            String subject = "⭐ " + recipientName + "님의 " + zodiacResult.getZodiacKoreanName() + " 운세";
            String htmlContent = generateZodiacEmailTemplate(model);

            sendEmail(toEmail, subject, htmlContent);
            log.info("별자리 운세 결과 이메일 발송 완료: {}", toEmail);

        } catch (Exception e) {
            log.error("별자리 운세 결과 이메일 발송 실패: {}", toEmail, e);
        }
    }

    /**
     * 이메일 발송
     * 
     * @param toEmail 수신자 이메일
     * @param subject 제목
     * @param htmlContent HTML 내용
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * 사주팔자 이메일 템플릿 생성
     */
    private String generateSajuEmailTemplate(Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process("email/saju-result", context);
    }

    /**
     * 일일운세 이메일 템플릿 생성
     */
    private String generateDailyFortuneEmailTemplate(Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process("email/daily-fortune", context);
    }

    /**
     * 토정비결 이메일 템플릿 생성
     */
    private String generateTojeongEmailTemplate(Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process("email/tojeong-result", context);
    }

    /**
     * 별자리 운세 이메일 템플릿 생성
     */
    private String generateZodiacEmailTemplate(Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process("email/zodiac-fortune", context);
    }
} 