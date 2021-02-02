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
import edu.internet2.middleware.subject.SubjectCaseInsensitiveMapImpl;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.LdapSourceAdapter;
import edu.internet2.middleware.subject.provider.SubjectImpl;

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
    String subjectID = "";
    
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
    
    Map<String, Object> translationMap = new CaseInsensitiveMap();
    
    for (String sourceAttribute: sourceAttributesToValues.keySet()) {
      translationMap.put("source_attribute__"+sourceAttribute, sourceAttributesToValues.get(sourceAttribute));
    }
    
    Map<String, Object> subjectAttributesToValues = new CaseInsensitiveMap();
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    
    if (StringUtils.isNotBlank(numberOfAttributes)) {
      
      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
      for (int i=0; i<numberOfAttrs; i++) {
        
        String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
        
        String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translationType");
        
        boolean isSourceAttribute = StringUtils.equals(translationType, "sourceAttribute");
        boolean isSourceAttributeSameAsSubjectAttribute = StringUtils.equals(translationType, "sourceAttributeSameAsSubjectAttribute");
        
        if (isSourceAttributeSameAsSubjectAttribute) {
          Object value = sourceAttributesToValues.get(subjectAttributeName);
          subjectAttributesToValues.put(subjectAttributeName, value);
          translationMap.put("subject_attribute__"+subjectAttributeName.toLowerCase(), value);
        } else if (isSourceAttribute) {
          
          String sourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute").toLowerCase();

          Object value = sourceAttributesToValues.get(sourceAttribute);
          subjectAttributesToValues.put(subjectAttributeName, value);
          translationMap.put("subject_attribute__"+subjectAttributeName.toLowerCase(), value);
        }
        
      }
      
      
      for (int i=0; i<numberOfAttrs; i++) {
        
        String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
        
        String translationType = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translationType");
        
        boolean isTranslation = StringUtils.equals(translationType, "translation");
        
        if (isTranslation) {

          String translation = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translation");
          
          Object valueObject = GrouperUtil.substituteExpressionLanguageScript(translation, translationMap, true, false, true);
          valueObject = GrouperUtil.stringValue(valueObject);
          subjectAttributesToValues.put(subjectAttributeName, valueObject);
          
        }
        
      }
      
    }
          
    subjectID = (String) sourceAttributesToValues.get(subjectIDAttributeName);
    
    if (StringUtils.isBlank(subjectID)) {
      log.error("No value for LDAP attribute \"" + subjectIDAttributeName + "\". It is Grouper attribute \"SubjectID\".\".  Subject's problematic attributes : " + entry.toString());
      return null;
    }
   
    if (this.subjectIDFormatToLowerCase) {
      subjectID = subjectID.toLowerCase();
    }

     
    SubjectImpl subject = new SubjectImpl(subjectID, null, null, this.getSubjectType().getName(), this.getId(), nameAttributeName, descriptionAttributeName);
    subject.setTranslationMap(translationMap);
    
    // add the attributes
    Map<String, Set<String>> myAttributes = new  SubjectCaseInsensitiveMapImpl<String, Set<String>>();

    for (String subjectAttributeName: subjectAttributesToValues.keySet()) {
      Object value = subjectAttributesToValues.get(subjectAttributeName);
      if (value instanceof Set) {
        myAttributes.put(subjectAttributeName, (Set<String>)value);
      } else {
        myAttributes.put(subjectAttributeName, GrouperUtil.toSetObject((String)value));
      }
    }

    subject.setAttributes(myAttributes);

    return subject;
  }
  
  
}
