package com.willowtreeapps.android.maven.plugins;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * User: charlie Date: 8/14/12 Time: 3:29 PM
 */
public class LintXmlInterpreter {

    private static Pattern scanning = Pattern.compile("^Scanning ");
    private static Pattern path = Pattern.compile("^([^:]+?):(\\d{1,}): (\\w+): (.+?)\\[(.+)\\]");
    private static Pattern affected = Pattern.compile("^(?:\\s*)(.+)");

    private Log log;
    private boolean logWarnings;

    private ArrayList<String> excludeFilters;

    public LintXmlInterpreter() {
        this.excludeFilters = new ArrayList<String>();
    }

    public List<LintXmlError> parse(File file) {
        List<LintXmlError> errors = new ArrayList<LintXmlError>();
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XmlHandler myHandler = new XmlHandler();
            saxParser.parse(file,myHandler);
            ArrayList<LintXmlError> tempErrors = myHandler.getErrors();

            for(LintXmlError temp : tempErrors)
            {
                boolean include = true;
                if(temp.location == null || temp.location.file == null)
                    continue;

                for(String filter : excludeFilters)
                {
                    if(temp.location.file.startsWith(filter))
                    {
                        include = false;
                        break;
                    }
                }

                if(!include)
                    continue;

                if(!logWarnings && temp.severity.equalsIgnoreCase("warning"))
                    continue;

                errors.add(temp);

            }
        }
        catch(Exception e)
        {
            log.error("Parsing failed",e);
        }
        return errors;
    }

    public void addExclusionFilter(String filter)
    {
        this.excludeFilters.add(filter);
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public void logWarnings(boolean yes)
    {
        logWarnings = yes;
    }
}
