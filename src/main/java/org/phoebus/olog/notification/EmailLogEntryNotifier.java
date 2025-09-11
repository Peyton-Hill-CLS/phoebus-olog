package org.phoebus.olog.notification;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.phoebus.olog.entity.Log;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailLogEntryNotifier implements LogEntryNotifier {

    private final Logger logger = Logger.getLogger(EmailLogEntryNotifier.class.getName());

    public Mailer mailer;
    public String URL = "https://svt-olog01.clsi.ca/logs/";

    public EmailLogEntryNotifier() {
        mailer = MailerBuilder
                .withSMTPServer("mail.clsi.ca", 25)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();

        System.setProperty("mail.smtp.starttls.enable", "false");
        System.setProperty("mail.smtp.ssl.trust", "mail.clsi.ca");

        logger.log(Level.INFO, "Starting email notifier");
    }

    @Override
    public void notify(Log logEntry) {
        if(logEntry.getForwardTo().isEmpty()) {
            return;
        }
        EmailPopulatingBuilder builder = EmailBuilder.startingBlank();

        for(String s : logEntry.getForwardTo()) {
            builder = builder.to(s);
        }

        StringBuilder content = new StringBuilder();
        content.append("User: ").append(logEntry.getOwner());
        content.append("\nDate: ").append(logEntry.getCreatedDate());
        content.append("\nSummary: ").append(logEntry.getTitle());
        content.append("\nDetails: \n").append(logEntry.getDescription());

        final Email email = builder
                .from("e-log@lightsource.ca")
                .withSubject("Olog Entry: " + logEntry.getTitle())
                .withHTMLText(convertMarkdownToHtml(content.toString()))
                .buildEmail();


        logger.log(Level.INFO, "Sending email");
        mailer.sendMail(email);
        logger.log(Level.INFO, "Email sent");
    }

    public static String convertMarkdownToHtml(String md) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(md);
        return HtmlRenderer.builder().build().render(document);
    }
}
