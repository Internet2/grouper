/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: GrouperEngineBuiltin.java,v 1.2 2009-02-09 05:33:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * built in grouper engines.  Note, if we prefix builtins with "grouper"
 * there will not be conflicts with custom engines
 */
public enum GrouperEngineBuiltin implements GrouperEngineIdentifier {

  /** gsh engine */
  GSH("grouperShell"), 
  
  /** default group ui engine */
  UI("grouperUI"), 
  
  /** web service engine */
  WS("grouperWS"), 

  /** loader engine */
  LOADER("grouperLoader"), 

  /** ldappc engine */
  LDAPPC("grouperLdappc"),

  /** ldappc engine */
  IMPORT("grouperImport"),

  /** usdu engine engine */
  USDU("grouperUsdu"),

  /** junit engine */
  JUNIT("grouperJunit");

  /**
   * have the label different from name
   * @param theGrouperEngine
   */
  private GrouperEngineBuiltin(String theGrouperEngine) {
    this.grouperEngine = theGrouperEngine;
  }
  
  /**
   * string that goes to the DB
   */
  private String grouperEngine;

  /**
   * @see edu.internet2.middleware.grouper.audit.GrouperEngineIdentifier#getGrouperEngine()
   */
  public String getGrouperEngine() {
    return this.grouperEngine;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperEngineBuiltin valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    return valueOfIgnoreCase(string, exceptionOnNull, true);
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @param exceptionIfNotFound if string isnt found should there be an exception
   * @return the enum or null or exception if not found
   */
  public static GrouperEngineBuiltin valueOfIgnoreCase(String string, boolean exceptionOnNull, boolean exceptionIfNotFound) {
    
    //check the string
    for (GrouperEngineBuiltin grouperEngineBuiltin : values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperEngineBuiltin.grouperEngine)) {
        return grouperEngineBuiltin;
      }
    }
    
    return GrouperUtil.enumValueOfIgnoreCase(GrouperEngineBuiltin.class, 
        string, exceptionOnNull, exceptionIfNotFound);

  }

}
