/*
 * @author mchyzer
 * $Id: GroupHookDbVersion.java,v 1.1.2.1 2009-02-13 20:54:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
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

    System.out.println(group.dbVersion().getAttributeOrNull(
        EXTENSION));
    
    final String currentExtension = group.getAttributeOrNull(EXTENSION);
    
    int dbDifferentFieldsSize = group.dbVersionDifferentFields().size();
    
    LOGGER
        .debug("isExtensionUpdate (1) => " + group.dbVersionDifferentFields() + ", currentExtension: " + currentExtension);
    final String previousExtension = group.dbVersion().getAttributeOrNull(
        EXTENSION);
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
    LOGGER.debug("Current extension: " + group.getAttributeOrNull(EXTENSION));
    LOGGER.debug("Previous extension: "
        + group.dbVersion().getAttributeOrNull(EXTENSION));
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
  }

}
