package org.phoebus.olog.notification;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
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
import java.util.List;
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
        content.append("\n\nDate: ").append(logEntry.getCreatedDate());
        content.append("\n\nSummary: ").append(logEntry.getTitle());
        content.append("\n\nDetails: \n\n").append(logEntry.getSource());
        content.append("\n\nView this log at [").append(URL).append(logEntry.getId()).append("](").append(URL).append(logEntry.getId()).append(")");

        final Email email = builder
                .from("o-log@lightsource.ca")
                .withSubject("Olog Entry: " + logEntry.getTitle())
                .withHTMLText(convertMarkdownToHtml(content.toString()))
                .buildEmail();


        logger.log(Level.INFO, "Sending email");
        mailer.sendMail(email);
        logger.log(Level.INFO, "Email sent");
    }

    public static String convertMarkdownToHtml(String md) {
        List<Extension> extensions = List.of(TablesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

        Node document = parser.parse(md);
        return renderer.render(document);
    }
}
