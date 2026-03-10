package br.com.desafiotecnico.urlshortner.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void shouldGenerateCodeWithCorrectLength() {
        String code = generator.generate(8);

        assertNotNull(code);
        assertEquals(8, code.length());
    }

    @Test
    void shouldGenerateCodeUsingOnlyAllowedCharacters() {
        String code = generator.generate(20);
        assertTrue(code.matches("[0-9A-Za-z]+"));
    }
}