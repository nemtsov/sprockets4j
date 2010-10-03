package com.georgecalm.sprockets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {
    private static Logger logger = LoggerFactory.getLogger(Environment.class);
    private Pathname root;
    private LinkedHashSet<Pathname> loadPath;
    private Map<String, String> constants;
    
    public Environment(String root) { this(root, new LinkedList<String>()); }
    public Environment(String root, List<String> loadPath) {
        this.root = new Pathname(this, root);
        this.loadPath = new LinkedHashSet<Pathname>();
        this.loadPath.add(this.root);
        this.constants = new HashMap<String, String>();
        
        // TODO: check - (I really hope I'm doing this right...)
        LinkedList<String> revLoadPath = new LinkedList<String>(loadPath);
        Collections.reverse(revLoadPath);
        for (String location : revLoadPath) {
            registerLoadLocation(location);
        }
    }
    
    public Pathname getRoot() { return root; }
    public Set<Pathname> getLoadPath() { return loadPath; }
    
    public Pathname getPathnameFrom(String location) {
        return new Pathname(this, absoluteLocationFrom(location));
    }
    
    public String registerLoadLocation(String location) {
        Pathname pathname = getPathnameFrom(location);
        loadPath.add(pathname);
        return location;
    }
    
    public Pathname find(String location) {
        Pathname found = null; 
        File locFile = new File(location);
        
        if (locFile.exists() && locFile.isAbsolute()) {
            found = getPathnameFrom(location);
        } else {
            List<Pathname> allFound = findAll(location);
            found = (allFound.size() > 0) ? allFound.get(0) : found;
        }
        
        return found;
    }
    
    public Map<String, String> getConstants() { return getConstants(false); }
    public Map<String, String> getConstants(boolean reload) {
        if (!constants.isEmpty() && !reload) { return constants; }
        
        for (Pathname path : loadPath) {
            constants = parseColonSeparatedPropertyFile(
                    new File(path.getAbsoluteLocation(), "constants.yml"), constants);
        }
        
        return constants;
    }
    
    @Override
    public String toString() {
        return String.format("root: %s, load-path: %s, constants: %s", 
                root.toString(), loadPath.toString(),
                ((null != constants) ? constants.toString() : "[]"));
    }
    
    @Override
    public boolean equals(Object otherEnv) {
        if (!(otherEnv instanceof Environment)) { return false; }
        Environment env = (Environment) otherEnv;
        
        boolean isEquals = (root.equals(env.getRoot()) && loadPath.equals(env.getLoadPath()) 
                && constants.equals(env.getConstants()));
        
        return isEquals;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + root.hashCode();
        result = 31 * result + loadPath.hashCode();
        result = 31 * result + constants.hashCode();
        return result;
    }
    
    private String absoluteLocationFrom(String location) {
        File f = new File(location);
        boolean isAbs = f.isAbsolute();
        
        location = (isAbs) ? location : (root.getAbsoluteLocation() + File.separator + location);
        String fabs = FilenameUtils.normalize(location);
        
        return fabs;
    }
    
    private List<Pathname> findAll(String location) {
        List<Pathname> list = new ArrayList<Pathname>();

        for (Pathname pname : loadPath) {
            Pathname floc = pname.find(location);
            if (null != floc) { list.add(floc); }
        }
        
        // making sure that the files from the path
        // which was added last appear first in the 
        // resulting list
        Collections.reverse(list);
        
        return list;
    }
    
    /**
     * This call will parse a colon separated file that
     * is located within the specified root of this
     * Environment. 
     * 
     * As it is very simple, this parser will silently 
     * ignore all lines after an error.  
     * 
     * @param fileName
     * @return a Map with property name/value pairs
     */
    private Map<String, String> parseColonSeparatedPropertyFile(File file, Map<String, String> map) {
        try {
            Scanner s = new Scanner(file);

            while (s.findInLine("\\s*(.*)\\s*:\\s*(.*)\\s*") != null) {
                MatchResult res = s.match();
                
                if (res.groupCount() == 2) {
                    map.put(res.group(1).trim(), res.group(2).trim());
                }
                
                if (s.hasNextLine()) { s.nextLine(); } 
            }
            
        } catch (FileNotFoundException e) {
            logger.info(file.getAbsolutePath() + 
                    " not found. It does not exist or is inaccessible");
        }
        
        return map;
    }
}
