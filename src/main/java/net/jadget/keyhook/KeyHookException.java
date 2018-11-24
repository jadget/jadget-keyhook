package net.jadget.keyhook;


public class KeyHookException extends RuntimeException {
    public KeyHookException() {
    }

    public KeyHookException(String message) {
        super(message);
    }

    public KeyHookException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyHookException(Throwable cause) {
        super(cause);
    }
}
