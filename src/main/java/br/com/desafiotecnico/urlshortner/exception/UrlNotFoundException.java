package br.com.desafiotecnico.urlshortner.exception;

public class UrlNotFoundException extends RuntimeException{

    public UrlNotFoundException(String message) {
        super(message);
    }
}
