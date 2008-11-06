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
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlVersionable;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Install the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInitializeSchema.java,v 1.8 2008-11-06 17:08:23 isgwb Exp $    
 * @since   1.2.0
 */
public class RegistryInitializeSchema {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(RegistryInitializeSchema.class);
  
  /** if initting */
  public static boolean inInitSchema = false;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
	  if(args==null || args.length==0 || args[0].equals("-h")) {
		  System.out.println(_getUsage());
		  return;
	  }
	  GrouperStartup.ignoreCheckConfig = true;
	  boolean callFromCommandLine=true;
	  boolean fromUnitTest = false;
      boolean theCompareFromDbVersion=true;
      boolean theDropBeforeCreate=false; 
      boolean theWriteAndRunScript=true;
      boolean dropOnly = false; 
      boolean installDefaultGrouperData=true;
      Map<String, DdlVersionable> maxVersions = null;
	  boolean promptUser=true;
      HashSet<String> argsSet = new HashSet<String>();
      for (int i=0;i<args.length;i++) {
    	  argsSet.add(args[i].toLowerCase());
      }
      if(argsSet.contains("-drop")) {
    	  theDropBeforeCreate=true;
      }
      if(!argsSet.contains("-install") && theDropBeforeCreate) {
    	  dropOnly=true;
    	  installDefaultGrouperData=false;
      }
      if(argsSet.contains("-noprompt")) {
    	  promptUser=false;
      }
      if(argsSet.contains("-ddlonly")) {
    	  theWriteAndRunScript=false;
      }
      if(argsSet.contains("-check")) {	  
    	  GrouperCheckConfig.checkConfig();
      }
      if(argsSet.contains("-reset")) {
		  RegistryReset.reset(promptUser);
		  return;
	  }
      if(argsSet.contains("-fortests")) {
		  initializeSchemaForTests();
		  return;
	  }
	 
      
    initialize(callFromCommandLine, fromUnitTest, theCompareFromDbVersion, theDropBeforeCreate, theWriteAndRunScript, dropOnly, installDefaultGrouperData, maxVersions, promptUser);
  }

  public static void initialize(boolean callFromCommandLine, boolean fromUnitTest,
	      boolean theCompareFromDbVersion, boolean theDropBeforeCreate, boolean theWriteAndRunScript,
	      boolean dropOnly, boolean installDefaultGrouperData, Map<String, DdlVersionable> maxVersions,
	      boolean promptUser) {
	    inInitSchema = true;
	    try {
	      //dont run from startup, run from here
	      GrouperStartup.runDdlBootstrap = false;
	      
	      //NOTE, dont make any calls other than outside of grouper until we call startup...
	      System.err.println("Based on grouper.properties: " + "ddlutils.schemaexport.installGrouperData" + "=" + isInstallGrouperData());
	      
	      //set vars so nothing else happens...
	      GrouperDdlUtils.compareFromDbDllVersion = false;
	      
	      GrouperStartup.ignoreCheckConfig = true;
	      
	      //GrouperStartup.startup();
	      
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
	      
	      /*
	       Trying to work around NoClassDefFoundError - without success 
	       if(installDefaultGrouperData && !dropOnly && theWriteAndRunScript) {
	    	  //register hib objects
	          	Hib3DAO.initHibernateIfNotInitted();
		    	//run the bootstrap
		          //GrouperDdlUtils.bootstrap(false, false, false);
		          RegistryInstall.install();
		  }*/
	      
	      if(!dropOnly && theWriteAndRunScript) {
	      //now check config - assuming we are not just dropping or we have used -ddlonly
	    	  GrouperCheckConfig.checkConfig();
	      }
	      
	      
	    } catch (Throwable t) {
	      t.printStackTrace();
	      throw new RuntimeException(t);
	    } finally {
	      inInitSchema = false;
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

      GrouperStartup.startup();

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
    return GrouperConfig.getPropertyBoolean("ddlutils.schemaexport.installGrouperData", true);
  }
  
  private static String _getUsage() {
	    return  "Usage:"                                                                + GrouperConfig.NL
	            + "args: -h,            Prints this message"                            + GrouperConfig.NL
	            + "args: [(-reset | -install [-drop])] [-drop] [-check] "               + GrouperConfig.NL
	            + "      [-ddlonly] [-noprompt]"                                        + GrouperConfig.NL
	            
	            + "  -check,            Verifies status of the registry"                + GrouperConfig.NL
	            + "  -reset,            Drops all data and re-inserts essential"        + GrouperConfig.NL
	            + "                     Grouper data e.g. root stem and fields"         + GrouperConfig.NL
	            + "  -install,          If required, installs the schema. By itself"    + GrouperConfig.NL
	            + "                     this option does not 'drop' an existing "       + GrouperConfig.NL
	            + "                     registry"                                       + GrouperConfig.NL
	            + "  -drop,             Drops all Grouper schema elements"              + GrouperConfig.NL
	             
	            + "  -ddlonly,          Writes to a file the DDL required to install"   + GrouperConfig.NL
	            + "                     or drop the Grouper schema, but does not run it"+ GrouperConfig.NL
	           
	            + "  -noprompt,         Do not ask user to confirm dstructive actions"  + GrouperConfig.NL
	            ;
	  } // private static String _getUsage()

  
}

