package com.willowtreeapps.android.maven.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which runs the android-lint tool against a maven project
 *
 * @goal run-lint
 *
 */
public class LintMojo
    extends AbstractMojo
{
    /**
     * Location of the project directory 
     * @parameter property="basedir"
     * @required
     */
    private File projectDirectory;

    /**
     * Location of the project directory
     * @parameter expression="${project.build.directory}/"
     * @required
     */
    private File buildDirectory;

    /**
     * A set of checks to perform through lint. Defaults to all. Note that any Ignores elements are ignored if this element is present
     * @parameter alias="checks"
     */
    private ArrayList<String> mChecks;

    /**
     * A set of checks to ignore through lint. Note that the presence of Checks elements will cause this to be ignored
     * @parameter alias="ignores"
     */
    private ArrayList<String> mIgnores;

    /**
     * A set of directories to ignore. The script searches the start of the path so partial paths are valid 
     * @parameter alias="exclusions"
     */
    private ArrayList<String> mExclusions;

    /**
     * The minimum logging level.(Warning,Error) Defaults to Error
     * @parameter alias="minLogLevel"
     *  default-value="Error"
     */
    private String mlogLevel;

    /**
     * The error level to fail builds on (Warning, Error, Never) Defaults to Error
     * @parameter alias="minErrorLevel"
     *  default-value="Error"
     */
    private String mErrorLevel;

    private String errorFormat ="Lint error found!\n\tFile: %1$s\n\tLine: %2$s\n\tColumn: %3$s\n"
            + "\tSeverity: %4$s\n\tCheck: %5$s\n\tMessage: %6$s\n";
    private boolean broke = false;
    List<LintXmlError> errors;
    private String lintLogFile = "lintlog.xml";

    public static final String ENV_ANDROID_HOME = "ANDROID_HOME";

    public void execute()
        throws MojoExecutionException
    {
        if(mExclusions == null || mExclusions.size() ==0)
        {
            mExclusions = new ArrayList<String>();
            mExclusions.add("target/unpack/apklibs/");
        }


        try
        {
            //Check to make sure the classpath exists for lint to run
            File f = new File(projectDirectory.getAbsolutePath()+"/.classpath");
            if(!f.exists())
            {
                throw new MojoExecutionException("Eclipse classpath not found");
            }
            ArrayList<String> params = new ArrayList<String>();

            //Check to make sure we can find lint
            final String androidHome = System.getenv(ENV_ANDROID_HOME);
            if ("".equals(androidHome) || androidHome == null) {
                throw new MojoExecutionException("No Android SDK path could be found. You may configure it" +
                        " by setting environment variable " + ENV_ANDROID_HOME);
            }

            //Create out directory in the build directory
            File lintFile = new File(buildDirectory + "/linttrap/"+ lintLogFile);
            lintFile.getParentFile().mkdirs();

            //Set up lint to output into the correct directory
            String lintPath = androidHome+"/tools/lint";
            if (System.getProperty("os.name").toLowerCase().indexOf("win")>=0){
                lintPath+=".bat"; //.bat for windows
            }
            params.add(lintPath);
            params.add("--xml");
            params.add(lintFile.getAbsolutePath());

            //Add the checks or exclusions
            if(mChecks != null && mChecks.size() > 0)
            {
                params.add("--check");
                String checks = "";
                for(int a=0; a<mChecks.size(); a++)
                {
                    checks += mChecks.get(a)+",";
                }
                checks = checks.substring(0,checks.length()-1);
                params.add(checks);
            }else if(mIgnores != null && mIgnores.size()>0)
            {
                params.add("--disable");
                String disables = "";
                for(int a=0; a<mIgnores.size(); a++)
                {
                    disables += mIgnores.get(a)+",";
                }
                disables = disables.substring(0,disables.length()-1);
                params.add(disables);
            }
            params.add(".");
            ProcessBuilder pb = new ProcessBuilder(params);
            pb.directory(projectDirectory);

            getLog().info("Beginning lint Run");
            getLog().info("Execution: " + StringUtils.join(params.iterator(), " "));
            Process p = pb.start();

            //wait for the process to exit
            p.waitFor();

            //Parse the resulting XML
            LintXmlInterpreter interpreter = new LintXmlInterpreter();
            interpreter.setLog(getLog());
            if(mlogLevel.equalsIgnoreCase("warning"))
            {
                interpreter.logWarnings(true);
            }
            for(int a=0; a<mExclusions.size(); a++)
            {
                interpreter.addExclusionFilter(mExclusions.get(a));
            }
            errors = interpreter.parse(lintFile);

            //For each error check to see if there are any errors/warnings that should cause the build to fail
            LintXmlError error;
            for(int a=0; a< errors.size(); a++)
            {
                error = errors.get(a);
                String errorLine = String.format(errorFormat,error.location.file,error.location.line,error.location.column,error.severity,error.errorCheck,error.message);

                if(error.severity.equalsIgnoreCase("warning"))
                {
                    getLog().warn(errorLine);
                }
                else if(error.severity.equalsIgnoreCase("error"))
                {
                    getLog().error(errorLine);
                }

                //Check to see if it's broken
                if(!broke)
                {
                    if(mErrorLevel.equalsIgnoreCase("warning"))
                    {
                        broke = true;
                    }
                    else if(mErrorLevel.equalsIgnoreCase("error") && error.severity.equalsIgnoreCase("error"))
                    {
                        broke = true;
                    }
                }
            }

            getLog().info("Ending lint Run");
        }
        catch(Exception e)
        {
            getLog().error("Lint interpreter crashed!",e);
            broke = true;
        }

        if(broke)
        {
            throw new MojoExecutionException(errors.size() + " Lint errors found during build");
        }

    }
}
