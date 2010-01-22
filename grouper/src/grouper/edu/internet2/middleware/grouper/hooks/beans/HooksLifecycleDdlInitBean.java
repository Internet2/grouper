/*
 * @author mchyzer
 * $Id: HooksLifecycleDdlInitBean.java,v 1.1 2008-07-27 07:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.List;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;



/**
 * <pre>
 * bean to hold objects for ddl init (add ddl object names here).
 * 
 * </pre>
 */
@GrouperIgnoreDbVersion
public class HooksLifecycleDdlInitBean extends HooksBean {

  /** hibernate configuration */
  private List<String> ddlObjectNames = null;
  
  /**
   * 
   */
  public HooksLifecycleDdlInitBean() {
    super();
  }


  /**
   * @param theDdlObjectNames is the configuration
   */
  public HooksLifecycleDdlInitBean(List<String> theDdlObjectNames) {
    this.ddlObjectNames = theDdlObjectNames;
  }

  
  
  /**
   * @return the ddlObjectNames
   */
  public List<String> getDdlObjectNames() {
    return this.ddlObjectNames;
  }


  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLifecycleDdlInitBean clone() {
    throw new RuntimeException("You cant clone this bean: " + HooksLifecycleDdlInitBean.class.getName());
  }
}
