package com.georgecalm.sprockets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import com.georgecalm.sprockets.Pathname.PathKind;

/**
 * Sprockets takes any number of <em>source files</em> and preprocesses 
 * them line-by-line in order to build a single <em>concatenation</em>. 
 * Specially formatted lines act as <em>directives</em> to the Sprockets 
 * preprocessor, telling it to <em>require</em> the contents of another 
 * file or library first or to <em>provide</em> a set of asset files to 
 * the document root. Sprockets attempts to fulfill required 
 * dependencies by searching a set of directories called the
 * <em>load path</em>. 
 */
public class Preprocessor {
    private Environment environment;
    private Concatenation concatenation;
    private List<SourceFile> sourceFiles;
    private List<Pathname> assetPaths;
    private Options options;
    private enum KindOfRequire { RELATIVE_REQUIRE, REQUIRE }
    
    /**
     * Creates a new Preprocessor
     * with the default options.
     * 
     * @param environment
     */
    public Preprocessor(Environment environment) { this(environment, new Options()); }
    
    public Preprocessor(Environment environment, Options options) {
        this.environment = environment;
        this.concatenation = new Concatenation();
        this.sourceFiles = new LinkedList<SourceFile>();
        this.assetPaths = new LinkedList<Pathname>();
        this.options = options;
    }
    
    public Environment getEnvironment() { return environment; }
    public Concatenation getConcatenation() { return concatenation; }
    public List<SourceFile> getSourceFiles() {  return sourceFiles; }
    public List<Pathname> getAssetPaths() { return assetPaths; }
    
    /**
     * Require all SourceFiles and provide
     * all assets which are specified in 
     * the lines of the given SourceFile.
     * 
     * This method is recursive.
     * 
     * @param sourceFile
     * @throws FileNotFoundException
     */
    public void require(SourceFile sourceFile) throws FileNotFoundException {
        if (sourceFiles.contains(sourceFile)) { return; }
        sourceFiles.add(sourceFile);
        
        List<SourceLine> lines;
        lines = sourceFile.getSourceLines();
        
        for (SourceLine sourceLine : lines) {
            if (sourceLine.isRequire()) {
                requireFromSourceLine(sourceLine);
            } else if (sourceLine.isProvide()) {
                provideFromSourceLine(sourceLine);
            } else {
                recordSourceLine(sourceLine);
            }
        }
    }
    
    /**
     * Provide an asset specified
     * by the given path-name.
     * 
     * @param assetPath
     */
    public void provide(Pathname assetPath) {
        if ((null == assetPath) || assetPath.toString().isEmpty() || assetPaths.contains(assetPath)) { return; }
        assetPaths.add(assetPath);
    }
    
    /**
     * Require a SourceFile
     * which the given SourceLine
     * specified.
     * 
     * @param line
     * @throws FileNotFoundException
     */
    private void requireFromSourceLine(SourceLine line) throws FileNotFoundException {
        require(getPathnameFrom(line).getSourceFile());
    }

    /**
     * Provide an asset that the 
     * given SourceLine specified.
     * 
     * @param line
     */
    private void provideFromSourceLine(SourceLine line) {
        provide(getAssetPathFrom(line));
    }
    
    /**
     * Records a SourceLine to the
     * current concatenation.
     * 
     * @param line
     */
    private void recordSourceLine(SourceLine line) {
        if (!(line.isComment() && isStipComments())) {
            concatenation.record(line);
        }
    }
    
    /**
     * Determines whether it is necessary
     * to strip comments from the preprocessed
     * SourceFiles based on the options.
     * 
     * If not set in the options (when strip-
     * comment is null), defaults to true.
     * 
     * @return
     */
    private boolean isStipComments() {
        Boolean isc = options.isStripComments();
        return ((null == isc) || isc);
    }
    
    /**
     * Retrieves a pathname from the
     * provided SourceLine.
     * 
     * @param line
     * @return
     * @throws FileNotFoundException
     */
    private Pathname getPathnameFrom(SourceLine line) throws FileNotFoundException {
        Pathname pname = getPathnameFinderFrom(line);
        if (null == pname) {
            throwFileNotFoundExceptionForLine(line);
        }
        
        return pname;
    }
    
    /**
     * Finds the Pathname of an absolute
     * require of the provided SourceLine. 
     * 
     * @param line
     * @return
     */
    private Pathname getPathnameForRequireFrom(SourceLine line) {
        return environment.find(getRequireLocationFrom(line));
    }
    
    /**
     * Finds the Pathname of a relative
     * require of the provided SourceLine. 
     * 
     * @param line
     * @return
     */
    private Pathname getPathnameForRelativeRequireFrom(SourceLine line) {
        return line.getSourceFile().find(getRequireLocationFrom(line));
    }
    
    /**
     * Factory method which first determines
     * the type of require (relative / absolute)
     * and then retrieves its pathname.
     * 
     * @param line
     * @return
     */
    private Pathname getPathnameFinderFrom(SourceLine line) {
        Pathname pname = null;
        
        switch (getKindOfRequireFrom(line)) {
            case RELATIVE_REQUIRE: pname = getPathnameForRelativeRequireFrom(line); break;
            case REQUIRE: pname = getPathnameForRequireFrom(line); break;
        }
        
        return pname;
    }
    
    /**
     * Determines the type of require
     * (relative of absolute).
     * 
     * @param line
     * @return
     */
    private KindOfRequire getKindOfRequireFrom(SourceLine line) {
        return (line.getRequire().charAt(0) == '"') ? KindOfRequire.RELATIVE_REQUIRE : KindOfRequire.REQUIRE;
    }
    
    /**
     * Retrieves the fully-qualified
     * location of a require from a 
     * SourceLine.
     * 
     * @param line
     * @return
     */
    private String getRequireLocationFrom(SourceLine line) {
        String req = line.getRequire();
        String loc = req.substring(1, (req.length() - 1));  // remove '"'s or '<'/'>'
        
        File floc = new File(loc);
        String locParent = floc.getParent();
        String locName = floc.getName();
        
        // the piece below has been modified from the original
        // to support any suffix provided by the user. 
        // it will default to .js if no suffix is provided
        
        int idxOfDot = locName.lastIndexOf(".");
        String suffix =  locName.substring(((idxOfDot == -1) ? locName.length() : idxOfDot), locName.length());
        suffix = (suffix.isEmpty() ? ".js" : suffix);
        
        int idxOfSuf = locName.indexOf(suffix);
        locName = locName.substring(0, ((idxOfSuf == -1) ? locName.length() : idxOfSuf));
        
        return (((null == locParent) ? "" : locParent) + locName + suffix);
    }
    
    /**
     * Retrieve a Pathname from 
     * the 'provide' of the given line.
     * 
     * @param line
     * @return
     */
    private Pathname getAssetPathFrom(SourceLine line) {
        return line.getSourceFile().find(line.getProvide(), PathKind.DIRECTORY);
    }
    
    /**
     * Helper to which throws a FileNotFoundException
     * with useful information about the given line. 
     * 
     * @param line
     * @throws FileNotFoundException
     */
    private void throwFileNotFoundExceptionForLine(SourceLine line) throws FileNotFoundException {
        String kind = getKindOfRequireFrom(line).toString().replaceAll("_", " ");
        String file = (new File(getRequireLocationFrom(line))).getName();
        throw new FileNotFoundException(String.format("can't find file for %s %s (%s)", kind, file, line.inspect()));
    }
}
