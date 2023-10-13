package edu.internet2.middleware.grouper.plugins;

public class GrouperPluginException extends RuntimeException{
    public GrouperPluginException() {
    }

    public GrouperPluginException(String message) {
        super(message);
    }

    public GrouperPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrouperPluginException(Throwable cause) {
        super(cause);
    }

    public GrouperPluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
