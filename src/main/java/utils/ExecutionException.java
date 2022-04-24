package utils;

public class ExecutionException extends RuntimeException{
    public ExecutionException(){
        super("SQL execution exception.");
    }
    public ExecutionException(String name) {
        super("SQL execution exception: " + name +".");
    }
}
