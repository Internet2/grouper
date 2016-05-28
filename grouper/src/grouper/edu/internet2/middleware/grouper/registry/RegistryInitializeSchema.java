/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.registry;
import java.io.File;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlVersionable;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Install the Groups Registry.
 * 
 * @author  blair christensen.
 * @version $Id: RegistryInitializeSchema.java,v 1.17 2009-04-28 20:08:08 mchyzer Exp $    
 * @since   1.2.0
 */
public class RegistryInitializeSchema {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(RegistryInitializeSchema.class);
  
  /** if initting */
  public static boolean    inInitSchema = false;

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      if (args == null || args.length == 0 || args[0].equals("-h")) {
        System.out.println(_getUsage());
        return;
      }
      GrouperStartup.ignoreCheckConfig = true;
      boolean callFromCommandLine = true;
      boolean fromUnitTest = false;
      boolean theCompareFromDbVersion = true;
      boolean theDropBeforeCreate = false;
      boolean theWriteAndRunScript = false;
      boolean dropOnly = false;
      boolean runReset = false;
      boolean runForTests = false;
      boolean runSqlFile = false;
      boolean runBootstrap = true;
      boolean installDefaultGrouperData = true;
      Map<String, DdlVersionable> maxVersions = null;
      boolean promptUser = true;
      HashSet<String> argsSet = new HashSet<String>();
      for (int i = 0; i < args.length; i++) {
        argsSet.add(args[i].toLowerCase());
      }
      if (argsSet.contains("-drop")) {
        theDropBeforeCreate = true;
      }
      if (argsSet.contains("-deep")) {
        GrouperDdlUtils.deepCheck = true;
      }
      if (argsSet.contains("-dontinstall")) {
        installDefaultGrouperData = false;
      }
      if (argsSet.contains("-droponly")) {
        dropOnly = true;
      }
      if (argsSet.contains("-noprompt")) {
        promptUser = false;
      }
      if (argsSet.contains("-runscript")) {
        theWriteAndRunScript = true;
      }
      if (argsSet.contains("-check")) {
      }
      if (argsSet.contains("-reset")) {
        runReset = true;
        RegistryReset.reset(promptUser, true);
        runBootstrap = false;
      }
      if (argsSet.contains("-fortests")) {
        runForTests = true;
        initializeSchemaForTests();
        runBootstrap = false;
      }
      if (argsSet.contains("-runsqlfile")) {
        if (promptUser) {
          String prompt = "run the sql file";
         
         //make sure it is ok to change db
         GrouperUtil.promptUserAboutDbChanges(prompt, true);
     
        }
        runSqlFile = true;
        String fileName = GrouperUtil.argAfter(args, "-runsqlfile");
        if (StringUtils.isBlank(fileName)) {
          System.out.println("Specify a fileName after -runsqlfile");
          System.exit(1);
        }
        File file = new File(fileName);
        if (!file.exists()) {
          System.out.println("File does not exist: " + GrouperUtil.fileCanonicalPath(file));
          System.exit(1);
        }
        GrouperDdlUtils.sqlRun(file, false, true);
        runBootstrap = false;
      }

