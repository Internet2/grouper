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
 * $Id: GroupHookDbVersion.java,v 1.4 2009-11-10 03:35:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class GroupHookDbVersion extends GroupHooks {
  /** Extension field in Grouper. */
  private static final String EXTENSION        = "extension";

  /** Serial version UID. */
  private static final long   serialVersionUID = 8623167847413079614L;

  /** Logger. */
  private static final Log LOGGER  = GrouperUtil.getLog(GroupHookDbVersion.class);

  /**
   * Builds an instance of ESCOGroupHooks.
   */
  public GroupHookDbVersion() {

    if (LOGGER.isInfoEnabled()) {
      final StringBuilder sb = new StringBuilder(
          "Creation of an hooks of class: ");
      sb.append(getClass().getSimpleName());
      sb.append(".");
      LOGGER.info(sb.toString());
    }
  }

  /**
   * Tests if the extension of a group is modified.
   * @param group the group to test.
   * @return True if the extension of the group is modified.
   */
  protected boolean isExtensionUpdate(final Group group) {

    if (!group.dbVersionDifferentFields().contains(EXTENSION)) {
      return false;
    }

    //System.out.println(group.dbVersion().getExtension());
    
    final String currentExtension = group.getExtension();
    
    int dbDifferentFieldsSize = group.dbVersionDifferentFields().size();
    
    LOGGER
        .debug("isExtensionUpdate (1) => " + group.dbVersionDifferentFields() + ", currentExtension: " + currentExtension);
    final String previousExtension = group.dbVersion().getExtension();
    LOGGER
        .debug("isExtensionUpdate (2) => " + group.dbVersionDifferentFields() + ", previousExtension: " + previousExtension );

    if (dbDifferentFieldsSize != group.dbVersionDifferentFields().size()) {
      throw new RuntimeException("Why is dbVersionDifferentFieldsSize different??? " + dbDifferentFieldsSize 
          + " != " + group.dbVersionDifferentFields().size());
    }
    
    if (!currentExtension.equals(previousExtension)) {
      return true;
    }
    return false;
  }

  /**
  * @param hooksContext
  * @param postUpdateBean
  * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostUpdate(HooksContext, HooksGroupBean)
  */
  @Override
  public void groupPostUpdate(final HooksContext hooksContext,
      final HooksGroupBean postUpdateBean) {

    final Group group = postUpdateBean.getGroup();
    LOGGER.debug("Current extension: " + group.getExtension());
    LOGGER.debug("Previous extension: "
        + group.dbVersion().getExtension());
    
    Group dbGroup = (Group)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READONLY_NEW, 
        new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        GrouperSession grouperSession = null;
        try {
          grouperSession = GrouperSession.startRootSession(false);
          Group dbGroup = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);
          //load from db
          dbGroup.getExtension();
          return dbGroup;
        } catch (GroupNotFoundException gnfe) {
          return null;
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
    
    });
    if (dbGroup != null) {
      LOGGER.debug("From db group extension: " + dbGroup.getExtension());
    }
  }

  /**
   * @param hooksContext
   * @param preUpdateBean
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(HooksContext, HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(final HooksContext hooksContext,
      final HooksGroupBean preUpdateBean) {
    final Group group = preUpdateBean.getGroup();
    LOGGER.debug("groupPreUpdate call isExtensionUpdate");
    if (isExtensionUpdate(group)) {
      LOGGER.debug("isExtensionUpdate");
    }
    StringBuilder typeString = new StringBuilder();
    typeString.append("type for group: " + group.getName() + ": ");
    if (LOGGER.isDebugEnabled()) {
      for (GroupType groupType : group.getTypes()) {
        typeString.append(groupType.getName() + ", ");
      }
      LOGGER.debug(typeString);
    }
  }

}
