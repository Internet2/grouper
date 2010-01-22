/*
 * @author mchyzer
 * $Id: HooksFieldBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for field low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksFieldBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: field */
  public static final String FIELD_FIELD = "field";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_FIELD);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_FIELD);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Field field = null;
  
  /**
   * 
   */
  public HooksFieldBean() {
    super();
  }

  /**
   * @param theField
   */
  public HooksFieldBean(Field theField) {
    this.field = theField;
  }
  
  /**
   * object being inserted
   * @return the Field
   */
  public Field getField() {
    return this.field;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksFieldBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
