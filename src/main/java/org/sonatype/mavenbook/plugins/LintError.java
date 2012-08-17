package org.sonatype.mavenbook.plugins;

/**
 * User: charlie Date: 8/14/12 Time: 3:52 PM
 */
public class LintError {
    public String path;
    public int lineNum;
    public String error;
    public String severity;
    public String errorCheck;
    public String affectedCode;
}
