package org.sonatype.mavenbook.plugins;

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: charlie Date: 8/14/12 Time: 3:29 PM
 */
public class LintInterpreter {

    private static Pattern scanning = Pattern.compile("^Scanning ");
    private static Pattern path = Pattern.compile("^([^:]+?):(\\d{1,}): (\\w+): (.+?)\\[(.+)\\]");
    private static Pattern affected = Pattern.compile("^(?:\\s*)(.+)");

    private Log log;

    private ArrayList<String> excludeFilters;

    public LintInterpreter() {
        this.excludeFilters = new ArrayList<String>();
    }

    public List<LintError> parse(BufferedReader reader) {
        ArrayList<LintError> errors = new ArrayList<LintError>();
        LintError current = new LintError();
        String line = "";
        int state = 0;
        try
        {
            while((line = reader.readLine()) != null)
            {
                //Skip any whitespace or scanning
                if(line.equals(""))
                    continue;
                if(scanning.matcher(line).find())
                    continue;

                Matcher m;
                log.info(line);
                switch(state)
                {
                    case 0:
                        m = path.matcher(line);
                        if(m.find())
                        {
                            current.path = m.group(1);
                            current.lineNum = Integer.parseInt(m.group(2));
                            current.severity = m.group(3);
                            current.error = m.group(4);
                            current.errorCheck = m.group(5);
                            state = 1;
                        }
                        break;
                    case 1:
                        m = affected.matcher(line);
                        m.find();
                        current.affectedCode = m.group(1);
                        boolean exclude = false;
                        for(int a=0;a<excludeFilters.size();a++)
                        {
                            if(current.path.startsWith(excludeFilters.get(a)))
                            {
                                exclude = true;
                                break;
                            }
                        }
                        if((!exclude) && current.severity.equalsIgnoreCase("error"))
                        {
                            errors.add(current);
                        }
                        current = new LintError();
                        state = 2;
                        break;
                    case 2:
                        state = 0;
                        break;
                }
            }
        }
        catch(Exception e)
        {
            log.error("Problem parsing errors",e);
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
}
