/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for attribute def name low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksAttributeDefNameBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefName */
  public static final String FIELD_ATTRIBUTE_DEF_NAME = "attributeDefName";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private AttributeDefName attributeDefName = null;
  
  /**
   * 
   */
  public HooksAttributeDefNameBean() {
    super();
  }

  /**
   * @param theAttribute 
   */
  public HooksAttributeDefNameBean(AttributeDefName theAttribute) {
    this.attributeDefName = theAttribute;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksAttributeDefNameBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @return the attribute
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

}
