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

import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;

/** 
 * A (rudimentary and preliminary) settings object for the Groups
 * Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Settings.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 *     
*/
class Settings implements Serializable {

  // Private Class Constants //
  private static final EventLog EL                      = new EventLog();
  private static final Log      LOG                     = LogFactory.getLog(Settings.class);
  private static final int      CURRENT_SCHEMA_VERSION  = 1;  // == 1.0
                                                      //  0      == 0.9

  // Private Transient Class Properties //
  private static transient Settings _s = null;

  // Hibernate Properties //
  private String  id;
  private int     schemaVersion;


  // Constructors //
  private Settings() {
    // For Hibernate
  } // private Settings()

  protected Settings(int version) {
    this.setSchemaVersion(version);
  } // protected Settings(version)


  // Protected Class Methods //
  protected static int getCurrentSchemaVersion() {
    return CURRENT_SCHEMA_VERSION;
  } // protected static int getCurrentSchemaVersion()

  protected static Settings getSettings() {
    String msg = "unable to retrieve settings: ";
    if (_s == null) {
      try {
        Session hs  = HibernateHelper.getSession();
        Query   qry = hs.createQuery("from Settings");
        List    l   = qry.list();
        if (l.size() == 1) {
          _s = (Settings) l.get(0);
        }
        hs.close();
      }
      catch (Exception e) {
        msg += e.getMessage(); // update the error message
      }
    }
    if (_s == null) {
      LOG.fatal(msg);
      throw new RuntimeException(msg);
    }
    return _s;
  } // protected static Settings getSettings()


  // Getters //
  protected String getId() {
    return this.id;
  }
  protected int getSchemaVersion() {
    return this.schemaVersion;
  }


  // Setters //
  protected void setId(String id) {
    this.id = id;
  }
  protected void setSchemaVersion(int version) {
    this.schemaVersion = version;
  }

}

