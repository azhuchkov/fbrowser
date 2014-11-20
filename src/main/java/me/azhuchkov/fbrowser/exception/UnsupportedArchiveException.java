package me.azhuchkov.fbrowser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates unsupported archive format. Can be also raised in a case of broken archive.
 *
 * @author Andrey Zhuchkov
 *         Date: 20.11.14
 */
@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class UnsupportedArchiveException extends RuntimeException {
    public UnsupportedArchiveException(String message) {
        super(message);
    }

    public UnsupportedArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
