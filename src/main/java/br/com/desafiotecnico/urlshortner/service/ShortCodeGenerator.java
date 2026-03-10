package br.com.desafiotecnico.urlshortner.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class ShortCodeGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final SecureRandom random = new SecureRandom();


    public String generate(int length){
        StringBuilder sb = new StringBuilder(length);
        for(int i =0; i < length; i++){
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return  sb.toString();
    }
}
