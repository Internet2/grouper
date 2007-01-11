package edu.internet2.middleware.ldappcTest.ants;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

import java.io.File;

/**
 * This class is an Ant task that checks properties for use in the Ant build file.
 *
 * Some of the checking in this class depends on the existence of standard 
 * locations for files within the CVS repository.
 *
 */
 
public class PropertyCheckerTask extends Task 
{
    /**
     * The build stage to check
     */
    private String stage;

    /**
     * The project base directory
     */
    private File basedir;

    /**
     * The Ant project
     */
    private Project project;

    /**
     * This is the setter for stage.
     * @param stage The stage.
     */

    public void setStage(String stage)
    {
        this.stage  = stage;
    }

    /**
     * This is the getter for stage.
     * @return The current value of stage.
     */
    public String getStage()
    {
        return stage;
    }



    /*
     **
     ** Start Methods Section
     **
     */
    
    /**
     * The execute method implements the abstract method of the Task
     * class that this class extends.
     */ 

    public PropertyCheckerTask()
    {
    }
    
    /**
     * The execute method implements the abstract method of the Task
     * class that this class extends.
     */ 

    public void execute() throws BuildException 
    {
        String maxSeverity = "INFO";
        int numberOfLocationKeys = 0;
        int numberOfValueKeys = 0;
        String valueKey[][] = new String[50][2];
        String[][] locationKey = new String[50][2];

        if (stage.equals("build"))
        {
            //
            // Array of properties with locations of files and directories to check
            //
              
            //
            // The following are needed for initial setup and compilation
            //

            int i = 0;
            locationKey[i][0] = "master";
            locationKey[i][1] = "INFO";
            i++;

            locationKey[i][0] = "applicationConfigDir";
            locationKey[i][1] = "INFO";
            i++;

            locationKey[i][0] = "systemPropertiesFile";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "basedir.src";
            locationKey[i][1] = "WARNING (required if compiling)";
            i++;

            locationKey[i][0] = "basedir.lib";
            locationKey[i][1] = "WARNING";
            i++;

            locationKey[i][0] = "jvmbase";
            locationKey[i][1] = "ERROR";
            i++;

            //
            // The following are needed for the build classpath.
            //
            
            locationKey[i][0] = "basedir.lib.hsqldbJar";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "buildapp.conf";
            locationKey[i][1] = "ERROR";
            i++;

            //
            // The following are needed for testing.
            //

            locationKey[i][0] = "gsDataDir";
            locationKey[i][1] = "WARNING (required if doing standard testing)";
            i++;

            locationKey[i][0] = "sourcesXmlDir";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "sourcesXml";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "grouper.conf";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "grouper.hsqldb";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "signet.conf";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "signet.hsqldb";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "buildapp.lib";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "buildapp.classes";
            locationKey[i][1] = "ERROR";
            i++;

            locationKey[i][0] = "sqltoolDir";
            locationKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            numberOfLocationKeys = i;
        
            //
            // Array of properties with values to check
            //

            i = 0;
            valueKey[i][0] = "jvmbase";
            valueKey[i][1] = "ERROR";
            i++;

            valueKey[i][0] = "libraryDir";
            valueKey[i][1] = "ERROR";
            i++;

            valueKey[i][0] = "runDatabase";
            valueKey[i][1] = "INFO";
            i++;

            valueKey[i][0] = "hsqldbJar";
            valueKey[i][1] = "WARNING";
            i++;

            valueKey[i][0] = "database.port";
            valueKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            valueKey[i][0] = "sqltool.rc";
            valueKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            valueKey[i][0] = "signet.dbname";
            valueKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            valueKey[i][0] = "signet.database";
            valueKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            valueKey[i][0] = "grouper.dbname";
            valueKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            valueKey[i][0] = "grouper.database";
            valueKey[i][1] = "INFO (required if using runDatabase)";
            i++;

            valueKey[i][0] = "noStopDb";
            valueKey[i][1] = "INFO (not normally used)";
            i++;

            valueKey[i][0] = "gsDataDirGrouperClasspathLoc";
            valueKey[i][1] = "INFO";
            i++;

            valueKey[i][0] = "gsDataDirSignetClasspathLoc";
            valueKey[i][1] = "INFO";
            i++;

    
            numberOfValueKeys = i;
        }
        else
        {
            output("FAILURE, Stage not recoginized: " + stage);
        }
            basedir = getProject().getBaseDir();
            output("++++++++++ Property Checker Results for stage: " + stage + " ++++++++++");
            output("The base directory for the project is: " + basedir);
    
            //
            // Check that locations are available for files or directories.
            //
    
            output("+++");
            output("+++ File and Directory Properties +++");
            output("+++");
            for (int i = 0; i < numberOfLocationKeys; i++)
            {
                checkFileProperty(locationKey[i], maxSeverity);
            }
    
            //
            // Check whether value properties are set.
            //
            
            output("+++");
            output("+++ Value Properties +++");
            output("+++");
            for (int i = 0; i < numberOfValueKeys; i++)
            {
                checkValueProperty(valueKey[i], maxSeverity);
            }

            if (!maxSeverity.equals("WARNING"))
            {
                setPropertyAlert("***** One or more WARNINGS occurred; check the property checker output above.");
            }

            if (!maxSeverity.equals("ERROR"))
            {
                setPropertyAlert("***** ***** ***** One or more ERRORS were detected;"
                       + " check the property checker output above.");
            }

    }

