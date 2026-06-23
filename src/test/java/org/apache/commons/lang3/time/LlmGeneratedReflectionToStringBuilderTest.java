package org.apache.commons.lang3.builder;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class LlmGeneratedReflectionToStringBuilderTest {

    // The LLM generated a dummy class to safely test reflection boundaries
    public static class DummyObject {
        public int publicField = 42;
        private String privateField = "secret";
        transient double transientField = 3.14;
    }

    @Test
    public void testToStringMethods() {
        DummyObject dummy = new DummyObject();
        String result = ReflectionToStringBuilder.toString(dummy);
        
        assertNotNull(result);
        assertTrue(result.contains("publicField=42"));
        assertTrue(result.contains("privateField=secret"));
        assertFalse(result.contains("transientField=3.14")); // Transients are excluded by default

        // Edge cases required by prompt
        assertEquals("<null>", ReflectionToStringBuilder.toString(null));
    }

    @Test
    public void testConstructors() {
        DummyObject dummy = new DummyObject();
        
        ReflectionToStringBuilder builder1 = new ReflectionToStringBuilder(dummy);
        assertNotNull(builder1.toString());

        ReflectionToStringBuilder builder2 = new ReflectionToStringBuilder(dummy, ToStringStyle.DEFAULT_STYLE);
        assertNotNull(builder2.toString());

        ReflectionToStringBuilder builder3 = new ReflectionToStringBuilder(dummy, ToStringStyle.DEFAULT_STYLE, new StringBuffer());
        assertNotNull(builder3.toString());

        // Null handling in constructor (Boundary rule)
        assertThrows(IllegalArgumentException.class, () -> new ReflectionToStringBuilder(null));
    }

    @Test
    public void testExcludeFieldNames() {
        DummyObject dummy = new DummyObject();
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(dummy);
        
        builder.setExcludeFieldNames("publicField");
        String[] excluded = builder.getExcludeFieldNames();
        assertNotNull(excluded);
        assertEquals(1, excluded.length);
        assertEquals("publicField", excluded[0]);

        String result = builder.toString();
        assertFalse(result.contains("publicField=42"));
        assertTrue(result.contains("privateField=secret"));

        // Edge case: null array casting
        builder.setExcludeFieldNames((String[]) null);
        assertArrayEquals(new String[0], builder.getExcludeFieldNames());
    }

    @Test
    public void testExcludeNullValues() {
        DummyObject dummy = new DummyObject();
        dummy.privateField = null; // Explicit null state

        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(dummy);
        assertFalse(builder.isExcludeNullValues()); // Default state

        builder.setExcludeNullValues(true);
        assertTrue(builder.isExcludeNullValues());

        String result = builder.toString();
        assertFalse(result.contains("privateField=<null>"));
    }

    @Test
    public void testAppendTransients() {
        DummyObject dummy = new DummyObject();
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(dummy);
        
        assertFalse(builder.isAppendTransients()); // Default state
        builder.setAppendTransients(true);
        assertTrue(builder.isAppendTransients());

        String result = builder.toString();
        assertTrue(result.contains("transientField=3.14"));
    }

    @Test
    public void testAppendStatics() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(new DummyObject());
        assertFalse(builder.isAppendStatics()); 
        
        builder.setAppendStatics(true);
        assertTrue(builder.isAppendStatics());
    }

    @Test
    public void testUpToClass() {
        DummyObject dummy = new DummyObject();
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(dummy);
        
        builder.setUpToClass(Object.class);
        assertEquals(Object.class, builder.getUpToClass());

        // Edge case
        builder.setUpToClass(null);
        assertNull(builder.getUpToClass());
    }

    @Test
    public void testAccept() throws NoSuchFieldException {
        DummyObject dummy = new DummyObject();
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(dummy);
        
        Field publicField = DummyObject.class.getDeclaredField("publicField");
        Field transientField = DummyObject.class.getDeclaredField("transientField");

        assertTrue(builder.accept(publicField));
        assertFalse(builder.accept(transientField)); // Excluded by default

        builder.setAppendTransients(true);
        assertTrue(builder.accept(transientField));
    }

    @Test
    public void testGetValue() throws Exception {
        DummyObject dummy = new DummyObject();
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(dummy);
        
        Field publicField = DummyObject.class.getDeclaredField("publicField");
        Object value = builder.getValue(publicField);
        
        assertEquals(42, value);
        
        // Edge case: null field
        assertThrows(IllegalArgumentException.class, () -> builder.getValue(null));
    }
    
    @Test
    public void testToStringExcludes() {
        DummyObject dummy = new DummyObject();
        Collection<String> excludes = new ArrayList<>();
        excludes.add("publicField");
        
        String result = ReflectionToStringBuilder.toStringExclude(dummy, excludes);
        assertFalse(result.contains("publicField=42"));
        
        String resultVarargs = ReflectionToStringBuilder.toStringExclude(dummy, "privateField");
        assertFalse(resultVarargs.contains("privateField=secret"));
        
        // Edge cases
        assertEquals("<null>", ReflectionToStringBuilder.toStringExclude(null, excludes));
        assertNotNull(ReflectionToStringBuilder.toStringExclude(dummy, (Collection<String>) null));
        assertNotNull(ReflectionToStringBuilder.toStringExclude(dummy, (String[]) null));
    }
}
