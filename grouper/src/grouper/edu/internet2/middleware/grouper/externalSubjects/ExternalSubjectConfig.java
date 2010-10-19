package edu.internet2.middleware.grouper.externalSubjects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * wrapper around config info for external subjects
 * @author mchyzer
 */
public class ExternalSubjectConfig {

  /**
   * cache the config stuff
   *
   */
  public static class ExternalSubjectConfigBean {
    
    /** expression language of the description */
    private String descriptionEl;

    /** if the name column is required */
    private boolean nameRequired = false;

    /** if the email column is required */
    private boolean emailRequired = false;

    /** if the email column is enabled */
    private boolean emailEnabled = true;

    /** if the institution column is required */
    private boolean institutionRequired = false;

    /** if the institution column is enabled */
    private boolean institutionEnabled = true;

    
    
    /**
     * expression language of the description
     * @return el of description
     */
    public String getDescriptionEl() {
      return this.descriptionEl;
    }



    /**
     * if the name column is required
     * @return if name required
     */
    public boolean isNameRequired() {
      return this.nameRequired;
    }

    /**
     * if the email column is required
     * @return if email required
     */
    public boolean isEmailRequired() {
      return this.emailRequired;
    }

    /**
     * if the email column is enabled
     * @return if email enabled
     */
    public boolean isEmailEnabled() {
      return this.emailEnabled;
    }



    /**
     * if the institution column is required
     * @return  institution required
     */
    public boolean isInstitutionRequired() {
      return this.institutionRequired;
    }



    /**
     * if the institution column is enabled
     * @return if the institution column is enabled
     */
    public boolean isInstitutionEnabled() {
      return this.institutionEnabled;
    }



    /**
     * attributes configured
     * @return attributes configured
     */
    public List<ExternalSubjectAttributeConfigBean> getExternalSubjectAttributeConfigBeans() {
      return this.externalSubjectAttributeConfigBeans;
    }



    
    /** attributes configured */
    private List<ExternalSubjectAttributeConfigBean> externalSubjectAttributeConfigBeans;

  }

  /**
   * attributes configured
   *
   */
  public static class ExternalSubjectAttributeConfigBean {

    /** friendly name can be shown on screen */
    private String friendlyName;

    /** system name is the column name of view, and can also be the subject attribute name */
    private String systemName;

    /** if this attribute value is required on screen */
    private boolean required;

    /** comment on view in DB, no special chars allowed */
    private String comment;
    
    /**
     * comment on view in DB, no special chars allowed
     * @return comment
     */
    public String getComment() {
      return this.comment;
    }

    /**
     * friendly name can be shown on screen
     * @return friendly name
     */
    public String getFriendlyName() {
      return this.friendlyName;
    }

    /**
     * system name is the column name of view, and can also be the subject attribute name
     * @return system name
     */
    public String getSystemName() {
      return this.systemName;
    }

    /**
     * if this attribute value is required on screen
     * @return if this attribute value is required on screen
     */
    public boolean isRequired() {
      return this.required;
    }

    
    
  }

  /** cache this so if file changes it will pick it back up */
  private static GrouperCache<Boolean, ExternalSubjectConfigBean> configCache = new GrouperCache(
      ExternalSubjectConfig.class.getName() + ".configCache", 50, false, 300, 300, false);

  /**
   * clear the config cache (e.g. for testing)
   */
  public static void clearCache() {
    configCache.clear();
  }
  
  /**
   * get the bean from cache or configure a new one
   * @return the config bean
   */
  public static ExternalSubjectConfigBean externalSubjectConfigBean() {
    ExternalSubjectConfigBean externalSubjectConfigBean = configCache.get(Boolean.TRUE);
    
    if (externalSubjectConfigBean == null) {
      
      synchronized (ExternalSubjectConfig.class) {

        //try again
        externalSubjectConfigBean = configCache.get(Boolean.TRUE);
        if (externalSubjectConfigBean == null) {
          
          externalSubjectConfigBean = new ExternalSubjectConfigBean();
          externalSubjectConfigBean.descriptionEl = GrouperConfig.getProperty("externalSubjects.desc.el");
          externalSubjectConfigBean.emailEnabled = GrouperConfig.getPropertyBoolean("externalSubjects.email.enabled", true);
          externalSubjectConfigBean.emailRequired = GrouperConfig.getPropertyBoolean("externalSubjects.email.required", false);
          externalSubjectConfigBean.institutionEnabled = GrouperConfig.getPropertyBoolean("externalSubjects.institution.enabled", false);
          externalSubjectConfigBean.institutionRequired = GrouperConfig.getPropertyBoolean("externalSubjects.institution.required", false);
          externalSubjectConfigBean.nameRequired = GrouperConfig.getPropertyBoolean("externalSubjects.name.required", false);
          externalSubjectConfigBean.externalSubjectAttributeConfigBeans = new ArrayList<ExternalSubjectAttributeConfigBean>();
          
          for (String propertyName : GrouperConfig.getPropertyNames()) {
            Matcher matcher = externalSubjectAttributeSystemNamePattern.matcher(propertyName);
            if (matcher.matches()) {

              String attributeConfigName = matcher.group(1);
              
              ExternalSubjectAttributeConfigBean externalSubjectAttributeConfigBean = new ExternalSubjectAttributeConfigBean();
              externalSubjectConfigBean.externalSubjectAttributeConfigBeans.add(externalSubjectAttributeConfigBean);
              
              externalSubjectAttributeConfigBean.systemName = GrouperConfig.getProperty(propertyName);
              externalSubjectAttributeConfigBean.friendlyName = GrouperConfig.getProperty(
                  "externalSubjects.attributes." + attributeConfigName + ".friendlyName");

              externalSubjectAttributeConfigBean.comment = GrouperConfig.getProperty(
                  "externalSubjects.attributes." + attributeConfigName + ".comment");

              externalSubjectAttributeConfigBean.required = GrouperConfig.getPropertyBoolean(
                  "externalSubjects.attributes." + attributeConfigName + ".required", false);              
            }
          }
          configCache.put(Boolean.TRUE, externalSubjectConfigBean);
        }        
      }
    }
    return externalSubjectConfigBean;
  }
  
  /**
   * <pre>
   * ^externalSubjects\.   matches start of string, externalSubjects, then a dot
   * attributes\.          matches attributes, then a dot
   * ([^.]+)\.             matches something not a dot, captures that, then a dot
   * systemName$           matches systemName, then the end of the string
   * </pre>
   */
  private static final Pattern externalSubjectAttributeSystemNamePattern = Pattern.compile("^externalSubjects\\.attributes\\.([^.]+)\\.systemName$");
  
}
