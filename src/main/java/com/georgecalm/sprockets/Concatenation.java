package com.georgecalm.sprockets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Concatenation {
    private List<SourceLine> sourceLines;
    private Map<SourceFile, Long> sourceFileMTimes;
    
    public Concatenation() {
        sourceLines = new LinkedList<SourceLine>();
        sourceFileMTimes = new HashMap<SourceFile, Long>();
    }
    
    public List<SourceLine> getSourceLines() { return sourceLines; }
    
    public void record(SourceLine sourceLine) {
        sourceLines.add(sourceLine);
        recordMTimeFor(sourceLine.getSourceFile());
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (SourceLine line : sourceLines) {
            sb.append(line);
        }
        
        return sb.toString();
    }
    
    public Long getMTime() {
        return (sourceFileMTimes.size() == 0) ? 0 : Collections.max(sourceFileMTimes.values());
    }
    
    public void saveTo(String fileName) throws IOException {
        File file = new File(fileName);
        file.setLastModified(getMTime());
        FileWriter fw = new FileWriter(file);
        fw.write(this.toString());
        fw.close();
    }
    
    private void recordMTimeFor(SourceFile sourceFile) {
        if (!sourceFileMTimes.containsKey(sourceFile)) {
            sourceFileMTimes.put(sourceFile, sourceFile.getMtime());
        }
    }
}
