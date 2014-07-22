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
public enum SortStringEnum {

  /**
   * sortString0
   */
  SORT_STRING_0 {

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SORT_STRING0;
    }

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.retrieveConfig().propertyValueBoolean("security.member.sort.string0.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.member.sort.string0.allowOnlyGroup");
      return SortStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 0;
    }
  },
  
  
  /**
   * sortString1
   */
  SORT_STRING_1 {

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SORT_STRING1;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.retrieveConfig().propertyValueBoolean("security.member.sort.string1.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.member.sort.string1.allowOnlyGroup");
      return SortStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 1;
    }
  },
  
  
  /**
   * sortString2
   */
  SORT_STRING_2 {

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SORT_STRING2;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.retrieveConfig().propertyValueBoolean("security.member.sort.string2.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.member.sort.string2.allowOnlyGroup");
      return SortStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 2;
    }
  },
  
  
  /**
   * sortString3
   */
  SORT_STRING_3 {

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SORT_STRING3;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.retrieveConfig().propertyValueBoolean("security.member.sort.string3.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.member.sort.string3.allowOnlyGroup");
      return SortStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 3;
    }
  },
  
  
  /**
   * sortString4
   */
  SORT_STRING_4 {

    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getFieldName()
     */
    @Override
    public String getFieldName() {
      return Member.FIELD_SORT_STRING4;
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#hasAccess()
     */
    @Override
    public boolean hasAccess() {

      boolean wheelOnly = GrouperConfig.retrieveConfig().propertyValueBoolean("security.member.sort.string4.wheelOnly", false);
      String allowOnlyGroupName = GrouperConfig.retrieveConfig().propertyValueString("security.member.sort.string4.allowOnlyGroup");
      return SortStringEnum.hasAccess(wheelOnly, allowOnlyGroupName);
    }
    
    /**
     * @see edu.internet2.middleware.grouper.member.SortStringEnum#getIndex()
     */
    @Override
    public int getIndex() {
      return 4;
    }
  };
  
  /**
   * @return the field name for a particular sort string
   */
  public abstract String getFieldName();
  
  /**
   * @return true if the user has access to a particular sort string
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
   * @return get the default sort string based on what this subject has access to or null if the subject doesn't have access to any.
   */
  public static SortStringEnum getDefaultSortString() {

    String defaultIndexOrder = GrouperConfig.retrieveConfig().propertyValueString("member.sort.defaultIndexOrder");
    if (GrouperUtil.isEmpty(defaultIndexOrder)) {
      return null;
    }
    
    String[] indexes = GrouperUtil.splitTrim(defaultIndexOrder, ",");
    for (String index : indexes) {
      SortStringEnum curr = newInstance(Integer.parseInt(index));
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
  public static SortStringEnum newInstance(int index) {
    if (index == 0) {
      return SORT_STRING_0;
    }
    
    if (index == 1) {
      return SORT_STRING_1;
    }
    
    if (index == 2) {
      return SORT_STRING_2;
    }
    
    if (index == 3) {
      return SORT_STRING_3;
    }
    
    if (index == 4) {
      return SORT_STRING_4;
    }
    
    throw new RuntimeException("Unexpected sort string index: " + index);
  }
}
