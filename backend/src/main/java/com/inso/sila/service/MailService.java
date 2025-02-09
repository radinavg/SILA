package com.inso.sila.service;

public interface MailService {

    /**
     * Sends user an email with new password when password is reset.
     *
     * @param email user's email address to which new password is sent.
     * @param name user's first and last name
     * @param password new password
     * */
    void sendResetPasswordMail(String email, String name, String password);


    /**
     * Sends user an email if user has been blocked due to too many login attempts.
     *
     * @param email user's email address to which new password is sent.
     * @param name user's first and last name
     * */
    void sendUserBlockedEmail(String email, String name);

    /**
     * Sends user an email if user has been unblocked.
     *
     * @param email user's email address to which new password is sent.
     * @param name user's first and last name
     * */
    void sendUserUnblockedEmail(String email, String name);

    /**
     * Sends user an email to the studio admin once the studio has been approved to go live on Sila.
     *
     * @param email studio admins email address to which approval email is sent
     * @param name studio name
     * */
    void sendApprovalEmailToStudioAdmin(String email, String name);
}
