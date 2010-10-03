package com.georgecalm.sprockets;

import static com.georgecalm.sprockets.Helper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SourceLineTest {
    @Test
    public void test_line_that_begins_with_double_slash_should_be_a_comment() {
        assertTrue(getSourceLine("//").isComment());
        assertTrue(getSourceLine("//test").isComment());
        assertTrue(getSourceLine("//= require").isComment());
        assertTrue(getSourceLine("//= require <foo>").isComment());
        assertTrue(getSourceLine(" //").isComment());
        assertTrue(getSourceLine("\t//").isComment());
        assertTrue(getSourceLine("//= require <foo>").isComment());
        assertFalse(getSourceLine("/loremipsum/").isComment());
    }
    
    @Test
    public void test_line_that_begins_a_multiline_comment() {
        assertTrue(getSourceLine(" /*").isBeginsMultilineComment());
        assertTrue(getSourceLine(" /**").isBeginsMultilineComment());
        assertFalse(getSourceLine("*/").isBeginsMultilineComment());
    }
    
    @Test
    public void test_line_that_begins_a_pdoc_comment() {
        assertFalse(getSourceLine(" */").isBeginsPdocComment());
        assertTrue(getSourceLine(" /**").isBeginsPdocComment());
    }
    
    @Test
    public void test_line_that_ends_a_multiline_comment() {
        assertTrue(getSourceLine(" */").isEndsMultilineComment());
        assertTrue(getSourceLine(" */").isEndsMultilineComment());
        assertFalse(getSourceLine("/*").isEndsMultilineComment());
    }
    
    @Test
    public void test_line_that_ends_a_pdoc_comment() {
        assertFalse(getSourceLine(" */").isEndsPdocComment());
        assertTrue(getSourceLine(" **/").isEndsPdocComment());
    }
    
    @Test
    public void test_line_that_contains_but_does_not_begin_with_double_slash_should_not_be_a_comment() {
        assertFalse(getSourceLine("f //").isComment());
        assertFalse(getSourceLine("f //= require <foo>").isComment());
    }
    
    @Test
    public void test_comment_should_be_extracted_from_comment_lines() {
        assertEquals("test", getSourceLine("//test").getComment());
        assertEquals(" test", getSourceLine("// test").getComment());
        assertNull(getSourceLine("f //test").getComment());
    }
    
    @Test
    public void test_line_that_contains_require_comment_should_be_a_require() {
        assertFalse(getSourceLine("// <foo>").isRequire());
        assertTrue(getSourceLine("//=  require <foo>").isRequire());
        assertFalse(getSourceLine("//= require<foo>").isRequire());
        assertTrue(getSourceLine("//= require \"foo\"").isRequire());
        assertFalse(getSourceLine("//= require <foo> f").isRequire());
    }
    
    @Test
    public void test_require_should_be_extracted_from_require_lines() {
        assertNull(getSourceLine("//= require").getRequire());
        assertEquals("<foo>", getSourceLine("//= require <foo>").getRequire());
        assertEquals("<foo>", getSourceLine("//= require   <foo> ").getRequire());
        assertEquals("\"foo\"", getSourceLine("//= require \"foo\"").getRequire());
    }
    
    @Test
    public void test_line_that_contains_a_provide_comment_should_be_a_provide() {
        assertTrue(getSourceLine("//= provide \"../assets\"").isProvide());
        assertFalse(getSourceLine("//= provide").isProvide());
        assertFalse(getSourceLine("//= provide <../assets>").isProvide());
    }
    
    @Test
    public void test_provide_should_be_extracted_from_provide_lines() {
        assertNull(getSourceLine("//= provide").getProvide());
        assertEquals("../assets", getSourceLine("//= provide \"../assets\"").getProvide());
    }
    
    @Test
    public void test_inspect_should_include_source_file_location_and_line_number() {
        Pathname pathname = new Pathname(getEnvironmentForResources(), "/a/b/c.js");
        SourceFile sourceFile = new SourceFile(getEnvironmentForResources(), pathname);
        assertEquals(String.format("line 25 of %s", (new File("/a/b/c.js")).getAbsolutePath()), 
                getSourceLine("hello", sourceFile, 25).inspect());
    }
    
    @Test
    public void test_interpolation_of_constants() throws UndefinedConstantException {
        Map<String, String> constants = new HashMap<String, String>(); 
        constants.put("VERSION", "1.0");
        assertEquals("var VERSION = \"1.0\";"+EOL, getSourceLine("var VERSION = \"<%= VERSION %>\";").toString(constants));
    
        Map<String, String> map = new HashMap<String, String>();
        map.put("ONE", "1"); map.put("TWO", "2");
        assertEquals("one: 1, two: 2, one: 1, two: 2"+EOL, getSourceLine("one: <%=ONE%>, two: <%= TWO%>, one: <%=ONE %>, two: <%= TWO %>").toString(map));
    }
    
    @Test
    public void test_interpolation_of_missing_constant_raises_undefined_constant_error() {
        try {
            Map<String, String> constants = new HashMap<String, String>(); 
            constants.put("VERSION", "1.0");
            getSourceLine("<%= NONEXISTENT %>").toString(constants);
            fail();
        } catch (UndefinedConstantException ex) {
            //success();
        }
    }
    
    @Test
    public void test_to_s_should_strip_trailing_whitespace_before_adding_line_ending() throws UndefinedConstantException {
        assertEquals("hello();\n", getSourceLine("hello();     \t  \r"+EOL).toString(new HashMap<String, String>()));
    }
}
