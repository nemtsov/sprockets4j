package com.georgecalm.sprockets;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  The SourceLine is one of the core
 *  classes in Sprockets. It holds all
 *  information about one line of a 
 *  SourceFile.
 */
public class SourceLine {
    private static Logger logger = LoggerFactory.getLogger(SourceLine.class);
    private SourceFile sourceFile;
    private String line;
    private Integer number;
    private String comment, require, provide;
    
    public SourceLine(SourceFile sourceFile, String line, Integer number) {
        this.sourceFile = sourceFile;
        this.line = line;
        this.number = number;
    }
    
    public SourceFile getSourceFile() { return sourceFile; }
    public String getLine() { return this.toString(); }
    public Integer getNumber() { return number; }
    
    /**
     * Retrieves a comment or
     * null if one does not exist
     * on this line.
     * 
     * @return
     */
    public String getComment() {
        if (null != comment) { return comment; }
        comment = findRegexGroup(line, "^\\s*\\/\\/(.*)", 1);
        return comment;
    }
    
    public boolean isComment() {
        return (null != getComment());
    }
    
    public void setComment() {
        comment = line;
    }
    
    public boolean isBeginsMultilineComment() {
        return line.matches("^\\s*\\/\\*(.*)");
    }
    
    public boolean isBeginsPdocComment() {
        return line.matches("^\\s*\\/\\*\\*(.*)");
    }
    
    public boolean isEndsMultilineComment() {
        return line.matches("^(.*)\\*\\/\\s*");
    }
    
    public boolean isEndsPdocComment() {
        return line.matches("^(.*)\\*\\*\\/\\s*");
    }
    
    /**
     * Retrieves the name of a
     * required SourceFile, if one is 
     * actually required on this line.
     * If an asset is not required on
     * this line -- null will be returned.
     * 
     * @return name of asset or null
     */
    public String getRequire() {
        if ((null != require) || !isComment()) { return require; }
        require = findRegexGroup(getComment(), "^=\\s+require\\s+((<(.*)>)|(\"(.*)\"))\\s*$", 1);
        return require;
    }
    
    public boolean isRequire() {
        return (null != getRequire());
    }
    
    /**
     * Retrieves the name of a
     * provided asset, if one is 
     * actually requested to be provided
     * on this line. If an asset was not requested
     * to be provided on this line -- null will 
     * be returned.
     * 
     * @return name of a required asset or null 
     */
    public String getProvide() {
        if ((null != provide) || !isComment()) { return provide; }
        provide = findRegexGroup(getComment(), "^=\\s+provide\\s+\"(.*?)\"\\s*$", 1);
        return provide;
    }
    
    public boolean isProvide() {
        return (null != getProvide());
    }

    /**
     * @return information about the current line
     */
    public String inspect() {
        return String.format("line %d of %s", number, sourceFile.getPathname());
    }
    
    /**
     * Returns a string representation of the 
     * current line, after the interpolation
     * of its SourceFile's constants. This method
     * will also strip all trailing white-space.
     * 
     * @return constant-interpolated source-line
     */
    @Override
    public String toString() {
        String str = line;
        
        try {
            str = toString(sourceFile.getEnvironment().getConstants());
        } catch (UndefinedConstantException ex) {
            logger.error(ex.getMessage());
        }
        
        return str;
    }
    
    /**
     * Returns a string representation of the 
     * current line, after the interpolation
     * of the provided constants. This method
     * will also strip all trailing white-space.
     * 
     * E.g. 
     *   "Hello <%= CONSTANT1 %>" will be replaced
     *   with "Hello World!" (provided that 
     *   CONSTANT1 = "World!")
     * 
     * @param constants to be interpolated
     * @return
     * @throws UndefinedConstantException
     */
    public String toString(Map<String, String> constants) throws UndefinedConstantException {
        String result = StringUtils.chomp(line);
        result = doInterpolateConstants(result, constants);
        result = stripTrailingWhitespace(result);
        return (result + System.getProperty("line.separator"));
    }
    
    @Override
    public boolean equals(Object otherSourceLine) {
        if (!(otherSourceLine instanceof SourceLine)) { return false; }
        SourceLine osl = (SourceLine) otherSourceLine;
        return ((sourceFile.equals(osl.sourceFile)) && (line.equals(osl.line)) && (number == osl.number));
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + sourceFile.hashCode();
        result = 31 * result + line.hashCode();
        result = 31 * result + number.hashCode();
        return result;
    }

    /**
     * Interpolates the provided
     * constants with the input string.
     * 
     * @param input
     * @param constants
     * @return
     * @throws UndefinedConstantException
     */
    private String doInterpolateConstants(String input, Map<String, String> constants) 
            throws UndefinedConstantException {
        String out = new String(input);
        
        Matcher m = Pattern.compile("<%=(.*?)%>").matcher(input);
        while (m.find()) {
            if (m.groupCount() != 1) { continue; }
            String constant = m.group(1);
            String trimmedConstant = constant.trim();
            
            if (constants.containsKey(trimmedConstant)) {
                out = out.replaceAll(("<%=" + constant + "%>"), constants.get(trimmedConstant));
            } else {
                throw new UndefinedConstantException (
                        String.format("couldn't find constant %s in %s", constant, inspect()));
            }
        }
        
        return out;
    }
    
    private String stripTrailingWhitespace(String input) {
        return input.replaceAll("\\s+$", "");
    }
    
    /**
     * Helper to retrieve a regex group
     * from an input, based on a pattern.
     * The specified group will be returned
     * if one is found; and null will be
     * returned if one is not found.
     * 
     * @param input
     * @param patternStr regex pattern
     * @param groupNumber to retrieve
     * 
     * @return found group or null in case  
     *         one is not found
     */
    private String findRegexGroup(CharSequence input, String patternStr, int groupNumber) {
        Matcher matcher = Pattern.compile(patternStr).matcher(input);
        return (matcher.find() && (matcher.groupCount() > 0))  ? matcher.group(groupNumber) : null;
    }
}
