package com.shiroTest.function.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        // 创建一个简单的邮件消息对象
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);                  // 收件人
        message.setSubject(subject);        // 邮件主题
        message.setText(text);              // 邮件正文
        message.setFrom("mailsenderblogweb@gmail.com");  // 发件人，必须和配置中的username相同

        // 发送邮件
        mailSender.send(message);
    }
}

