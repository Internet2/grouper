package edu.internet2.middleware.grouper.subj;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.LdapSourceAdapter;

public class GrouperLdapSourceAdapter2_5 extends LdapSourceAdapter {
  
  private static Log log = LogFactory.getLog(GrouperLdapSourceAdapter2_5.class);

  @Override
  public boolean isEditable() {
    return true;
  }

  @Override
  public Map<Integer, String> getSortAttributes() {
    
    if (this.sortAttributes == null) {
      synchronized(GrouperLdapSourceAdapter2_5.class) {
        if (this.sortAttributes == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          String sortAttributeCountKey = "subjectApi.source."+this.getConfigId()+".sortAttributeCount";
          int sortAttributeCount = SubjectConfig.retrieveConfig().propertyValueInt(sortAttributeCountKey, 0);
          
          for (int i = 0; i < sortAttributeCount; i++) {
            
            String sortAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source."+this.getConfigId()+".sortAttribute."+i+".attributeName");
            temp.put(i, sortAttributeName);
          }
          
          this.sortAttributes = temp;
        }
      }
    }
    
    return this.sortAttributes;
    
  }

  @Override
  public Map<Integer, String> getSearchAttributes() {
    
    if (this.searchAttributes == null) {
      synchronized(GrouperLdapSourceAdapter2_5.class) {
        if (this.searchAttributes == null) {
          LinkedHashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
          
          String searchAttributeCountKey = "subjectApi.source."+this.getConfigId()+".searchAttributeCount";
          int searchAttributeCount = SubjectConfig.retrieveConfig().propertyValueInt(searchAttributeCountKey, 0);
          
          for (int i = 0; i < searchAttributeCount; i++) {

            String searchAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source."+this.getConfigId()+".searchAttribute."+i+".attributeName");
            temp.put(i, searchAttributeName);
          }
          
          this.searchAttributes = temp;
        }
      }
    }
    
    return this.searchAttributes;
    
    
  }
  
  @Override
  public boolean isEnabled() {
    return SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source."+getConfigId()+".enabled", true);
  }
  
  
  /**
   * @param entry
   */
  @Override
  public Subject createSubject(LdapEntry entry) {
    
    Map<String, Object> sourceAttributesToValues = new CaseInsensitiveMap();

    if (entry==null) {
      log.error("Ldap createSubject called with null entry.");
      return (null);
    }
    
    String multivaluedLdapAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".multivaluedLdapAttributes");
    Set<String> multivaluedLdapAttributesSet = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(multivaluedLdapAttributes, ","));
    
    for (LdapAttribute attribute: GrouperUtil.nonNull(entry.getAttributes())) {
      String attributeName = attribute.getName().toLowerCase();
      
      if (multivaluedLdapAttributesSet.contains(attributeName)) {
        Set<String> values = new HashSet<String>();
        
        Collection<String> stringValues = attribute.getStringValues();
        if (this.getSourceAttributesToLowerCase().containsKey(attributeName)) {
          for (String singleValue: stringValues) {
            singleValue = singleValue == null ? null: singleValue.toLowerCase();
            values.add(singleValue);
          }
        } else {
          values.addAll(stringValues);
        }
        sourceAttributesToValues.put(attributeName, values);
      } else {
        if (GrouperUtil.length(attribute.getStringValues()) > 0) {
          String singleValue = attribute.getStringValues().iterator().next();
          
          if (this.getSourceAttributesToLowerCase().containsKey(attributeName) && singleValue != null) {
            singleValue = singleValue.toLowerCase();
          }
          
          sourceAttributesToValues.put(attributeName, singleValue);
        }
      }
      
    }
    
    String subjectID = (String) sourceAttributesToValues.get(subjectIDAttributeName);
    
    if (StringUtils.isBlank(subjectID)) {
      log.error("No value for LDAP attribute \"" + subjectIDAttributeName + "\". It is Grouper attribute \"SubjectID\".\".  Subject's problematic attributes : " + entry.toString());
      return null;
    }
   
    if (this.subjectIDFormatToLowerCase) {
      subjectID = subjectID.toLowerCase();
    }

    Subject subject = createSubject(sourceAttributesToValues, subjectID);
    
    return subject;
  }
  
  
}
