package com.georgecalm.sprockets;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *  The Pathname holds the
 *  path-name of a source-file,
 *  asset, or directory.
 */
public class Pathname {
    public enum PathKind {FILE, DIRECTORY}
    private Environment environment;
    private String absoluteLocation;
    
    public Pathname(Environment environment, String absoluteLocation) {
        this.environment = environment;
        this.absoluteLocation = FilenameUtils.normalize((new File(absoluteLocation)).getAbsolutePath());
    }
    
    public Environment getEnvironment() { return environment; }
    public String getAbsoluteLocation() { return absoluteLocation; }
    
    /**
     * Returns a Pathname for the location relative 
     * to this pathname's absolute location. Assumes
     * that the provided location is for a FILE, and
     * not a directory.
     * 
     * @param location
     * @return
     */
    public Pathname find(String location) { return find(location, PathKind.FILE); }
    
    /**
     * Returns a Pathname for the location relative 
     * to this pathname's absolute location.
     * 
     * @param location
     * @param kind (e.g. FILE or DIRECTORY)
     * @return
     */
    public Pathname find(String location, PathKind kind) {
        File loc = new File(absoluteLocation);
        String absLoc = (loc.isFile()) ? loc.getParent() : loc.getAbsolutePath();
        
        File file = new File(absLoc + File.separator + location);
        boolean isValid = (file.exists() && ((kind == PathKind.FILE && file.isFile()) || 
                (kind == PathKind.DIRECTORY && file.isDirectory())));
        return isValid ? (new Pathname(environment, file.getAbsolutePath())) : null;
    }
    
    public Pathname getParentPathname() {
        Pathname pn = new Pathname(environment, (new File(absoluteLocation)).getParent());
        return pn;
    }
    
    /**
     * Retrieves a SourceFile to which
     * the current path points
     * 
     * @return
     */
    public SourceFile getSourceFile() {
        return new SourceFile(this.environment, this);
    }
    
    /**
     * Retrieves the String representation
     * of the contents of the file at the 
     * current path
     * 
     * @return
     * @throws IOException
     */
    public String getContents() throws IOException {
        return FileUtils.readFileToString(new File(absoluteLocation));
    }

    @Override
    public boolean equals(Object otherPathname) {
        if (!(otherPathname instanceof Pathname)) { return false; }
        Pathname pathname = (Pathname) otherPathname;
        return (absoluteLocation.equals(pathname.getAbsoluteLocation()));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + absoluteLocation.hashCode();
        return result;
    }
    
    /**
     * Returns the absolute-location
     * of this pathname
     */
    @Override
    public String toString() {
        return absoluteLocation;
    }
}
