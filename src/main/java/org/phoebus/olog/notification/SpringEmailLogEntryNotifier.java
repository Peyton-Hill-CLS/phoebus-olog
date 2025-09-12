package org.phoebus.olog.notification;

import jakarta.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@Component
public class SpringEmailLogEntryNotifier {
    private final Logger logger = Logger.getLogger(EmailLogEntryNotifier.class.getName());

    public Mailer mailer;

    public final Parser parser;
    public final HtmlRenderer renderer;

    @Value("${email.url}")
    public String url;
    @Value("${email.host}")
    public String host;
    @Value("${email.port:25}")
    public int port;
    @Value("${email.address}")
    public String address;

    public SpringEmailLogEntryNotifier() {
        List<Extension> extensions = List.of(TablesExtension.create());
        parser = Parser.builder().extensions(extensions).build();
        renderer = HtmlRenderer.builder().extensions(extensions).build();

        logger.log(Level.INFO, "Starting email notifier");
    }

    @PostConstruct
    public void initMailer() {
        logger.info("Initializing mailer with host: " + host + ", port: " + port + " address: " + address);

        mailer = MailerBuilder
                .withSMTPServer(host, port)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();

        System.setProperty("mail.smtp.starttls.enable", "false");
        System.setProperty("mail.smtp.ssl.trust", host);
    }

    public void notify(Log logEntry) {
        if (logEntry.getForwardTo().isEmpty()) {
            return;
        }
        EmailPopulatingBuilder builder = EmailBuilder.startingBlank();

        for (String s : logEntry.getForwardTo()) {
            builder = builder.to(s);
        }

        StringBuilder content = new StringBuilder();
        content.append("User: ").append(logEntry.getOwner());
        content.append("\\\nDate: ").append(logEntry.getCreatedDate());
        content.append("\\\nSummary: ").append(logEntry.getTitle());
        content.append("\\\nDetails: \n\n").append(logEntry.getSource());
        content.append("\n\nView this log at: [").append(url).append(logEntry.getId()).append("](").append(url).append(logEntry.getId()).append(")");

        final Email email = builder
                .from(address)
                .withSubject("Olog Entry: " + logEntry.getTitle())
                .withHTMLText(convertMarkdownToHtml(content.toString()))
                .buildEmail();


        logger.log(Level.INFO, "Sending email");
        mailer.sendMail(email);
        logger.log(Level.INFO, "Email sent");
    }

    public String convertMarkdownToHtml(String md) {
        Node document = parser.parse(md);
        return renderer.render(document);
    }
}
