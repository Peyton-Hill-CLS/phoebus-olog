package org.phoebus.olog.notification;

import org.phoebus.olog.Application;
import org.phoebus.olog.entity.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class EmailLogEntryNotifier implements LogEntryNotifier {

    private final SpringEmailLogEntryNotifier child;

    public EmailLogEntryNotifier() {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        this.child = context.getBean(SpringEmailLogEntryNotifier.class);
    }

    @Override
    public void notify(Log logEntry) {
        child.notify(logEntry);
    }
}
