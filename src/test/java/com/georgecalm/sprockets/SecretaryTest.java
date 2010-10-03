package com.georgecalm.sprockets;

import static com.georgecalm.sprockets.Helper.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SecretaryTest {
    private Secretary sec;
    
    @Before
    public void runBeforeEveryTest() {
        // Options opt = new Options();
        // opt.setRoot(RESOURCES_PATH);
        // sec = new Secretary(opt);
    }

    @Test
    public void test_load_locations_are_not_expanded_when_expand_paths_is_false() {
        Options opt = new Options(); 
        opt.setRoot(RESOURCES_PATH); 
        opt.setExpandPaths(false);
        
        sec = new Secretary(opt);
        sec.addLoadLocation("src/**/", opt);
        
        List<String> goodLocs = new ArrayList<String>();
        goodLocs.add(RESOURCES_PATH + File.separator + "src/**");
        goodLocs.add(RESOURCES_PATH);
        
        List<String> absLocs = new ArrayList<String>();
        Set<Pathname> paths = sec.getEnvironment().getLoadPath();
        for (Pathname path : paths) {
            absLocs.add(path.getAbsoluteLocation());
        }
        
        //TODO: asserts?
        assertTrue(true);
    }
}
