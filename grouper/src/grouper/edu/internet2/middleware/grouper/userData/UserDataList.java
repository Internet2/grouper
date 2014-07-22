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
/**
 * 
 */
package edu.internet2.middleware.grouper.userData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * list of user data objects
 * @author mchyzer
 *
 */
public class UserDataList {

  /**
   * 
   * @param jsonString
   * @return
   */
  public static UserDataList jsonMarshalFrom(String jsonString) {
    if (StringUtils.isBlank(jsonString)) {
      return null;
    }
    return GrouperUtil.jsonConvertFrom(jsonString, UserDataList.class);
  }
  
  /**
   * convert the object to json
   * @return the string
   */
  public String jsonConvertTo() {
    return GrouperUtil.jsonConvertTo(this, false);
  }
  
  /**
   * add a userDataObject and timestamp, if it already exists, reorder it so it is first
   * @param userDataObject
   * @return true if made changes, false if not
   */
  public boolean addUserDataObject(UserDataObject userDataObject, int maxSize) {

    if (userDataObject == null) {
      throw new RuntimeException("Why is userDataObject null?");
    }
    
    if (StringUtils.isBlank(userDataObject.getUuid())) {
      throw new RuntimeException("Why is uuid null?");
    }

    //lets see if already there
    for (UserDataObject current : GrouperUtil.nonNull(this.getList(), UserDataObject.class)) {

      //its there, nothing else to do
      if (StringUtils.equals(current.getUuid(), userDataObject.getUuid())
          && GrouperUtil.equals(current.getTimestamp(), userDataObject.getTimestamp())) {
        return false;
      }

    }

    
    List<UserDataObject> userDataObjectList = new ArrayList<UserDataObject>();

    //add this one to the front of the list
    userDataObjectList.add(userDataObject);

    //only add if uuid is different
    for (UserDataObject current : GrouperUtil.nonNull(this.getList(), UserDataObject.class)) {

      if (!StringUtils.equals(current.getUuid(), userDataObject.getUuid())) {
        userDataObjectList.add(current);
      }
      
      //dont have more than a certain number
      if (userDataObjectList.size() >= maxSize) {
        break;
      }
      
    }
    
    //move the list to the array
    this.list = GrouperUtil.toArray(userDataObjectList, UserDataObject.class);
    
    return true;
  }

  
  /**
   * replace a userDataList with a new list, keep the old timestamps if they are there
   * @param uuids
   * @param maxSize
   * @return true if made changes, false if not
   */
  public boolean replaceUserDataObjectsWithSubset(Set<String> uuids, int maxSize) {
    
    //dont go over the max size
    UserDataObject[] newList = new UserDataObject[Math.min(GrouperUtil.length(uuids), maxSize)];
    
    //lets see if there are no changes needed, this assumes there are no duplicates
    if (GrouperUtil.length(uuids) == GrouperUtil.length(this.getList())) {
      boolean hasMismatch = false;
      for (UserDataObject userDataObject : GrouperUtil.nonNull(this.getList(), UserDataObject.class)) {
        if (!uuids.contains(userDataObject.getUuid())) {
          hasMismatch = true;
        }
      }
      if (!hasMismatch) {
        return false;
      }
    }
    
    Set<String> alreadySeen = new LinkedHashSet<String>();
    
    int index=0;
    
    //lets see if already there
    for (UserDataObject current : GrouperUtil.nonNull(this.getList(), UserDataObject.class)) {

      //lets see if it is valid and not already seen
      if (index < maxSize && !alreadySeen.contains(current.getUuid()) && uuids.contains(current.getUuid())) {
        
        newList[index++] = current;
        alreadySeen.add(current.getUuid());
      }

    }

    
    //move the list to the array
    this.list = newList;
    
    return true;
  }

  
  /**
   * add a userDataObject and timestamp, if it already exists, reorder it so it is first
   * @param userDataObject
   * @return true if made changes, false if not
   */
  public boolean removeUuid(String uuid) {

    if (StringUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid null?");
    }

    List<UserDataObject> userDataObjectList = new ArrayList<UserDataObject>();

    boolean foundMatch = false;
    
    //lets see if already there
    for (UserDataObject current : GrouperUtil.nonNull(this.getList(), UserDataObject.class)) {

      //its there, nothing else to do
      if (StringUtils.equals(current.getUuid(), uuid)) {
        foundMatch = true;
      } else {
        userDataObjectList.add(current);
      }

    }
    
    this.list = GrouperUtil.toArray(userDataObjectList, UserDataObject.class);
    
    return foundMatch;
  }

  /**
   * 
   */
  public UserDataList() {
    super();
  }

  /**
   * construct with field
   * @param list
   */
  public UserDataList(UserDataObject[] list) {
    super();
    this.list = list;
  }

  /**
   * list of objects
   */
  private UserDataObject[] list;

  
  /**
   * list of objects
   * @return the list
   */
  public UserDataObject[] getList() {
    return list;
  }

  /**
   * list of objects
   * @param list1
   */
  public void setList(UserDataObject[] list1) {
    this.list = list1;
  }
  
  
  
}
