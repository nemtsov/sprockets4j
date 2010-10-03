package com.georgecalm.sprockets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.georgecalm.sprockets.Pathname.PathKind;

/**
 * The SourceFile is one of the core
 * classes in Sprockets. It holds all
 * information about a particular file
 * in the system.
 */
public class SourceFile {
    private Environment environment;
    private Pathname pathname;
    private List<SourceLine> lines;

    public SourceFile(Environment environment, Pathname pathname) {
        //TODO: why not extract the env from the pathname?
        this.environment = environment;  
        this.pathname = pathname;
    }
    
    public Environment getEnvironment() { return environment; }
    public Pathname getPathname() { return pathname; }

    public List<SourceLine> getSourceLines() throws FileNotFoundException {
        if (null != lines) { return lines; }
        
        lines = new LinkedList<SourceLine>();
        List<SourceLine> comments = new LinkedList<SourceLine>();
        
        Scanner scanner = new Scanner(new File(pathname.getAbsoluteLocation()));
        try {
            String rawLine;
            int lineno = 0;
            while (scanner.hasNextLine()) {
                rawLine = scanner.nextLine();
                SourceLine line = new SourceLine(this, rawLine, lineno++);
                lines.add(line);
                
                if (line.isBeginsPdocComment() || (comments.size() > 0)) {
                    comments.add(line);
                }
                
                if (line.isEndsMultilineComment()) {
                    if (line.isEndsPdocComment()) {
                        for (SourceLine l : comments) {
                            l.setComment();
                        }
                    }
                    comments.clear();
                }
            }
        } finally {
            scanner.close();
        }
        
        return lines;
    }
    
    // Not porting each_source_line. Prefer to use getSourceLines() with for:in instead.
    // Although it can be implemented with a sub-classed Runnable, that would complicate things.
    
    /**
     * Finds the path-name by a location.
     * This method assumes you are providing
     * a location of a file, and not a directory.
     * 
     * @param location of the file
     * @return path-name
     */
    public Pathname find(String location) { 
        return find(location, PathKind.FILE); 
    }
    
    /**
     * Files the path-name by a location.
     * The "kind" determines whether the location
     * is a file or a directory.
     * 
     * @param location of the file or directory
     * @param kind (file or directory)
     * @return
     */
    public Pathname find(String location, PathKind kind) {
        return pathname.find(location, kind);
    }
    
    @Override
    public boolean equals(Object otherSourceFile) {
        if (!(otherSourceFile instanceof SourceFile)) { return false; }
        SourceFile sourceFile = (SourceFile) otherSourceFile;
        return (pathname.equals(sourceFile.pathname));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + pathname.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return pathname.toString();
    }

    /**
     * Retrieves the time when the
     * current file was last modified. 
     * In case the file does not exist, 
     * the current time (in milliseconds)  
     * will be returned.
     * 
     * @return time
     */
    public Long getMtime() {
        File file = new File(pathname.getAbsoluteLocation());
        return file.exists() ? file.lastModified() : System.currentTimeMillis(); 
    }
}