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
package edu.internet2.middleware.grouper.member;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
public enum SearchStringEnum {

  /**
   * searchString0
   */
  SEARCH_STRING_0 {

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SEARCH_STRING0;
    }

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.getPropertyBoolean("security.member.search.string0.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.getProperty("security.member.search.string0.allowOnlyGroup");
      return SearchStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 0;
    }
  },
  
  
  /**
   * searchString1
   */
  SEARCH_STRING_1 {

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SEARCH_STRING1;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.getPropertyBoolean("security.member.search.string1.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.getProperty("security.member.search.string1.allowOnlyGroup");
      return SearchStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 1;
    }
  },
  
  
  /**
   * searchString2
   */
  SEARCH_STRING_2 {

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SEARCH_STRING2;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.getPropertyBoolean("security.member.search.string2.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.getProperty("security.member.search.string2.allowOnlyGroup");
      return SearchStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 2;
    }
  },
  
  
  /**
   * searchString3
   */
  SEARCH_STRING_3 {

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SEARCH_STRING3;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.getPropertyBoolean("security.member.search.string3.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.getProperty("security.member.search.string3.allowOnlyGroup");
      return SearchStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 3;
    }
  },
  
  
  /**
   * searchString4
   */
  SEARCH_STRING_4 {

    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SEARCH_STRING4;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.getPropertyBoolean("security.member.search.string4.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.getProperty("security.member.search.string4.allowOnlyGroup");
      return SearchStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SearchStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 4;
    }
  };
  
  /**
   * @return the field name for a particular search string
   */
  public abstract String getFieldName();
  
  /**
   * @return true if the user has access to a particular search string
   */
  public abstract boolean hasAccess();
  
  /**
   * @return the index
   */
  public abstract int getIndex();
  
  /**
   * @param wheelOnly
   * @param allowOnlyGroupName
   * @return boolean
   */
  private static boolean hasAccess(boolean wheelOnly, String allowOnlyGroupName) {
    GrouperSession session = GrouperSession.staticGrouperSession();
    Subject subject = session.getSubject();
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    if (wheelOnly) {
      return false;
    }
    
    if (GrouperUtil.isEmpty(allowOnlyGroupName)) {
      return true;
    }
    
    Group allowOnlyGroup = GrouperDAOFactory.getFactory().getGroup().findByName(allowOnlyGroupName, true, null);
    return allowOnlyGroup.hasMember(subject);
  }
  
  /**
   * @return get the default search string based on what this subject has access to or null if the subject doesn't have access to any.
   */
  public static SearchStringEnum getDefaultSearchString() {

    String defaultIndexOrder = GrouperConfig.getProperty("member.search.defaultIndexOrder");
    if (GrouperUtil.isEmpty(defaultIndexOrder)) {
      return null;
    }
    
    String[] indexes = GrouperUtil.splitTrim(defaultIndexOrder, ",");
    for (String index : indexes) {
      SearchStringEnum curr = newInstance(Integer.parseInt(index));
      if (curr.hasAccess()) {
        return curr;
      }
    }
    
    return null;
  }
  
  /**
   * @param index
   * @return return enum based on the index value
   */
  public static SearchStringEnum newInstance(int index) {
    if (index == 0) {
      return SEARCH_STRING_0;
    }
    
    if (index == 1) {
      return SEARCH_STRING_1;
    }
    
    if (index == 2) {
      return SEARCH_STRING_2;
    }
    
    if (index == 3) {
      return SEARCH_STRING_3;
    }
    
    if (index == 4) {
      return SEARCH_STRING_4;
    }
    
    throw new RuntimeException("Unexpected search string index: " + index);
  }
}
