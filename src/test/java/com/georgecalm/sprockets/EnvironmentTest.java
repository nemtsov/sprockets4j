package com.georgecalm.sprockets;

import static com.georgecalm.sprockets.Helper.RESOURCES_PATH;
import static com.georgecalm.sprockets.Helper.assertAbsoluteLocation;
import static com.georgecalm.sprockets.Helper.assertAbsoluteLocationEndsWith;
import static com.georgecalm.sprockets.Helper.getEnvironmentForResources;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class EnvironmentTest {
    @Test
    public void test_load_path_locations_become_pathnames_for_absolute_locations_from_the_root() {
        Environment env = new Environment("/root", Arrays.asList("/a", "b"));
        assertLoadPathEquals(Arrays.asList("/a", "/root/b", "/root"), env);
    }
    
    @Test
    public void test_pathname_from_for_location_with_leading_slash_should_return_a_pathname_with_the_location_unchanged() {
        Environment env = new Environment("/root");
        assertAbsoluteLocation("/a", env.getPathnameFrom("/a"));
    }
    
    @Test
    public void test_pathname_from_for_relative_location_should_return_a_pathname_for_the_expanded_absolute_location_from_root() {
        Environment env = new Environment("/root");
        assertAbsoluteLocation("/root/a", env.getPathnameFrom("a"));
        assertAbsoluteLocation("/root/a", env.getPathnameFrom("./a"));
        assertAbsoluteLocation("/a", env.getPathnameFrom("../a"));
    }
    
    @Test
    public void test_register_load_location_should_unshift_the_location_onto_the_load_path() {
        Environment env = new Environment("/root");
        env.registerLoadLocation("a");
        assertLoadPathEquals(Arrays.asList("/root/a", "/root"), env);
        env.registerLoadLocation("b");
        assertLoadPathEquals(Arrays.asList("/root/b", "/root/a", "/root"), env);
    }
    
    @Test
    public void test_register_load_location_should_remove_already_existing_locations_before_unshifting() {
        Environment env = new Environment("/root");
        env.registerLoadLocation("a");
        env.registerLoadLocation("b");
        assertLoadPathEquals(Arrays.asList("/root/b", "/root/a", "/root"), env);
        env.registerLoadLocation("a");
        assertLoadPathEquals(Arrays.asList("/root/b", "/root/a", "/root"), env);
    }
    
    @Test
    public void test_find_should_return_the_first_matching_pathname_in_the_load_path() {
        Environment env = getEnvironmentForResources();
        Pathname firstPath = env.find("foo.js");
        assertAbsoluteLocationEndsWith("src/foo.js", firstPath);
        
        env.registerLoadLocation(new File(RESOURCES_PATH + File.separator + "src", "foo").getAbsolutePath());
        Pathname secondPath = env.find("foo.js");
        
        assertFalse(firstPath.equals(secondPath));
        assertAbsoluteLocationEndsWith("foo/foo.js", secondPath);
    }
    
    @Test
    public void test_find_should_return_nil_when_no_matching_source_file_is_found() {
        Environment env = getEnvironmentForResources();
        assertNull(env.find("nonexistent.js"));
    }
    
    @Test
    public void test_constants_should_return_a_hash_of_all_constants_defined_in_the_load_path() {
        Map<String, String> constants = getEnvironmentForResources().getConstants();
        
        Set<String> keys = constants.keySet();
        assertEquals(4, keys.size());
        
        List<String> sortedList = new ArrayList<String>(keys);
        Collections.sort(sortedList);
        
        assertEquals("HELLO", sortedList.get(0));
        assertEquals("ONE", sortedList.get(1));
        assertEquals("TWO", sortedList.get(2));
        assertEquals("VERSION", sortedList.get(3));
    }
    
    private void assertLoadPathEquals(List<String> loadPathAbsoluteLocations, Environment env) {
        Set<Pathname> paths = env.getLoadPath();
        Set<String> pathLoc = new HashSet<String>();
        for (Pathname path : paths) {
            pathLoc.add(path.getAbsoluteLocation());
        }

        assertEquals(pathLoc.size(), loadPathAbsoluteLocations.size());
        for (String path : loadPathAbsoluteLocations) {
            assertTrue(pathLoc.contains(path));
        }
    }
}
