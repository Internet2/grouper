/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;

/** 
 * A (rudimentary and preliminary) settings object for the Groups
 * Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Settings.java,v 1.11 2006-12-27 18:22:21 blair Exp $
 * @since   1.0
 */
class Settings {

  // PRIVATE CLASS CONSTANTS //
  private static final int  CURRENT_SCHEMA_VERSION  = 2;  //  == 1.2
                                                  //  1       == 1.0
                                                  //  0       == 0.9


  // PRIVATE CLASS PROPERTIES //
  private static Settings _s = null;


  // HIBERNATE PROPERTIES //
  private String  id;
  private int     schemaVersion;


  // CONSTRUCTORS //

  // @since 1.0
  private Settings() {
    // For Hibernate
  } // private Settings()

  // @since 1.0
  protected Settings(int version) {
    this.setSchemaVersion(version);
  } // protected Settings(version)


  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static int getCurrentSchemaVersion() {
    return CURRENT_SCHEMA_VERSION;
  } // protected static int getCurrentSchemaVersion()

  // @since   1.2.0
  protected static Settings internal_getSettings() 
    throws  GrouperRuntimeException
  {
    String msg = E.SETTINGS;
    if (_s == null) {
      try {
        _s = HibernateRegistryDAO.findSettings();
      }
      catch (GrouperException eGE) {
        msg += eGE.getMessage(); // update the error message
      }
    }
    if (_s == null) {
      ErrorLog.fatal(Settings.class, msg);
      throw new GrouperRuntimeException(msg);
    }
    return _s;
  } // protected static Settings internal_getSettings()


  // GETTERS //
  // @since 1.0
  protected String getId() {
    return this.id;
  }
  // @since 1.0
  protected int getSchemaVersion() {
    return this.schemaVersion;
  }


  // SETTERS //
  // @since 1.0
  protected void setId(String id) {
    this.id = id;
  }
  // @since 1.0
  protected void setSchemaVersion(int version) {
    this.schemaVersion = version;
  }

}

