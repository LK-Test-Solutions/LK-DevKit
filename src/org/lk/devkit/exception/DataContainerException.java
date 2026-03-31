package org.lk.devkit.exception;

@SuppressWarnings("serial")
public class DataContainerException extends RuntimeException {

    public DataContainerException(Exception e) {
        super(e);
    }

    public DataContainerException(String message) {
        super(message);
    }
}
