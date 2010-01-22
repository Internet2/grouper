/*
 * @author mchyzer
 * $Id: HooksGrouperSessionBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for grouper session low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksGrouperSessionBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: grouperSession */
  public static final String FIELD_GROUPER_SESSION = "grouperSession";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_GROUPER_SESSION);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private GrouperSession grouperSession = null;
  
  /**
   * 
   */
  public HooksGrouperSessionBean() {
    super();
  }

  /**
   * @param theGrouperSession
   */
  public HooksGrouperSessionBean(GrouperSession theGrouperSession) {
    this.grouperSession = theGrouperSession;
  }
  
  /**
   * object being inserted
   * @return the Group
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksGrouperSessionBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
