package com.georgecalm.sprockets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;

public class Helper {
    public static final String RESOURCES_PATH = getResourcesPath();
    public static final String EOL = System.getProperty("line.separator");
    private static List<String> sourceDirectoriesInResourcePath;

    public static File getLocationForResource(String resource) {
        return new File(RESOURCES_PATH, resource);
    }
    
    public static String getContentOfResource(String resource) throws IOException {
        return FileUtils.readFileToString(getLocationForResource(resource));
    }
    
    public static Environment getEnvironmentForResources() {
        List<String> sourceDirs = getSourceDirectoriesInResourcesPath();
        return new Environment(RESOURCES_PATH, sourceDirs);
    }
    
    public static List<String> getSourceDirectoriesInResourcesPath() {
        if (null != sourceDirectoriesInResourcePath) { return sourceDirectoriesInResourcePath; }
        
        List<File> lst = new LinkedList<File>();
        File dir = new File(RESOURCES_PATH);

        FileFilter filter = new NameFileFilter("src");
        listDirectories(lst, dir, filter);
        
        List<String> lstPaths = new LinkedList<String>();
        for (File file : lst) {
            lstPaths.add(file.getAbsolutePath());
        }
        
        return lstPaths;
    }
    
    public static void assertAbsoluteLocation(String location, Pathname pathname) {
        assertEquals(new File(location).getAbsolutePath(), pathname.getAbsoluteLocation());
    }
    
    public static void assertAbsoluteLocationEndsWith(String locEnding, Pathname pathname) {
        assertNotNull(pathname);
        String path = pathname.getAbsoluteLocation();
        boolean endsWithLoc = path.endsWith(locEnding);
        assertTrue(endsWithLoc);
    }
 
    public static Pathname getPathname(String location) { return getPathname(location, getEnvironmentForResources()); }
    public static Pathname getPathname(String location, Environment env) {
        return new Pathname(env, (RESOURCES_PATH + File.separator + location));
    }
    
    public static SourceFile getSourceFile(String location) { return getSourceFile(location, getEnvironmentForResources()); }
    public static SourceFile getSourceFile(String location, Environment env) {
        return new SourceFile(env, getPathname(location, env));
    }
    
    public static SourceLine getSourceLine(String line) { return getSourceLine(line, getSourceFile("dummy")); }
    public static SourceLine getSourceLine(String line, SourceFile sfile) { return getSourceLine(line, sfile, 1); }
    public static SourceLine getSourceLine(String line, SourceFile sfile, int lineNumber) {
        return new SourceLine(sfile, line, lineNumber);
    }
    
    
    private static String getResourcesPath() {
        String path;
        String s = File.separator;
        File f = new File(String.format("src%stest%sresources", s, s, s));
        
        // testing in an IDE
        if (f.exists()) {
            path = f.getAbsolutePath();
        }
        // testing from Maven
        else {
            URL url = (Helper.class).getResource(s);
            path = url.getFile();
        }

        return path;
    }
    
    private static void listDirectories(List<File> filesList, File dir, FileFilter filter) {
        File[] files = dir.listFiles();
        if (files == null) { return; }
        
        for (File f : files) {
            if (!f.isDirectory()) { continue; }
            if (filter.accept(f)) { filesList.add(f); }
            listDirectories(filesList, f, filter);
        }
    }
}
