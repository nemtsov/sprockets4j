package com.georgecalm.sprockets;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Secretary {
    private static final Options DEFAULT_OPTIONS = new Options();
    static {
        DEFAULT_OPTIONS.setRoot(".");
        DEFAULT_OPTIONS.setLoadPath(new ArrayList<String>());
        DEFAULT_OPTIONS.setSourceFiles(new ArrayList<SourceFile>());
        DEFAULT_OPTIONS.setExpandPaths(true);
        DEFAULT_OPTIONS.setStripComments(true);
    }
    
    private Options options;
    private Environment environment;
    private Preprocessor preprocessor;
    
    public Secretary(Options options) {
        this.reset(options);
    }
    
    public Environment getEnvironment() { return environment; }
    public Preprocessor getPreprocessor() { return preprocessor; }
    
    public void reset() { reset(new Options()); }
    public void reset(Options opt) {
        options = DEFAULT_OPTIONS.merge(opt);
        environment = new Environment(options.getRoot());
        
        Options ppOpt = new Options();
        ppOpt.setStripComments(options.isStripComments());
        preprocessor = new Preprocessor(environment, ppOpt);
        
        addLoadLocations(options.getLoadPath());
        addSourceFiles(options.getSourceFiles());
    }
    
    public void addLoadLocation(String loadPath) { addLoadLocation(loadPath, new Options()); }
    public void addLoadLocation(String loadPath, Options options) {
        List<String> loadPaths = new LinkedList<String>();
        loadPaths.add(loadPath);
        addLoadLocations(loadPaths, options);
    }
    
    public void addLoadLocations(List<String> loadPath) { addLoadLocations(loadPath, new Options()); }
    public void addLoadLocations(List<String> loadPath, Options options) {
        List<String> locations = expandPaths(loadPath);
        for (String location : locations) {
            environment.registerLoadLocation(location);
        }
    }
    
    public void addSourceFile(SourceFile sourceFile) { addSourceFile(sourceFile, new Options()); }
    public void addSourceFile(SourceFile sourceFile, Options options) {
        List<SourceFile> sourceFiles = new LinkedList<SourceFile>();
        sourceFiles.add(sourceFile);
        addSourceFiles(sourceFiles, options);
    }
    
    public void addSourceFiles(List<SourceFile> sourceFiles) { addSourceFiles(sourceFiles, new Options()); }
    public void addSourceFiles(List<SourceFile> sourceFiles, Options options) {
        //TODO: ...
    }
    
    public Concatenation getConcatenation() {
        return preprocessor.getConcatenation();
    }
    
    public void installAssets() {
        //TODO: ...
    }
    
    public Long getSourceLastModified() {
        //TODO: ...
        return null;
    }
    
    /**
     * Defaults to expanding the paths.
     * 
     * @param paths
     * @return
     */
    private List<String> expandPaths(List<String> paths) { 
        return expandPaths(paths, options); 
    }
    
    /**
     * Defaults to expanding the paths if
     * not set in the options.
     * 
     * @param paths
     * @param options
     * @return
     */
    private List<String> expandPaths(List<String> paths, Options options) { 
        List<String> arr = new ArrayList<String>();
        Boolean isExpandPaths = options.isExpandPaths();
        isExpandPaths = ((null == isExpandPaths) || isExpandPaths); // default null to true
        
        for (String path : paths) {
            if (isExpandPaths) {
                
            } else {
                
            }
        }
        
        return arr;
    }
    
    private String getFromRoot(String path) {
        return (new File(path).isAbsolute()) ? path : (options.getRoot() + File.separator + path);
    }
    
    private void copyAssetsFrom(String assetPath) {
        //TODO: ...
    }
    
    private String getRelativeFilePathsBeneath(String assetPath) {
        //TODO: ...
        return null;
    }
    
    private String getAssetRoot() {
        //TODO: ...
        return null;
    }
    
    private List<String> getPathPieces(String path) {
        //TODO: ...
        return null;
    }
}
