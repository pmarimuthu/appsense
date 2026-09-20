package com.maybank.appsense.core.event;

import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import java.util.List;

public record NoticeEvent(MessageKey key, List<Object> args, Severity severity) implements AppEvent {

    public enum Severity {
        INFO, WARNING, ERROR
    }

    public static NoticeEvent info(MessageKey key, Object... args) {
        return new NoticeEvent(key, List.of(args), Severity.INFO);
    }

    public static NoticeEvent error(AppSenseException exception) {
        return new NoticeEvent(exception.getMessageKey(), List.of(exception.getArgs()), Severity.ERROR);
    }

    public String text() {
        return MessageUtil.get(key, args.toArray());
    }
}
