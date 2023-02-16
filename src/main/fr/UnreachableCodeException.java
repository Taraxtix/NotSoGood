package main.fr;

public class UnreachableCodeException extends Throwable {
    public UnreachableCodeException(){
        super("Accessing unreachable code !");
    }
}
