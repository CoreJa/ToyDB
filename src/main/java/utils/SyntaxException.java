package utils;

public class SyntaxException extends RuntimeException{
    public SyntaxException(){
        super("SQL Query Error.");
    }
    public SyntaxException(String text) {
        super("SQL Query Error: " + text + ".");
    }

}
