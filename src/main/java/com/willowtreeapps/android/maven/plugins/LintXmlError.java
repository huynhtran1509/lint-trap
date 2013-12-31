package com.willowtreeapps.android.maven.plugins;

/**
 * User: charlie Date: 8/14/12 Time: 3:52 PM
 */
public class LintXmlError {

    public LintXmlError() {
        location = new ErrorLocation();
    }

    public String severity;
    public String errorCheck;
    public String message;
    public ErrorLocation location;
}
