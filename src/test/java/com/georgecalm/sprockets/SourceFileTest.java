package com.georgecalm.sprockets;

import static com.georgecalm.sprockets.Helper.getContentOfResource;
import static com.georgecalm.sprockets.Helper.getSourceFile;
import static com.georgecalm.sprockets.Helper.assertAbsoluteLocationEndsWith;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class SourceFileTest {
    @Test
    public void test_each_source_line() throws IOException {
        String content = getContentOfResource("src/foo/bar.js");
        
        SourceFile sf = getSourceFile("src/foo/bar.js");
        List<SourceLine> slines = sf.getSourceLines();
        
        StringBuilder sb = new StringBuilder();
        for (SourceLine line : slines) { sb.append(line.getLine()); }
        
        assertEquals(content, sb.toString());
        assertEquals(4, slines.size());
    }
    
    @Test
    public void test_find_should_return_pathname_for_file_relative_to_the_current_pathname() {
        SourceFile sf = getSourceFile("src/foo/foo.js");
        Pathname pn = sf.find("bar.js");
        assertAbsoluteLocationEndsWith("test/resources/src/foo/bar.js", pn);
    }
    
    @Test
    public void test_find_should_return_nil_for_nonexistent_file() {
        assertNull(getSourceFile("src/foo/foo.js").find("nonexistent.js"));
    }
    
    @Test
    public void test_equality_of_source_files() {
        assertEquals(getSourceFile("src/foo/foo.js"), getSourceFile("src/foo/foo.js"));
        assertEquals(getSourceFile("src/foo/foo.js"), getSourceFile("src/foo/../foo/foo.js"));
        assertFalse(getSourceFile("src/foo/foo.js").equals(getSourceFile("src/foo.js")));
        assertFalse(getSourceFile("src/foo/foo.js").equals(getSourceFile("src/foo/bar.js")));
    }
    
    @Test
    public void test_mtime_should_return_now_if_file_does_not_exist() {
        //NOTE: lastModified would be 0 if we're not setting to currentTimeInMillis
        assertTrue(getSourceFile("src/foo/nonexistent.js").getMtime() > 0);
    }
}
