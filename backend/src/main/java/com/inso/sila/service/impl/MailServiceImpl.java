package com.inso.sila.service.impl;

import com.inso.sila.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.invoke.MethodHandles;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String SILA_EMAIL = "sila.fitness.app@gmail.com";


    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendResetPasswordMail(String email, String name, String password) {
        LOG.trace("sendingResetPasswordMail({}, {}, {})", email, name, password);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Sila Fitness & Health: Password Reset");

        String emailText = String.format("""
            Hello %s,
    
            We have received a request to change the password for your account on Sila Fitness App.
            If you haven't made this request, please ignore this message.
    
            Here are your updated login details:
            Email: %s
            Temporary Password: %s
    
            Please log in and change your password immediately to ensure the security of your account.
    
            If you have any questions or need further assistance, our support team is here to help.
    
            Best regards,
            Sila Fitness & Health Team
            """, name, email, password);


        message.setText(emailText);
        message.setFrom(SILA_EMAIL);
        mailSender.send(message);
    }

    @Override
    public void sendUserBlockedEmail(String email, String name) {
        LOG.trace("sendUserBlockedEmail({}, {})", email, name);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Sila Fitness & Health: You are Blocked!");

        String emailText = String.format("""
            Hello %s,
           \s
            We have detected multiple failed login attempts into your account.
            For security reasons, we have temporarily blocked your account.
           \s
            If this was you, please contact us by replying to this email, and one of our administrators\s
            will assist you in unblocking your account manually. Alternatively, you can use the "Forgot Password"\s
            option on the login page. This will allow you to reset your password and regain access to your account.
           \s
            If you choose to reset your password, you will receive an email with a temporary password.\s
            Please make sure to log in using the temporary password and change it immediately for security reasons.
           \s
            If you have any questions or need further assistance, feel free to contact our support team.\s
            We are here to help.
           \s
            Best regards,
            The Sila Fitness & Health Team
           \s""", name);

        message.setText(emailText);
        message.setFrom(SILA_EMAIL);
        mailSender.send(message);

    }

    @Override
    public void sendUserUnblockedEmail(String email, String name) {
        LOG.trace("sendUserUnblockedEmail({}, {})", email, name);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Sila Fitness & Health: Your account has been unblocked!");

        String emailText = String.format("""
            Hello %s,
            
            We are happy to inform you that your account on the Sila Fitness App has been successfully unblocked.
            You can now log in using your credentials and continue enjoying our services.
            
            If you requested password request upon receiving this email, please use password we sent you
            to log in and change it as soon as possible. If this is not the case, you can login with your old credentials.
            
            If you experience any issues or need assistance, please don’t hesitate to contact our support team.
            
            Best regards,
            The Sila Fitness & Health Team
            """, name);



        message.setText(emailText);
        message.setFrom(SILA_EMAIL);
        mailSender.send(message);
    }

    @Override
    public void sendApprovalEmailToStudioAdmin(String email, String name) {
        LOG.trace("sendApprovalEmailToStudioAdmin({}, {})", email, name);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Sila Fitness & Health: Your Studio is live on Sila!");

        String emailText = String.format("""
            Hello %s,
        
            We are delighted to inform you that your studio has been successfully published on our platform!
        
            You can now log in to your studio account using the credentials you provided when submitting the studio registration form.
        
            Once logged in, feel free to add new activities to your studio and customize your page as needed.
        
            If you have any questions or need assistance, please don't hesitate to contact us.
            We are here to help!
        
            Best regards,
            The Sila Fitness & Health Team
            """, name);



        message.setText(emailText);
        message.setFrom(SILA_EMAIL);
        mailSender.send(message);
    }

}
