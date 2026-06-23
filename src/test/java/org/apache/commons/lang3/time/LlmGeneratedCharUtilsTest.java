package org.apache.commons.lang3;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LlmGeneratedCharUtilsTest {

    @Test
    public void testToCharacterObject() {
        assertEquals(Character.valueOf('a'), CharUtils.toCharacterObject('a'));
        assertEquals(Character.valueOf('A'), CharUtils.toCharacterObject('A'));
        assertEquals(Character.valueOf('0'), CharUtils.toCharacterObject('0'));
        
        // String inputs
        assertEquals(Character.valueOf('a'), CharUtils.toCharacterObject("a"));
        assertNull(CharUtils.toCharacterObject(""));
        assertNull(CharUtils.toCharacterObject((String) null));
    }

    @Test
    public void testToChar() {
        assertEquals('A', CharUtils.toChar(Character.valueOf('A')));
        assertEquals('A', CharUtils.toChar(Character.valueOf('A'), 'X'));
        assertEquals('X', CharUtils.toChar((Character) null, 'X'));
        
        assertThrows(IllegalArgumentException.class, () -> CharUtils.toChar((Character) null));
        
        // String inputs
        assertEquals('A', CharUtils.toChar("A"));
        assertEquals('A', CharUtils.toChar("A", 'X'));
        assertEquals('X', CharUtils.toChar("", 'X'));
        assertEquals('X', CharUtils.toChar((String) null, 'X'));
        
        assertThrows(IllegalArgumentException.class, () -> CharUtils.toChar(""));
        assertThrows(IllegalArgumentException.class, () -> CharUtils.toChar((String) null));
    }

    @Test
    public void testToIntValue() {
        assertEquals(0, CharUtils.toIntValue('0'));
        assertEquals(9, CharUtils.toIntValue('9'));
        assertThrows(IllegalArgumentException.class, () -> CharUtils.toIntValue('A'));
        
        assertEquals(0, CharUtils.toIntValue('0', -1));
        assertEquals(-1, CharUtils.toIntValue('A', -1));
        
        assertEquals(0, CharUtils.toIntValue(Character.valueOf('0')));
        assertThrows(IllegalArgumentException.class, () -> CharUtils.toIntValue((Character) null));
        
        assertEquals(0, CharUtils.toIntValue(Character.valueOf('0'), -1));
        assertEquals(-1, CharUtils.toIntValue((Character) null, -1));
    }

    @Test
    public void testToString() {
        assertEquals("a", CharUtils.toString('a'));
        assertEquals("a", CharUtils.toString(Character.valueOf('a')));
        assertNull(CharUtils.toString((Character) null));
    }

    @Test
    public void testUnicodeEscaped() {
        assertEquals("\\u0041", CharUtils.unicodeEscaped('A'));
        assertEquals("\\u0041", CharUtils.unicodeEscaped(Character.valueOf('A')));
        assertNull(CharUtils.unicodeEscaped((Character) null));
        
        // Boundary explicit casting rule applied
        assertEquals("\\u0080", CharUtils.unicodeEscaped((char) 128));
        assertEquals("\\u0000", CharUtils.unicodeEscaped((char) 0));
    }

    @Test
    public void testIsAscii() {
        assertTrue(CharUtils.isAscii('a'));
        assertTrue(CharUtils.isAscii('A'));
        assertTrue(CharUtils.isAscii('3'));
        assertTrue(CharUtils.isAscii('-'));
        assertTrue(CharUtils.isAscii('\n'));
        assertFalse(CharUtils.isAscii((char) 128));
        assertFalse(CharUtils.isAscii((char) 255));
    }

    @Test
    public void testIsAsciiPrintable() {
        assertTrue(CharUtils.isAsciiPrintable('a'));
        assertTrue(CharUtils.isAsciiPrintable('A'));
        assertTrue(CharUtils.isAsciiPrintable('3'));
        assertTrue(CharUtils.isAsciiPrintable('-'));
        
        // Edge cases and boundaries
        assertFalse(CharUtils.isAsciiPrintable('\n'));
        assertFalse(CharUtils.isAsciiPrintable((char) 31)); // control char
        assertTrue(CharUtils.isAsciiPrintable((char) 32));  // space
        assertTrue(CharUtils.isAsciiPrintable((char) 126)); // tilde
        assertFalse(CharUtils.isAsciiPrintable((char) 127)); // DEL
        assertFalse(CharUtils.isAsciiPrintable((char) 128));
    }

    @Test
    public void testIsAsciiControl() {
        assertFalse(CharUtils.isAsciiControl('a'));
        assertTrue(CharUtils.isAsciiControl('\n'));
        assertTrue(CharUtils.isAsciiControl((char) 31));
        assertFalse(CharUtils.isAsciiControl((char) 32));
        assertTrue(CharUtils.isAsciiControl((char) 127));
        assertFalse(CharUtils.isAsciiControl((char) 128));
    }

    @Test
    public void testIsAsciiAlpha() {
        assertTrue(CharUtils.isAsciiAlpha('a'));
        assertTrue(CharUtils.isAsciiAlpha('A'));
        assertFalse(CharUtils.isAsciiAlpha('3'));
        assertFalse(CharUtils.isAsciiAlpha('-'));
        assertFalse(CharUtils.isAsciiAlpha('\n'));
        assertFalse(CharUtils.isAsciiAlpha((char) 128));
    }

    @Test
    public void testIsAsciiAlphaUpper() {
        assertFalse(CharUtils.isAsciiAlphaUpper('a'));
        assertTrue(CharUtils.isAsciiAlphaUpper('A'));
        assertFalse(CharUtils.isAsciiAlphaUpper('3'));
        assertFalse(CharUtils.isAsciiAlphaUpper('-'));
        assertFalse(CharUtils.isAsciiAlphaUpper((char) 128));
    }

    @Test
    public void testIsAsciiAlphaLower() {
        assertTrue(CharUtils.isAsciiAlphaLower('a'));
        assertFalse(CharUtils.isAsciiAlphaLower('A'));
        assertFalse(CharUtils.isAsciiAlphaLower('3'));
        assertFalse(CharUtils.isAsciiAlphaLower('-'));
        assertFalse(CharUtils.isAsciiAlphaLower((char) 128));
    }

    @Test
    public void testIsAsciiNumeric() {
        assertFalse(CharUtils.isAsciiNumeric('a'));
        assertFalse(CharUtils.isAsciiNumeric('A'));
        assertTrue(CharUtils.isAsciiNumeric('3'));
        assertTrue(CharUtils.isAsciiNumeric('0'));
        assertTrue(CharUtils.isAsciiNumeric('9'));
        assertFalse(CharUtils.isAsciiNumeric('-'));
        assertFalse(CharUtils.isAsciiNumeric((char) 128));
    }

    @Test
    public void testIsAsciiAlphanumeric() {
        assertTrue(CharUtils.isAsciiAlphanumeric('a'));
        assertTrue(CharUtils.isAsciiAlphanumeric('A'));
        assertTrue(CharUtils.isAsciiAlphanumeric('3'));
        assertFalse(CharUtils.isAsciiAlphanumeric('-'));
        assertFalse(CharUtils.isAsciiAlphanumeric('\n'));
        assertFalse(CharUtils.isAsciiAlphanumeric((char) 128));
    }

    @Test
    public void testCompare() {
        assertEquals(0, CharUtils.compare('a', 'a'));
        assertTrue(CharUtils.compare('a', 'b') < 0);
        assertTrue(CharUtils.compare('b', 'a') > 0);
        
        // Edge cases
        assertTrue(CharUtils.compare((char) 0, (char) 128) < 0);
        assertEquals(0, CharUtils.compare((char) 128, (char) 128));
    }
}
