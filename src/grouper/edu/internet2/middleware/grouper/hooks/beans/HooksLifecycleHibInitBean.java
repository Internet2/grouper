/*
 * @author mchyzer
 * $Id: HooksLifecycleHibInitBean.java,v 1.1 2008-07-10 00:46:54 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import org.hibernate.cfg.Configuration;



/**
 * <pre>
 * bean to hold objects for hibernate init (add mappings here).
 * To add a mapping, call configuration.addResource(path);
 * where path is something like this:  some/package/here/Mapping.hbm.xml
 * 
 * </pre>
 */
public class HooksLifecycleHibInitBean extends HooksBean {
  
  
  /** hibernate configuration */
  private Configuration configuration = null;
  
  /**
   * 
   * @param theConfiguration is the configuration
   */
  public HooksLifecycleHibInitBean(Configuration theConfiguration) {
    this.configuration = theConfiguration;
  }

  
  /**
   * hibernate configuration
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return this.configuration;
  }
  
}
