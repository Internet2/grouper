package edu.internet2.middleware.grouper.externalSubjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   * //#add multiple group assignment actions by URL param: externalSubjectInviteName
   * //externalSubject.autoadd.testingLibrary.externalSubjectInviteName=library
   * //#comma separated groups to add for this type of invite
   * //externalSubject.autoadd.testingLibrary.groups=
   * //#should be insert, update, or insert,update
   * //externalSubject.autoadd.testingLibrary.actions=insert,update
   * //#should be insert, update, or insert,update
   * //externalSubject.autoadd.testingLibrary.expireAfterDays=
   * cache the auto add config
   */
  public static class ExternalSubjectAutoaddBean {
    
    /** invite name from url */
    private String externalSubjectInviteName;
    
    /** groups to add comma separated */
    private String groups;
    
    /** insert, update, or insert,update */
    private String actions;
    
    /** days after which expire the membership */
    private int expireAfterDays;

    /**
     * invite name from url
     * @return invite name from url
     */
    public String getExternalSubjectInviteName() {
      return this.externalSubjectInviteName;
    }

    /**
     * invite name from url
     * @param externalSubjectInviteName1
     */
    public void setExternalSubjectInviteName(String externalSubjectInviteName1) {
      this.externalSubjectInviteName = externalSubjectInviteName1;
    }

    /**
     * groups to add comma separated
     * @return groups
     */
    public String getGroups() {
      return this.groups;
    }

    /**
     * groups to add comma separated
     * @param groups1
     */
    public void setGroups(String groups1) {
      this.groups = groups1;
    }

    /**
     * insert, update, or insert,update
     * @return insert, update, or insert,update
     */
    public String getActions() {
      return this.actions;
    }

    /**
     * insert, update, or insert,update
     * @param actions1
     */
    public void setActions(String actions1) {
      this.actions = actions1;
    }

    /**
     * days after which expire the membership
     * @return days after which expire the membership
     */
    public int getExpireAfterDays() {
      return this.expireAfterDays;
    }

    /**
     * days after which expire the membership
     * @param expireAfterDays1
     */
    public void setExpireAfterDays(int expireAfterDays1) {
      this.expireAfterDays = expireAfterDays1;
    }
    
    
    
  }
  
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

  /** cache this so if file changes it will pick it back up */
  private static GrouperCache<Boolean, Map<String, ExternalSubjectAutoaddBean>> autoaddConfigCache = new GrouperCache<Boolean, Map<String, ExternalSubjectAutoaddBean>>(
      ExternalSubjectConfig.class.getName() + ".autoaddConfigCache", 50, false, 300, 300, false);

  /**
   * clear the config cache (e.g. for testing)
   */
  public static void clearCache() {
    configCache.clear();
    autoaddConfigCache.clear();
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
   * get the bean map from cache or configure a new one
   * @return the config bean
   */
  public static Map<String, ExternalSubjectAutoaddBean> externalSubjectAutoaddConfigBean() {
    Map<String, ExternalSubjectAutoaddBean> autoaddMap = autoaddConfigCache.get(Boolean.TRUE);
    
    if (autoaddMap == null) {
      
      synchronized (ExternalSubjectConfig.class) {

        //try again
        autoaddMap = autoaddConfigCache.get(Boolean.TRUE);
        if (autoaddMap == null) {
          
          autoaddMap = new HashMap<String, ExternalSubjectAutoaddBean>();
          
          for (String propertyName : GrouperConfig.getPropertyNames()) {
            Matcher matcher = externalSubjectAutoaddInviteNamePattern.matcher(propertyName);
            if (matcher.matches()) {

              String inviteConfigName = matcher.group(1);
              
              ExternalSubjectAutoaddBean externalSubjectAutoaddBean = new ExternalSubjectAutoaddBean();
              
              externalSubjectAutoaddBean.externalSubjectInviteName = GrouperConfig.getProperty(propertyName);
              
              externalSubjectAutoaddBean.actions = GrouperConfig.getProperty(
                  "externalSubject.autoadd." + inviteConfigName + ".actions");
              externalSubjectAutoaddBean.groups = GrouperConfig.getProperty(
                  "externalSubject.autoadd." + inviteConfigName + ".groups");
              externalSubjectAutoaddBean.expireAfterDays = GrouperConfig.getPropertyInt(
                  "externalSubject.autoadd." + inviteConfigName + ".expireAfterDays", -1);

              autoaddMap.put(externalSubjectAutoaddBean.externalSubjectInviteName, externalSubjectAutoaddBean);

            }
          }
          autoaddConfigCache.put(Boolean.TRUE, autoaddMap);
        }        
      }
    }
    return autoaddMap;
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

  /**
   * externalSubject.autoadd.testingLibrary.externalSubjectInviteName
   * <pre>
   * ^externalSubjects\.        matches start of string, externalSubjects, then a dot
   * autoadd\.                  matches autoadd, then a dot
   * ([^.]+)\.                  matches something not a dot, captures that, then a dot
   * externalSubjectInviteName$ matches systemName, then the end of the string
   * </pre>
   */
  private static final Pattern externalSubjectAutoaddInviteNamePattern = Pattern.compile("^externalSubjects\\.autoadd\\.([^.]+)\\.externalSubjectInviteName$");

}
