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
package edu.internet2.middleware.grouper.misc;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    /**
     * if there is a uuid, validate it for this
     * @param nameToEdit
     */
    @Override
    public void validateNameToEdit(String nameToEdit, String name) {
      GrouperUtil.assertion((StringUtils.equals(nameToEdit, name)) 
          || StringUtils.isBlank(nameToEdit), "Must not pass in a lookup name for an insert: " + nameToEdit + ", " + name);
    }
    
    /**
     * if update based on SaveMode
     * @param nameToEdit 
     * @return true if this is an update
     */
    @Override
    public boolean isUpdate(String nameToEdit, String name) {
      this.validateNameToEdit(nameToEdit, name);
      return false;
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
    /**
     * if there is a uuid, validate it for this
     * @param nameToEdit
     */
    @Override
    public void validateNameToEdit(String nameToEdit, String name) {
      GrouperUtil.assertion(!StringUtils.isBlank(nameToEdit), "Must pass in a lookup name for an update");
    }
    
    /**
     * if update based on SaveMode
     * @param nameToEdit 
     * @return true if this is an update
     */
    @Override
    public boolean isUpdate(String nameToEdit, String name) {
      this.validateNameToEdit(nameToEdit, name);
      return true;
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
    
    /**
     * if there is a uuid, validate it for this
     * @param nameToEdit
     * @param name
     */
    @Override
    public void validateNameToEdit(String nameToEdit, String name) {
      //nothing to do, anything is fine
    }

    /**
     * if update based on SaveMode
     * @param nameToEdit 
     * @return true if this is an update
     */
    @Override
    public boolean isUpdate(String nameToEdit, String name) {
      this.validateNameToEdit(nameToEdit, name);
      return !StringUtils.isBlank(nameToEdit);
    }

  };
  
  /**
   * if allowed to update
   * @return true if allowed to update
   */
  public abstract boolean allowedToUpdate();
  
  /**
   * if update based on SaveMode
   * @param nameToEdit 
   * @return true if this is an update
   */
  public abstract boolean isUpdate(String nameToEdit, String name);
  
  /**
   * if allowed to insert
   * @return true if allowed to insert
   */
  public abstract boolean allowedToInsert();
  
  /**
   * if there is a uuid, validate it for this
   * @param nameToEdit
   * @param name
   */
  public abstract void validateNameToEdit(String nameToEdit, String name);
  
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
