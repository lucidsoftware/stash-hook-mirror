package com.englishtown.bitbucket.hook;

import com.atlassian.bitbucket.scm.CommandErrorHandler;
import com.atlassian.bitbucket.scm.CommandExitHandler;
import com.atlassian.bitbucket.scm.CommandOutputHandler;
import com.atlassian.bitbucket.io.StringOutputHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Handles removing passwords from output text
 */
class PasswordHandler extends StringOutputHandler
        implements CommandOutputHandler<String>, CommandErrorHandler, CommandExitHandler {

    private final String target;
    private final CommandExitHandler exitHandler;

    private static final String PASSWORD_REPLACEMENT = ":*****@";

    public PasswordHandler(String password, CommandExitHandler exitHandler) {
        this.exitHandler = exitHandler;
        this.target = ":" + password + "@";
    }

    @Nonnull
    public String cleanText(String text) {
        if (text == null) {
           return "";
        } else if(text.isEmpty()) {
            return text;
        }
        return text.replace(target, PASSWORD_REPLACEMENT);
    }

    @Nonnull
    @Override
    public String getOutput() {
        return cleanText(super.getOutput());
    }

    @Override
    public void onCancel(@Nonnull String command, int exitCode, @Nullable String stdErr, @Nullable Throwable thrown) {
        exitHandler.onCancel(cleanText(command), exitCode, cleanText(stdErr), getPasswordSafeException(thrown));
    }

    @Override
    public void onExit(@Nonnull String command, int exitCode, @Nullable String stdErr, @Nullable Throwable thrown) {
        exitHandler.onExit(cleanText(command), exitCode, cleanText(stdErr), getPasswordSafeException(thrown));
    }

    class PasswordSafeException extends Throwable {
        public PasswordSafeException(Throwable thrown) {
            super("Wrapping " + thrown.getClass().getSimpleName() + ": " + cleanText(thrown.getMessage()));
            setStackTrace(thrown.getStackTrace());
        }
    }

    @Nullable
    private PasswordSafeException getPasswordSafeException(@Nullable Throwable thrown) {
        PasswordSafeException exception = null;
        if (thrown != null) {
            exception = new PasswordSafeException(thrown);
        }
        return exception;
    }
}

