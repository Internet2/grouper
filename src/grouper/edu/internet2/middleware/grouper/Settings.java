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

package edu.internet2.middleware.grouper;

/** 
 * A (rudimentary and preliminary) settings object for the Groups
 * Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Settings.java,v 1.15 2007-02-08 16:25:25 blair Exp $
 * @since   1.0
 */
class Settings extends GrouperAPI {

  // TODO 20070207 technically this should be in the DAO only
  // PRIVATE CLASS CONSTANTS //
  private static final int  CURRENT_SCHEMA_VERSION  = 2;  //  == 1.2
                                                  //  1       == 1.0
                                                  //  0       == 0.9


  // PRIVATE CLASS PROPERTIES //
  private static Settings _s = null;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static int internal_getCurrentSchemaVersion() {
    return CURRENT_SCHEMA_VERSION;
  } // protected static int internal_getCurrentSchemaVersion()

  // @since   1.2.0
  protected static Settings internal_getSettings() {
    if (_s == null) {
      SettingsDTO dto = HibernateSettingsDAO.findSettings();
      _s = new Settings();
      _s.setDTO(dto);
    }
    return _s;
  } // protected static Settings internal_getSettings()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected SettingsDTO getDTO() {
    return (SettingsDTO) super.getDTO();
  } // protected SettingsDTO getDTO()

  protected int getSchemaVersion() {
    return this.getDTO().getSchemaVersion();
  } // protected int getSchemaVersion()

} // class Settings extends GrouperAPI

