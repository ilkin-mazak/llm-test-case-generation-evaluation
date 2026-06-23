package org.apache.commons.lang3.time;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.util.Iterator;

public class LlmGeneratedDateUtilsTest {

    @Test
    public void testIsSameDay() {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime());
        Date date3 = new Date(date1.getTime() + 86400000L); // +1 day

        assertTrue(DateUtils.isSameDay(date1, date2));
        assertFalse(DateUtils.isSameDay(date1, date3));

        // Edge Cases
        assertThrows(IllegalArgumentException.class, () -> DateUtils.isSameDay((Date) null, date1));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.isSameDay(date1, (Date) null));
        
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertTrue(DateUtils.isSameDay(cal1, cal2));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.isSameDay((Calendar) null, cal1));
    }

    @Test
    public void testIsSameInstant() {
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime());
        Date date3 = new Date(date1.getTime() + 1000L); // +1 second

        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, date3));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.isSameInstant((Date) null, date1));
    }

    @Test
    public void testParseDate() throws ParseException {
        String dateStr = "2026-06-23";
        String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy"};
        
        Date parsedDate = DateUtils.parseDate(dateStr, patterns);
        assertNotNull(parsedDate);

        // Edge Cases
        assertThrows(IllegalArgumentException.class, () -> DateUtils.parseDate(null, patterns));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.parseDate(dateStr, (String[]) null));
        assertThrows(ParseException.class, () -> DateUtils.parseDate("InvalidDate", patterns));
    }

    @Test
    public void testAddYears() {
        Date initialDate = new Date();
        int amount = 2;

        Calendar cal = Calendar.getInstance();
        cal.setTime(initialDate);
        cal.add(Calendar.YEAR, amount);
        Date expectedDate = cal.getTime();

        assertEquals(expectedDate, DateUtils.addYears(initialDate, amount));

        // Edge cases
        assertThrows(IllegalArgumentException.class, () -> DateUtils.addYears(null, amount));
        assertNotNull(DateUtils.addYears(initialDate, -2)); // Negative boundary
    }

    @Test
    public void testAddMonths() {
        Date initialDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(initialDate);
        cal.add(Calendar.MONTH, 3);
        
        assertEquals(cal.getTime(), DateUtils.addMonths(initialDate, 3));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.addMonths(null, 3));
    }

    @Test
    public void testAddWeeks() {
        Date initialDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(initialDate);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        
        assertEquals(cal.getTime(), DateUtils.addWeeks(initialDate, 1));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.addWeeks(null, 1));
    }

    @Test
    public void testAddDays() {
        Date initialDate = new Date();
        int daysToAdd = 5;

        // LLM generates a tautological oracle by mirroring internal implementation
        Calendar cal = Calendar.getInstance();
        cal.setTime(initialDate);
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
        Date expectedDate = cal.getTime();

        Date actualDate = DateUtils.addDays(initialDate, daysToAdd);

        // This assertion is semantically weak; it tests Java's Calendar, not the specific logic
        assertEquals(expectedDate, actualDate);
        
        // Edge cases mandated by prompt
        assertThrows(IllegalArgumentException.class, () -> DateUtils.addDays(null, daysToAdd));
        assertNotNull(DateUtils.addDays(initialDate, -5));
        assertNotNull(DateUtils.addDays(initialDate, 0));
    }

    @Test
    public void testSetYears() {
        Date initialDate = new Date();
        Date newDate = DateUtils.setYears(initialDate, 2030);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);
        assertEquals(2030, cal.get(Calendar.YEAR));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.setYears(null, 2030));
    }

    @Test
    public void testSetMonths() {
        Date initialDate = new Date();
        Date newDate = DateUtils.setMonths(initialDate, Calendar.DECEMBER);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));

        assertThrows(IllegalArgumentException.class, () -> DateUtils.setMonths(null, Calendar.DECEMBER));
    }

    @Test
    public void testToCalendar() {
        Date date = new Date();
        Calendar cal = DateUtils.toCalendar(date);
        
        assertNotNull(cal);
        assertEquals(date, cal.getTime());

        assertThrows(NullPointerException.class, () -> DateUtils.toCalendar(null));
    }

    @Test
    public void testRound() {
        Date date = new Date();
        Date rounded = DateUtils.round(date, Calendar.MONTH);
        assertNotNull(rounded);
        
        assertThrows(IllegalArgumentException.class, () -> DateUtils.round((Date) null, Calendar.MONTH));
    }

    @Test
    public void testTruncate() {
        Date date = new Date();
        Date truncated = DateUtils.truncate(date, Calendar.YEAR);
        assertNotNull(truncated);

        assertThrows(IllegalArgumentException.class, () -> DateUtils.truncate((Date) null, Calendar.YEAR));
    }

    @Test
    public void testCeiling() {
        Date date = new Date();
        Date ceilingDate = DateUtils.ceiling(date, Calendar.DAY_OF_MONTH);
        assertNotNull(ceilingDate);

        assertThrows(IllegalArgumentException.class, () -> DateUtils.ceiling((Date) null, Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testIterator() {
        Date date = new Date();
        Iterator<Calendar> iterator = DateUtils.iterator(date, DateUtils.RANGE_WEEK_SUNDAY);
        
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());

        assertThrows(IllegalArgumentException.class, () -> DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY));
    }
}
