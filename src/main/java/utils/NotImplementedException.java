package utils;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
        super("SQL NotImplemented Exception: Some Executions have not been implemented yet.");
    }

    public NotImplementedException(String text) {
        super("SQL NotImplemented Exception: " + text + " has not been implemented yet.");
    }
}
