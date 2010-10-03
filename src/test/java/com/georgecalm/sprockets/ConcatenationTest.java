package com.georgecalm.sprockets;

import static org.junit.Assert.*;
import static com.georgecalm.sprockets.Helper.*;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ConcatenationTest {
    private Concatenation conc;
    
    @Before
    public void runBeforeEveryTest() {
        conc = new Concatenation();
    }
    
    @Test
    public void testRecord() {
        SourceFile sf = getSourceFile("testSourceFile.txt");
        conc.record(new SourceLine(sf, "  hello", 0));
        conc.record(new SourceLine(sf, "world", 1));
        assertEquals("  hello" + EOL + "world" + EOL, conc.toString());
    }
    
    @Test
    public void testToString() {
        assertEquals("", conc.toString());
        conc.record(getSourceLine("hello\n"));
        conc.record(getSourceLine("world\n"));
        assertEquals("hello\nworld\n", conc.toString());
    }
    
    @Test
    public void testSaveTo() throws IOException {
        File fileName = new File(RESOURCES_PATH, "output.js");
        conc.saveTo(fileName.toString());
        assertEquals(conc.toString(), FileUtils.readFileToString(fileName));
        fileName.delete(); // need to clean-up after the test run
    }
    
    @Test
    public void testSaveToNotEmpty() throws IOException {
        File fileName = new File(RESOURCES_PATH, "output.js");
        conc.record(getSourceLine("a source line\n"));
        conc.saveTo(fileName.toString());
        assertEquals(conc.toString(), FileUtils.readFileToString(fileName));
        fileName.delete(); // clean-up
    }
}
