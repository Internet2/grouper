/*
 * @author mchyzer
 * $Id: HooksAttributeDefBean.java 6921 2010-08-10 21:03:10Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for attribute def name low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksAttributeDefBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDef */
  public static final String FIELD_ATTRIBUTE_DEF = "attributeDef";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   */
  public HooksAttributeDefBean() {
    super();
  }

  /**
   * @param theAttribute 
   */
  public HooksAttributeDefBean(AttributeDef theAttribute) {
    this.attributeDef = theAttribute;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksAttributeDefBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @return the attribute
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

}
