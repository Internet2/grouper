/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;


/**
 * utils about emails
 */
public class GrouperEmailUtils {

  /**
   * grouper subject attribute bean
   */
  public static class GrouperSubjectAttributeBean {
    
    /** source id */
    private String sourceId;
    
    /** email address name */
    private String emailAttributeName;
    
    /**
     * soruceId
     * @return the sourceId
     */
    public String getSourceId() {
      return this.sourceId;
    }
    
    /**
     * source id
     * @param sourceId1 the sourceId to set
     */
    public void setSourceId(String sourceId1) {
      this.sourceId = sourceId1;
    }
    
    /**
     * @return the emailAttributeName
     */
    public String getEmailAttributeName() {
      return this.emailAttributeName;
    }
    
    /**
     * @param emailAttributeName1 the emailAttributeName to set
     */
    public void setEmailAttributeName(String emailAttributeName1) {
      this.emailAttributeName = emailAttributeName1;
    }
    
  }
  
  /** cache of config */
  private static GrouperCache<Boolean, Map<String, GrouperSubjectAttributeBean>> subjectSourceAttributes
    = new GrouperCache<Boolean, Map<String, GrouperSubjectAttributeBean>>(
        GrouperEmailUtils.class.getName() + ".subjectSourceEmailAttribute", 1000, false, 60 * 60, 60 * 60, false);
     
  
  /**
   * <pre>
   * match: mail.source.someName.name
   * regex: ^mail\.source\.([^.]+\.name).*$
   * </pre>
   */
  private static Pattern namePattern = Pattern.compile("^mail\\.source\\.([^.]+)\\.name.*$");
  
  /**
   * 
   * @param sourceId
   * @return the bean for this source or null if none configured
   */
  public static GrouperSubjectAttributeBean subjectSourceAttributes(String sourceId) {
    
    Map<String, GrouperSubjectAttributeBean> map = subjectSourceAttributes.get(Boolean.TRUE);

    if (map == null) {
      
      synchronized(GrouperEmailUtils.class) {

        map = subjectSourceAttributes.get(Boolean.TRUE);
        if (map == null) {
          
          map = new HashMap<String, GrouperSubjectAttributeBean>();
          
          //lets reload
          
          Set<String> propertyNames = GrouperConfig.getPropertyNames();
          
          for (String propertyName : propertyNames) {
            
            Matcher matcher = namePattern.matcher(propertyName);
            if (matcher.matches()) {
              
              String configName = matcher.group(1);
              String theSourceId = GrouperConfig.getProperty(propertyName);
              String emailAttributeName = GrouperConfig.getProperty("mail.source." + configName + ".emailAttributeName");
              
              GrouperSubjectAttributeBean theGrouperSubjectAttributeBean = new GrouperSubjectAttributeBean();
              theGrouperSubjectAttributeBean.setSourceId(theSourceId);
              theGrouperSubjectAttributeBean.setEmailAttributeName(emailAttributeName);
              
              map.put(sourceId, theGrouperSubjectAttributeBean);
            }
            
          }
          
          subjectSourceAttributes.put(Boolean.TRUE, map);
        }
      }
      
      
    }
    
    return map.get(sourceId);
    
  }
  
}
