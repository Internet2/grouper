/*******************************************************************************
 * Copyright 2015 Internet2
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
 * $Id: GroupAttributeNameValidationHook.java,v 1.6 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * group names will case insensitive be unique
 * 
 * set that with grouper.properties:
 * 
 * hooks.group.class = edu.internet2.middleware.grouper.hooks.examples.GroupUniqueNameCaseInsensitiveHook
 * 
 * </pre>
 */
public class GroupUniqueNameCaseInsensitiveHook extends GroupHooks {
  
  /**
   * veto key
   */
  public static final String VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE = "veto.group.unique.nameCaseInsensitive";

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    Group group = preInsertBean.getGroup();
    verifyCaseInsensitiveName(group);
  }

  /**
   * 
   * @param group
   */
  public static void verifyCaseInsensitiveName(Group group) {
    
    //see if there is another group with the same name case insensitive
    long count = HibernateSession.byHqlStatic().createQuery("select count(theGroup) "
        + "from Group as theGroup where "
        + " (lower(theGroup.nameDb) = :theName or lower(theGroup.alternateNameDb) = :theName) "
        + "and theGroup.uuid != :theUuid ")
        .setString("theName", group.getName().toLowerCase())
        .setString("theUuid", group.getId()).uniqueResult(long.class);

// this wont work with alternate names or if two stems with different case
//    
//    boolean searchByParentFolder = GrouperConfig.retrieveConfig().propertyValueBoolean("groupUniqueNameCaseInsensitiveHook.searchByParentFolder", false);
//
//    if (searchByParentFolder) {
//      String parentFolderName = GrouperUtil.parentStemNameFromName(group.getName());
//
//      count = HibernateSession.byHqlStatic()
//          .createQuery("select count(theGroup) from Group theGroup, Stem theStem "
//              + "where lower(theGroup.extensionDb) = :theExtension "
//              + "and theStem.nameDb = :stemName "
//              + "and theGroup.parentUuid = theStem.uuid "
//              + "and theGroup.uuid != :theUuid ")
//          .setString("theExtension", StringUtils.defaultString(GrouperUtil.extensionFromName(group.getName())).toLowerCase())
//          .setString("stemName", parentFolderName)
//          .setString("theUuid", group.getId())
//          .uniqueResult(long.class);
//      
//    } else {
    
    if (count > 0) {
      throw new HookVeto(VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE, "The group ID is already in use, please use a different ID");
    }
        
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    Group group = preUpdateBean.getGroup();
    if (group.dbVersionDifferentFields().contains(Group.FIELD_EXTENSION) || group.dbVersionDifferentFields().contains(Group.FIELD_NAME)) {
      verifyCaseInsensitiveName(group);
    }
  }
  
}
