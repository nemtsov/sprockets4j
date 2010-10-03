package com.georgecalm.sprockets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.DirectoryWalker;

public class GlobFilenameExpander extends DirectoryWalker {
    private Pattern pattern;
    private File startDirectory;
    
    public GlobFilenameExpander(File startDirectory) {
        super();
        this.startDirectory = startDirectory;
    }

    public List<File> expand(String glob) throws IOException {
        this.pattern = globToRegexPattern(new File(startDirectory + File.separator + glob).toString());
        List<File> results = new ArrayList<File>();
        walk(startDirectory, results);
        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean handleDirectory(File directory, int depth,
            Collection results) {
        
        Matcher m = pattern.matcher(directory.toString());
        if (m.matches()) {
            results.add(directory);
        }
        
        boolean isDiveIntoDir = true;
        return isDiveIntoDir;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleFile(File file, int depth, Collection results) {
        Matcher m = pattern.matcher(file.toString());
        if (m.matches()) {
            results.add(file);
        }
    }
    
    private static Pattern globToRegexPattern(final String glob) throws PatternSyntaxException {
        /* Stack to keep track of the parser mode: */
        /* "--" : Base mode (first on the stack) */
        /* "[]" : Square brackets mode "[...]" */
        /* "{}" : Curly braces mode "{...}" */
        final Deque<String> parserMode = new ArrayDeque<String>();
        parserMode.push("--"); // base mode

        final int globLength = glob.length();
        int index = 0; // index in glob

        /* equivalent REGEX expression to be compiled */
        final StringBuilder t = new StringBuilder();

        while (index < globLength) {
            char c = glob.charAt(index++);

            if (c == '\\') {
                /***********************
                 * (1) ESCAPE SEQUENCE *
                 ***********************/

                if (index == globLength) {
                    /* no characters left, so treat '\' as literal char */
                    t.append(Pattern.quote("\\"));
                } else {
                    /* read next character */
                    c = glob.charAt(index);
                    final String s = c + "";

                    if (("--".equals(parserMode.peek()) && "\\[]{}?*"
                            .contains(s))
                            || ("[]".equals(parserMode.peek()) && "\\[]{}?*!-"
                                    .contains(s))
                            || ("{}".equals(parserMode.peek()) && "\\[]{}?*,"
                                    .contains(s))) {
                        /* escape the construct char */
                        index++;
                        t.append(Pattern.quote(s));
                    } else {
                        /* treat '\' as literal char */
                        t.append(Pattern.quote("\\"));
                    }
                }
            } else if (c == '*') {
                /************************
                 * (2) GLOB PATTERN '*' *
                 ************************/

                /* create non-capturing group to match zero or more characters */
                t.append(".*");
            } else if (c == '?') {
                /************************
                 * (3) GLOB PATTERN '?' *
                 ************************/

                /* create non-capturing group to match exactly one character */
                t.append('.');
            } else if (c == '[') {
                /****************************
                 * (4) GLOB PATTERN "[...]" *
                 ****************************/

                /* opening square bracket '[' */
                /* create non-capturing group to match exactly one character */
                /* inside the sequence */
                t.append('[');
                parserMode.push("[]");

                /* check for negation character '!' immediately after */
                /* the opening bracket '[' */
                if ((index < globLength) && (glob.charAt(index) == '!')) {
                    index++;
                    t.append('^');
                }
            } else if ((c == ']') && "[]".equals(parserMode.peek())) {
                /* closing square bracket ']' */
                t.append(']');
                parserMode.pop();
            } else if ((c == '-') && "[]".equals(parserMode.peek())) {
                /* character range '-' in "[...]" */
                t.append('-');
            } else if (c == '{') {
                /****************************
                 * (5) GLOB PATTERN "{...}" *
                 ****************************/

                /* opening curly brace '{' */
                /* create non-capturing group to match one of the */
                /* strings inside the sequence */
                t.append("(?:(?:");
                parserMode.push("{}");
            } else if ((c == '}') && "{}".equals(parserMode.peek())) {
                /* closing curly brace '}' */
                t.append("))");
                parserMode.pop();
            } else if ((c == ',') && "{}".equals(parserMode.peek())) {
                /* comma between strings in "{...}" */
                t.append(")|(?:");
            } else {
                /*************************
                 * (6) LITERAL CHARACTER *
                 *************************/

                /* convert literal character to a regex string */
                t.append(Pattern.quote(c + ""));
            }
        }
        /* done parsing all chars of the source pattern string */

        /* check for mismatched [...] or {...} */
        if ("[]".equals(parserMode.peek()))
            throw new PatternSyntaxException(
                    "Cannot find matching closing square bracket ] in GLOB expression",
                    glob, -1);

        if ("{}".equals(parserMode.peek()))
            throw new PatternSyntaxException(
                    "Cannot find matching closing curly brace } in GLOB expression",
                    glob, -1);

        String patternString = t.toString();
        return Pattern.compile(patternString);
    }
}
