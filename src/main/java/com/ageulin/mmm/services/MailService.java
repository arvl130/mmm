package com.ageulin.mmm.services;

import com.ageulin.mmm.utils.EnvironmentVariableUtils;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final Resend client;
    private final String fromURL;

    public MailService() {
        this.fromURL = EnvironmentVariableUtils
            .getenvOrFail("MAIL_FROM_URL");

        var apiKey = EnvironmentVariableUtils
            .getenvOrFail("RESEND_API_KEY");
        this.client = new Resend(apiKey);
    }

    public void sendPlainTextMail(
        String subject,
        String plainTextBody,
        String ...recipients
    ) throws ResendException {
        var mailFromURL = "MMM <mmm@"  + this.fromURL + ">";
        var emailOptions = CreateEmailOptions.builder()
            .from(mailFromURL)
            .to(recipients)
            .subject(subject)
            .text(plainTextBody)
            .build();

        client.emails().send(emailOptions);
    }
}