      LOG.debug("theDropBeforeCreate? " + theDropBeforeCreate
          + ", theWriteAndRunScript? " + theWriteAndRunScript + ", dropOnly? "
          + dropOnly + ", installDefaultGrouperData? "
          + installDefaultGrouperData + ", promptUser? " + promptUser);
      inInitSchema = true;
      try {
        //dont run from startup, run from here
        GrouperStartup.runDdlBootstrap = false;
        
        //set vars so nothing else happens...
        GrouperDdlUtils.compareFromDbDllVersion = false;
        
        GrouperStartup.ignoreCheckConfig = true;
        
        try {
          GrouperStartup.logErrorStatic = false;
          GrouperStartup.startup();
        } catch (Exception e) {
          //ignore
          LOG.debug(e);
        } finally {
          GrouperStartup.logErrorStatic = true;
        }
        
        if (runBootstrap) {
          //run the bootstrap
          GrouperDdlUtils.bootstrapHelper(callFromCommandLine, 
                                      fromUnitTest, 
                                      theCompareFromDbVersion, 
                                      theDropBeforeCreate, 
                                      theWriteAndRunScript, 
                                      dropOnly, 
                                      installDefaultGrouperData, 
                                      maxVersions, 
                                      promptUser);
        }
        
        if (!dropOnly && (theWriteAndRunScript || runReset || runSqlFile || runForTests)) {
          //start again
          GrouperStartup.started = false;
          GrouperStartup.ignoreCheckConfig = false;
          //now check config - assuming we are not just dropping or we have used -ddlonly
          GrouperStartup.startup();
        }
        
        
      } catch (Throwable t) {
        t.printStackTrace();
        throw new RuntimeException(t);
      } finally {
        inInitSchema = false;
      }

    } finally {
      GrouperDdlUtils.deepCheck = false;
    }
  }

  /**
   * @param fromCommandLine if called from command line
   */
  public static void initializeSchema(boolean fromCommandLine) {
    inInitSchema = true;
    try {
      //dont run from startup, run from here
      GrouperStartup.runDdlBootstrap = false;
      
      //NOTE, dont make any calls other than outside of grouper until we call startup...
      System.err.println("Based on grouper.properties: " + "ddlutils.schemaexport.installGrouperData" + "=" + isInstallGrouperData());
      
      //set vars so nothing else happens...
      GrouperDdlUtils.compareFromDbDllVersion = false;
      
      GrouperStartup.ignoreCheckConfig = true;
      
      GrouperStartup.startup();
      
      //run the bootstrap
      GrouperDdlUtils.bootstrap(fromCommandLine, isInstallGrouperData(), true);

      //now check config
      GrouperCheckConfig.checkConfig();
      
    } catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t);
    } finally {
      inInitSchema = false;
    }
  }

  /**
   * init ddl for tests (drop and create, run now, etc)
   */
  public static void initializeSchemaForTests() {
    inInitSchema = true;
    try {
      //dont run from startup, run from here
      GrouperStartup.runDdlBootstrap = false;

      GrouperStartup.ignoreCheckConfig = true;
      
      try {
        GrouperStartup.startup();
      } catch (Exception e) {
        LOG.error(e);
      }
      
      GrouperDdlUtils.bootstrapHelper(true, false, true, true, true, false, true, null, true);
      
      //everything right version
      GrouperDdlUtils.everythingRightVersion = true;
      
      //now check config
      GrouperCheckConfig.checkConfig();

    } finally {
      inInitSchema = false;
    }

  }
  
  /**
   * if schema should be initted after schemaexport
   * @return the inInitSchema
   */
  public static boolean isInstallGrouperData() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("ddlutils.schemaexport.installGrouperData", true);
  }
  
  /**
   * 
   * @return string
   */
  private static String _getUsage() {
    return "Usage:"
        + GrouperConfig.NL
        + "args: -h,            Prints this message"
        + GrouperConfig.NL
        + "args: [-reset] [-dontinstall] [-drop] [-droponly] [-check] [-deep] "
        + GrouperConfig.NL
        + "      [-runscript] [-noprompt] [-runsqlfile fileName]"
        + GrouperConfig.NL

        + "  -check,            Verifies status of the registry based on DDL version number"
        + GrouperConfig.NL
        + "  -deep,             Verifies status of the registry based on database objects"
        + GrouperConfig.NL
        + "                     Note, this will alway generate a script, but might not do"
        + GrouperConfig.NL
        + "                     much"
        + GrouperConfig.NL
        + "  -reset,            Drops all data and re-inserts essential"
        + GrouperConfig.NL
        + "                     Grouper data e.g. root stem and fields"
        + GrouperConfig.NL
        + "  -dontinstall,      Will not make sure all default data is"
        + GrouperConfig.NL
        + "                     in registry (e.g. root stem)"
        + GrouperConfig.NL
        + "  -drop,             Drops all Grouper schema elements before recreating"
        + GrouperConfig.NL
        + "  -droponly,         Drops all Grouper elements, does not recreate"
        + GrouperConfig.NL
        + "  -runscript,        Will run the generated DDL script after writing to a file"
        + GrouperConfig.NL
        + "  -runsqlfile fileName,       Will run a file of sql in the location of fileName"
        + GrouperConfig.NL
        + "  -noprompt,         Do not ask user to confirm/approve which database is "
        + GrouperConfig.NL
        + "                     affected"
        + GrouperConfig.NL
        + GrouperConfig.NL
        + "NOTE WELL: no registry changes will be effected unless the -runscript or -runsqlfile option"
        + GrouperConfig.NL + "is selected, except for -runsqlfile, and -reset" + GrouperConfig.NL;
  } // private static String _getUsage()

}

