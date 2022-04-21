package utils;

public class SyntaxException extends RuntimeException{
    private final String text;

    public SyntaxException(String text) {
        this.text = text;
    }
    public void show() {
        System.out.println("SQL Query Error: " + this.text);
    }
}
