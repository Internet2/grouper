/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Pennsylvania

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

import org.apache.commons.lang.StringUtils;

/**
 * save mode for static saves.  insert only (exception if update),
 * update only (exception on insert), and insert or update doesnt matter.
 * @author mchyzer
 *
 */
public enum SaveMode {
  
  /** insert only, if exists then exception */
  INSERT {
  
    /**
     * if allowed to update
     * @return true if allowed to update
     */
    @Override
    public boolean allowedToUpdate() {
      return false;
    }
    
    /**
     * if allowed to insert
     * @return true if allowed to insert
     */
    @Override
    public boolean allowedToInsert() {
      return true;
    }
  }, 
  
  /** update only, if not exist, then exception */
  UPDATE {
     
    /**
     * if allowed to update
     * @return true if allowed to update
     */
    @Override
    public boolean allowedToUpdate() {
      return true;
    }
    
    /**
     * if allowed to insert
     * @return true if allowed to insert
     */
    @Override
    public boolean allowedToInsert() {
      return false;
    }
  },
  
  /** it will insert or update depending if exsits or not */
  INSERT_OR_UPDATE {
    
    /**
     * if allowed to update
     * @return true if allowed to update
     */
    @Override
    public boolean allowedToUpdate() {
      return true;
    }
    
    /**
     * if allowed to insert
     * @return true if allowed to insert
     */
    @Override
    public boolean allowedToInsert() {
      return true;
    }
    

  };
  
  /**
   * if allowed to update
   * @return true if allowed to update
   */
  public abstract boolean allowedToUpdate();
  
  /**
   * if allowed to insert
   * @return true if allowed to insert
   */
  public abstract boolean allowedToInsert();
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum of null or exception if not found
   */
  public static SaveMode valueOfIgnoreCase(String string) {
    //TODO make this generic
    if (StringUtils.isBlank(string)) {
      return null;
    }
    for (SaveMode saveMode : SaveMode.values()) {
      if (StringUtils.equalsIgnoreCase(string, saveMode.name())) {
        return saveMode;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find saveMode from string: '").append(string);
    error.append("', expecting one of: ");
    for (SaveMode wsMemberFilter : SaveMode.values()) {
      error.append(wsMemberFilter.name()).append(", ");
    }
    throw new RuntimeException(error.toString());
  }

}