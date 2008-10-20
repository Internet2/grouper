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
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Install the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInitializeSchema.java,v 1.6 2008-10-20 17:51:23 mchyzer Exp $    
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
    initializeSchema(true);
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
      System.err.println("Based on grouper.properties: " + "ddlutils.schemaexport.dropThenCreate" + "=" + isDropBeforeCreate());
      System.err.println("Based on grouper.properties: " + "ddlutils.schemaexport.writeAndRunScript" + "=" + isWriteAndRunScript());
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
  
  /**
   * if we should drop everything before creating stuff
   * @return the deleteBeforeCreate
   */
  public static boolean isDropBeforeCreate() {
    return GrouperConfig.getPropertyBoolean("ddlutils.schemaexport.dropThenCreate", true);
  }
  
  /**
   * if we should run this after writing it
   * @return the writeAndRunScript
   */
  public static boolean isWriteAndRunScript() {
    return GrouperConfig.getPropertyBoolean("ddlutils.schemaexport.writeAndRunScript", false);
  } 

}

