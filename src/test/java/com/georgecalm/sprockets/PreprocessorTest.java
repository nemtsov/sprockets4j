package com.georgecalm.sprockets;

import static com.georgecalm.sprockets.Helper.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PreprocessorTest {
    private Environment env;
    private Preprocessor pp;

    @Before
    public void runBeforeEveryTest() {
        env = getEnvironmentForResources();
        pp = new Preprocessor(env);
    }
    
    @Test
    public void test_double_slash_comments_that_are_not_requires_should_be_removed_by_default() throws FileNotFoundException {
        requireFileForThisTest();
        assertConcatenationDoesNotContainLine("// This is a double-slash comment that should not appear in the resulting output file.");
        assertConcatenationContainsLine("/* This is a slash-star comment that should not appear in the resulting output file. */");
    }
    
    @Test
    public void test_double_slash_comments_that_are_not_requires_should_be_ignored_when_strip_comments_is_false() throws FileNotFoundException {
        Options opt = new Options(); opt.setStripComments(false);
        pp = new Preprocessor(env, opt);
        requireFileForThisTest();
        assertConcatenationContainsLine("// This is a double-slash comment that should appear in the resulting output file.");
        assertConcatenationContainsLine("/* This is a slash-star comment that should appear in the resulting output file. */");

        assertConcatenationContainsLine("/* This is multiline slash-star comment");
        assertConcatenationContainsLine("*  that should appear in the resulting");
        assertConcatenationContainsLine("*  output file */");

        assertConcatenationContainsLine("This is not a PDoc comment that should appear in the resulting output file.");
    }
    
    @Test
    public void test_multiline_comments_should_be_removed_by_default() throws FileNotFoundException {
        requireFileForThisTest();
        assertConcatenationDoesNotContainLine("/**");
        assertConcatenationDoesNotContainLine(" *  This is a PDoc comment");
        assertConcatenationDoesNotContainLine(" *  that should appear in the resulting output file.");
        assertConcatenationDoesNotContainLine("**/");
    }
    
    @Test
    public void test_requiring_a_single_file_should_replace_the_require_comment_with_the_file_contents() throws FileNotFoundException {
        requireFileForThisTest();
        
        StringBuilder sb = new StringBuilder();
        sb.append("var before_require;" + EOL);
        sb.append("var Foo = { };" + EOL);
        sb.append("var after_require;" + EOL);
        
        assertConcatenationContains(sb.toString());
    }
    
    @Test
    public void test_requiring_a_file_that_does_not_exist_should_raise_an_error() {
        try {
            requireFileForThisTest();
        } catch(Exception ex) {
            assertTrue(ex instanceof FileNotFoundException);
        }
    }
    
    @Test
    public void test_requiring_the_current_file_should_do_nothing() throws FileNotFoundException {
        requireFileForThisTest();
        assertEquals("", getOutputText());
    }
    
    @Test
    public void test_requiring_a_file_after_it_has_already_been_required_should_do_nothing() throws FileNotFoundException {
        requireFileForThisTest();
        
        StringBuilder sb = new StringBuilder();
        sb.append("var before_first_require;" + EOL);
        sb.append("var Foo = { };" + EOL);
        sb.append("var after_first_require_and_before_second_require;" + EOL);
        sb.append("var after_second_require;" + EOL);
        
        assertConcatenationContains(sb.toString());
    }
    
    private Concatenation getConcatenation() {
        return pp.getConcatenation();
    }
    
    private String getOutputText() {
        return getConcatenation().toString();
    }
    
    private List<String> getSourceLinesMatching(String line) {
        List<String> matchingLines = new ArrayList<String>();
        
        Concatenation conc = getConcatenation();
        for (SourceLine sourceLine : conc.getSourceLines()) {
            if (sourceLine.toString().trim().equals(line.trim())) {
                matchingLines.add(line);
            }
        }
        
        return matchingLines;
    }
    
    private void requireFile(String location) throws FileNotFoundException {
        Pathname path = env.find(location);
        SourceFile sf = path.getSourceFile();
        pp.require(sf);
    }
    
    private void requireFileForThisTest() throws FileNotFoundException {
        requireFile(getFileForThisTest());
    }
    
    private String getFileForThisTest() {
        String fileName = null;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement el : stackTraceElements) {
            String mname = el.getMethodName();
            
            if (mname.indexOf("test") == 0) {
              fileName = (mname + ".js").substring(5);
              break;
            }
        }
        
        return fileName;
    }
    
    private void assertConcatenationDoesNotContainLine(String line) {
        assertEquals(0, getSourceLinesMatching(line).size());
    }
    
    private void assertConcatenationContainsLine(String line) {
        assertTrue(getSourceLinesMatching(line).size() > 0);
    }
    
    private void assertConcatenationContains(String indentedText) {
        //TODO: understand
    }
}
