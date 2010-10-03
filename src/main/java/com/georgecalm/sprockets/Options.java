package com.georgecalm.sprockets;

import java.util.Collections;
import java.util.List;

public class Options {
    private String root = null;
    private List<String> loadPath = Collections.emptyList();
    private List<SourceFile> sourceFiles = Collections.emptyList();
    private Boolean expandPaths = null;
    private Boolean stripComments = null;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public List<String> getLoadPath() {
        return loadPath;
    }

    public void setLoadPath(List<String> loadPath) {
        this.loadPath = loadPath;
    }

    public List<SourceFile> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<SourceFile> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public Boolean isExpandPaths() {
        return expandPaths;
    }

    public void setExpandPaths(Boolean expandPaths) {
        this.expandPaths = expandPaths;
    }

    public Boolean isStripComments() {
        return stripComments;
    }

    public void setStripComments(Boolean stripComments) {
        this.stripComments = stripComments;
    }

    public Options merge(Options userOptions) {
        Options merged = new Options();

        merged.setRoot((null != userOptions.getRoot()) ? userOptions.getRoot() : this.root);
        merged.setLoadPath(userOptions.getLoadPath().isEmpty() ? userOptions.getLoadPath() : this.loadPath);
        merged.setSourceFiles(userOptions.getSourceFiles().isEmpty() ? userOptions.getSourceFiles() : this.sourceFiles);
        merged.setExpandPaths((null != userOptions.isExpandPaths()) ? userOptions.isExpandPaths() : this.expandPaths);
        merged.setStripComments((null != userOptions.isStripComments()) ? userOptions.isStripComments() : this.stripComments);

        return merged;
    }
}
