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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Install the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistryInitializeSchema.java,v 1.2 2008-07-27 07:37:24 mchyzer Exp $    
 * @since   1.2.0
 */
public class RegistryInitializeSchema {

  /** if initting */
  public static boolean inInitSchema = false;
  
  /**
   * @param args
   * @throws Throwable 
   */
  public static void main(String[] args) throws Throwable {
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
      
      GrouperStartup.startup();
      
      //make sure it is ok to change db
      GrouperUtil.promptUserAboutDbChanges("schemaexport all tables", true);
  
      //run the bootstrap
      GrouperDdlUtils.bootstrap(true, isInstallGrouperData());
      
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    } finally {
      inInitSchema = false;
    }
  }

  
  /**
   * if schema should be initted after schemaexport
   * @return the inInitSchema
   */
  private static boolean isInstallGrouperData() {
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

