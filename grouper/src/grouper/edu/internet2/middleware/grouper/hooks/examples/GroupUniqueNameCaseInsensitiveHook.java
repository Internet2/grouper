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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
 * or
 * 
 * grouperHook.GroupUniqueNameCaseInsensitiveHook.autoRegister = true (default)
 * </pre>
 */
public class GroupUniqueNameCaseInsensitiveHook extends GroupHooks {
  
  /**
   * only register once
   */
  private static boolean registered = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.GroupUniqueNameCaseInsensitiveHook.autoRegister", true)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), 
          GroupUniqueNameCaseInsensitiveHook.class);
    }
    
    registered = true;

  }

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
        + " (lower(theGroup.nameDb) in (:theName, :theName2) or lower(theGroup.alternateNameDb) in (:theName, :theName2) or lower(theGroup.displayNameDb) = :theName3) "
        + "and theGroup.uuid != :theUuid ")
        .setString("theName", group.getName().toLowerCase())
        .setString("theName2", GrouperUtil.defaultIfEmpty(group.getAlternateName(), group.getName()).toLowerCase())
        .setString("theName3", group.getDisplayName().toLowerCase())
        .setString("theUuid", group.getId()).uniqueResult(long.class);

    if (count > 0) {
      count = HibernateSession.byHqlStatic().createQuery("select count(theGroup) "
          + "from Group as theGroup where "
          + " (lower(theGroup.nameDb) in (:theName, :theName2) or lower(theGroup.alternateNameDb) in (:theName, :theName2)) "
          + "and theGroup.uuid != :theUuid ")
          .setString("theName", group.getName().toLowerCase())
          .setString("theName2", GrouperUtil.defaultIfEmpty(group.getAlternateName(), group.getName()).toLowerCase())
          .setString("theUuid", group.getId()).uniqueResult(long.class);
      if (count > 0) {
        throw new HookVeto(VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.group.unique.idCaseInsensitive.default"));
      } else {
        throw new HookVeto(VETO_GROUP_UNIQUE_NAME_CASE_INSENSITIVE, GrouperTextContainer.textOrNull("veto.group.unique.nameCaseInsensitive.default"));
      }
    }

    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    Group group = preUpdateBean.getGroup();
    if (group.dbVersionDifferentFields().contains(Group.FIELD_EXTENSION) || group.dbVersionDifferentFields().contains(Group.FIELD_NAME) || group.dbVersionDifferentFields().contains(Group.FIELD_ALTERNATE_NAME_DB)) {
      verifyCaseInsensitiveName(group);
    }
  }
  
}
