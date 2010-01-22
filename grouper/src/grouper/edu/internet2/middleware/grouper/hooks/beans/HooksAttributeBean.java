/*
 * @author mchyzer
 * $Id: HooksAttributeBean.java,v 1.1 2008-11-04 07:17:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksAttributeBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attribute */
  public static final String FIELD_ATTRIBUTE = "attribute";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Attribute attribute = null;
  
  /**
   * 
   */
  public HooksAttributeBean() {
    super();
  }

  /**
   * @param theAttribute 
   */
  public HooksAttributeBean(Attribute theAttribute) {
    this.attribute = theAttribute;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksAttributeBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @return the attribute
   */
  public Attribute getAttribute() {
    return this.attribute;
  }

}
