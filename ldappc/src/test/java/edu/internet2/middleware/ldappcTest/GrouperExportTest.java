/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package edu.internet2.middleware.ldappcTest;

import junit.framework.TestCase;

import edu.internet2.middleware.subject.Subject;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.XmlExporter;
import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappcTest.DisplayTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.util.Properties;

/**
 * Class for doing a Grouper export
 * @author Gil Singer 
 */
public class GrouperExportTest extends BaseTestCase
{

    /**
     * the PrintWriter for outputing the exported data
     */
    private PrintWriter printWriter;

    /**
     * Exporter of grouper data
     */
    private XmlExporter xmlExporter;

    /**
     * Constructor
     */
    public GrouperExportTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName()); 

        Properties exportProperties = new Properties();
        
        try
        {
            //InputStream is = GrouperExportTest.class.getResourceAsStream("export.properties");
            InputStream is = GrouperExportTest.class.getResourceAsStream("/edu/internet2/middleware/ldappcTest/configuration/data/export.properties");
            
            if (is == null)
            {
                ErrorLog.fatal(this.getClass(),"Failed to find the export properties file; null input stream.");
                
            }
            exportProperties.load(is);
        }    
        catch(IOException ioe)
        {
            ErrorLog.fatal(this.getClass(),"Failed to find the export properties file.  " + ioe.getMessage());
        }
        
        /* The above input stream will fill the properties files similarly to
           the following lines of code:
        exportProperties.setProperty("export.metadata","true");
        exportProperties.setProperty("export.data","true");
        exportProperties.setProperty("export.privs.naming","true");
        exportProperties.setProperty("export.privs.access","true");
        exportProperties.setProperty("export.group.members","true");
        exportProperties.setProperty("export.group.lists","true");
        exportProperties.setProperty("export.group.internal-attributes","true");
        exportProperties.setProperty("export.group.custom-attributes","true");
        exportProperties.setProperty("export.stem.internal-attributes","true");
        exportProperties.setProperty("export.privs.for-parents","true");
        */
        File exportOutputFile = null;
        FileWriter fileWriter = null;
        
        try
        {
            exportOutputFile = new File("./output/exportOutputFile");
            fileWriter = new FileWriter(exportOutputFile);
        }
        catch(IOException ioe2)
        {
            ErrorLog.fatal(this.getClass(),"Failed to create the export properties file.  " + ioe2.getMessage());
        }
        
        printWriter = new PrintWriter( new BufferedWriter(fileWriter) );

        // 
        // Get a Grouper session for Grouper1.1, which now requires one for the
        // XmlExporter constructor.
        //
        
        GrouperSubjectRetriever subjectRetriever = new GrouperSubjectRetriever();
        Subject subject = subjectRetriever.findSubjectById("GrouperSystem");
        assertNotNull(subject);
        if (subject != null)
        {
            assertEquals("GrouperSystem", subject.getId());
            assertEquals("GrouperSystem", subject.getName());
        }
        
        GrouperSession grouperSession = null;
        try
        {
            grouperSession = GrouperSession.start(subject);
        }
        catch(SessionException se)
        {
            ErrorLog.fatal(this.getClass(),"could not create GrouperSession.  " + se.getMessage());
        }        
        
        try
        {      
            // Use next line for Grouper 1.0
            //xmlExporter = new XmlExporter(exportProperties);
            // Use next line for Grouper 1.1
            xmlExporter = new XmlExporter(grouperSession, exportProperties);
        }
        catch(Exception e)
        {
            ErrorLog.fatal(this.getClass(),"Failed to create xmlExporter.  " + e.getMessage());
         }
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
        BaseTestCase.runTestRunner(GrouperExportTest.class);
    }
    

    /**
     * A test of Grouper export capability.
     */
    public void testGrouperExport() 
    {
        DisplayTest.showRunTitle("testGrouperExport", "Grouper export run.");

        GrouperSubjectRetriever subjectRetriever = new GrouperSubjectRetriever();
        Subject subject = subjectRetriever.findSubjectById("GrouperSystem");
        assertNotNull(subject);
        if (subject != null)
        {
            assertEquals("GrouperSystem", subject.getId());
            assertEquals("GrouperSystem", subject.getName());
        }
        
        GrouperSession grouperSession = null;
        try
        {
            grouperSession = GrouperSession.start(subject);
        }
        catch(SessionException se)
        {
            ErrorLog.fatal(this.getClass(),"could not create GrouperSession.  " + se.getMessage());
        }
        
        try
        {
            // Use the next line for Grouper 1.0
            //xmlExporter.export(grouperSession, printWriter);
            // Use the next line for Grouper 1.0
            xmlExporter.export(printWriter);
        }
        catch(Exception e)
        {
            ErrorLog.fatal(this.getClass(),"could not create GrouperSession.  " + e.getMessage());
        }
    }
}
        