    /**
     * Check File Properties 
     * @param valueKey An array of length two where the first element is
     * the key and the second is a message indicating the significance
     * of the message, such as INFO, WARNING, or ERROR. 
     */
    private void checkValueProperty(String[] valueKey, String maxSeverity)
    {
        boolean error = false;
        String key = valueKey[0];
        String value = getProject().getProperty(key);
        String severityLevel = valueKey[1];
        output(key + "=" + value);
        if (value == null)
        {
            output(">*** " + severityLevel + ", Property: " + key + " is not set.");
            error = true;
        }

        if (error)
        {
            if (!maxSeverity.equals("ERROR"))
            {
                if (severityLevel.indexOf("WARNING") != -1) maxSeverity = "WARNING";
            }
            if (severityLevel.indexOf("ERROR") != -1) maxSeverity = "ERROR";
        }
    }

    /**
     * Check File Properties 
     * @param locationKey An array of length two where the first element is
     * the key and the second is a message indicating the significance
     * of the message, such as INFO, WARNING, or ERROR. 
     */
    private void checkFileProperty(String[] locationKey, String maxSeverity)
    {
        boolean error = false;
        File file = null;
        String key = locationKey[0];
        String value = getProject().getProperty(key);
        String severityLevel = locationKey[1];

        output(key + "=" + value);
        if (value == null)
        {
            output(">*** " + severityLevel + ", Property: " + key + " is not set.");
            error = true;    
        }
        else
        {
            file = new File(value);
            if (!file.exists() || !file.isAbsolute())
            {
                file = new File(basedir + "/" + value);
            }

            if (file.exists())
            {
                if (file.isDirectory())
                {
                    output(">>> is an available directory.");
                }
                else if (file.isFile())
                {
                    output(">>> is an available file.");
                }
                else
                {
                    output("*** " + severityLevel + " above is NOT a file or directory.");
                    error = true;
                }
            }
            else
            {
                output(">*** " + severityLevel + ", file/directory above can not be found.");
                error = true;
            }
        }

        if (error)
        {
            if (!maxSeverity.equals("ERROR"))
            {
                if (severityLevel.indexOf("WARNING") != -1) maxSeverity = "WARNING";
            }
            if (severityLevel.indexOf("ERROR") != -1) maxSeverity = "ERROR";
        }
    }

    /**
     * Set property alert message - not used yet.
     * @param alertMessage An message indicating the property setting are 
     * suspect. 
     */
    public void setPropertyAlert(String alertMessage)
    {
        //
        // Set the alert property. 
        //

        // TODO: Fix problem with severity showing up as ERROR when it should not
        // and uncomment the following.
        //getProject().setProperty("propertyAlert", alertMessage);
    } 

    /**
     * Method to send a message to some output medium, currently
     * System.out.
     * @param message The message to be displayed.
     */       
    public void output(String message) 
    {
        System.out.println(message);
    }
}

