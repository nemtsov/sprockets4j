package com.georgecalm.sprockets;

import static com.georgecalm.sprockets.Helper.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class PathnameTest {
    @Test
    public void test_absolute_location_is_automatically_expanded() {
        String absLoc = (new File(RESOURCES_PATH, "foo")).getAbsolutePath();
        assertAbsoluteLocation(absLoc, getPathname("foo"));
        assertAbsoluteLocation(absLoc, getPathname("./foo"));
        assertAbsoluteLocation(absLoc, getPathname("./foo/../foo"));
    }
    
    @Test
    public void test_find_should_return_a_pathname_for_the_location_relative_to_the_absolute_location_of_the_pathname() {
        assertAbsoluteLocationEndsWith("src/foo/bar.js", getPathname("src/foo").find("bar.js"));
    }
    
    @Test
    public void test_find_should_return_nil_when_the_location_relative_to_the_absolute_location_of_the_pathname_is_not_a_file_or_does_not_exist() {
        assertNull(getPathname("src/foo").find("nonexistent.js"));
    }
    
    @Test
    public void test_parent_pathname_should_return_a_pathname_for_the_parent_directory() {
        assertAbsoluteLocationEndsWith("src", getPathname("src/foo").getParentPathname());
        assertAbsoluteLocationEndsWith("foo", getPathname("src/foo/foo.js").getParentPathname());
    }
    
    @Test
    public void test_source_file_should_return_a_source_file_for_the_pathname() {
        SourceFile sf = getPathname("src/foo.js").getSourceFile();
        assertTrue(sf instanceof SourceFile);
        assertEquals(getPathname("src/foo.js"), sf.getPathname());
    }
    
    @Test
    public void test_equality_of_pathnames() {
        assertEquals(getPathname("src/foo.js"), getPathname("src/foo.js"));
        assertEquals(getPathname("src/foo.js"), getPathname("src/foo/../foo.js"));
        assertFalse(getPathname("src/foo.js").equals(getPathname("src/foo/foo.js")));
    }
    
    @Test
    public void test_to_s_should_return_absolute_location() {
        assertEquals(getPathname("src/foo.js").toString(), getPathname("src/foo.js").toString());
    }
}
