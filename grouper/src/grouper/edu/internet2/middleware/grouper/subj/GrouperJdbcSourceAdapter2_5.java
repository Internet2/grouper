package edu.internet2.middleware.grouper.subj;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter2;
import edu.internet2.middleware.subject.provider.JdbcSubjectAttributeSet;
import edu.internet2.middleware.subject.provider.SubjectImpl;
import edu.internet2.middleware.subject.util.SubjectApiUtils;

public class GrouperJdbcSourceAdapter2_5 extends JDBCSourceAdapter2 {

  @Override
  public boolean isEditable() {
    return true;
  }
  
  @Override
  public boolean isEnabled() {
    return SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source."+getConfigId()+".enabled", true);
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
  protected void setupDataSource(Properties props) throws SourceUnavailableException {
    super.setupDataSource(props);
    
    if (subjectIdCol.startsWith("$")) {
      selectCols.remove(subjectIdCol);
      subjectIdCol = null;
    }
    
    
    String extraAttributesFromSource = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".extraAttributesFromSource");
    if (StringUtils.isNotBlank(extraAttributesFromSource)) {
      for (String extraAttribute : SubjectUtils.splitTrim(extraAttributesFromSource, ",")) {
        selectCols.add(extraAttribute);
      }
    }
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    if (StringUtils.isNotBlank(numberOfAttributes)) {
      
      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
      Set<String> subjectIdentifiers = new TreeSet<String>();
      for (int i=0; i<numberOfAttrs; i++) {
        
        boolean isTranslation = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".isTranslation", false);
        if (!isTranslation) {
          String sourceAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute");
          String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
          boolean isSubjectIdentifier = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".subjectIdentifier", false);
          if (StringUtils.isNotBlank(sourceAttributeName)) {
            selectCols.add(sourceAttributeName);
          }
          
          if (isSubjectIdentifier) {
            subjectIdentifiers.add(subjectAttributeName);
            // param.subjectIdentifierCol0.value
          }
        }
      
      }
      
    }
    
  }
  
  
  /**
   * Loads attributes for the argument subject.
   * @param resultSet 
   * @param query for logging
   * @param resultSetMetaData 
   * @return attributes
   * @throws SQLException 
   */
  protected Map<String, Set<String>> loadAttributes(ResultSet resultSet, String query,
      ResultSetMetaData resultSetMetaData) throws SQLException {
    Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();
    
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    if (StringUtils.isNotBlank(numberOfAttributes)) {
      
      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
      for (int i=0; i<numberOfAttrs; i++) {
        
        boolean isTranslation = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".isTranslation", false);
        if (!isTranslation) {
          String sourceAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute");
          if (StringUtils.isNotBlank(sourceAttributeName)) {
            
            String value = retrieveString(resultSet, sourceAttributeName, sourceAttributeName,
                query, resultSetMetaData);
            
            //String attributeName = this.subjectAttributeColToName.get(colName);
            attributes.put(sourceAttributeName, new JdbcSubjectAttributeSet(value));
            
          }
        }
      
      }
      
    }
    
    return attributes;
  }
  
  /**
   * Create a subject from the current row in the resultSet
   * 
   * @param resultSet
   * @param query 
   * @param identifiersForIdentifierToMap
   * @param resultIdentifierToSubject
   * @return subject
   * @throws SQLException 
   */
  protected Subject createSubject(ResultSet resultSet, String query, 
      Collection<String> identifiersForIdentifierToMap, Map<String, Subject> resultIdentifierToSubject) throws SQLException {

    String name = null;
    String subjectID = null;
    String description = null;
    SubjectImpl subject = null;
    //lets do this through metadata so caps dont matter
    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

    if (subjectIdCol != null) {
      subjectID = retrieveString(resultSet, this.subjectIdCol, "subjectIdCol", query, resultSetMetaData);
    }
    
    if (StringUtils.isNotBlank(this.nameCol)) {
      name = retrieveString(resultSet, this.nameCol, "nameCol", query, resultSetMetaData);
    }
    
    if (StringUtils.isNotBlank(this.descriptionCol)) {
      description = retrieveString(resultSet, this.descriptionCol, "descriptionCol",
          query, resultSetMetaData);
    }
    
    // if name attribute is present, let's use that.
    if (StringUtils.isNotBlank(this.nameAttributeName)) {
      name = null;
    }
    
    // if description attribute is present, let's use that.
    if (StringUtils.isNotBlank(this.descriptionAttributeName)) {
      description = null;
    }
    
    Map<String, Object> sourceAttributesToValues = new CaseInsensitiveMap();
    
    Map<String, Set<String>> attributes = loadAttributes(resultSet, query, resultSetMetaData);
    
    Map<String, Object> translationMap = new CaseInsensitiveMap();
    
    for (String attributeName: attributes.keySet()) {
      
      Set<String> attributeValues = attributes.get(attributeName);
      
      attributeName = attributeName.toLowerCase();
      
      if (GrouperUtil.length(attributeValues) > 0) {
        String singleValue = attributeValues.iterator().next();
        
        if (this.getSourceAttributesToLowerCase().containsKey(attributeName) && singleValue != null) {
          singleValue = singleValue.toLowerCase();
        }
        
        sourceAttributesToValues.put(attributeName, singleValue);
      }
      
      translationMap.put("source_attribute__"+attributeName, sourceAttributesToValues.get(attributeName));
      
    }
    
    Map<String, Object> subjectAttributesToValues = new CaseInsensitiveMap();
    
    String numberOfAttributes = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".numberOfAttributes");
    
    if (StringUtils.isNotBlank(numberOfAttributes)) {
      
      int numberOfAttrs = Integer.parseInt(numberOfAttributes);
      for (int i=0; i<numberOfAttrs; i++) {
        
        String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
        boolean isTranslation = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".isTranslation", false);
        
        if (!isTranslation) {
          
          String sourceAttribute = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".sourceAttribute").toLowerCase();

          Object value = sourceAttributesToValues.get(sourceAttribute);
          subjectAttributesToValues.put(subjectAttributeName, value);
          translationMap.put("subject_attribute__"+subjectAttributeName.toLowerCase(), sourceAttributesToValues.get(sourceAttribute));
        }
        
      }
      
      String subjectIdField = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".param.SubjectID_AttributeType.value");
      
      for (int i=0; i<numberOfAttrs; i++) {
        
        String subjectAttributeName = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".name");
        
        boolean isTranslation = SubjectConfig.retrieveConfig().propertyValueBoolean("subjectApi.source." + this.getConfigId() + ".attribute."+i+".isTranslation", false);
        
        if (isTranslation) {

          String translation = SubjectConfig.retrieveConfig().propertyValueString("subjectApi.source." + this.getConfigId() + ".attribute."+i+".translation");
          
          Object valueObject = GrouperUtil.substituteExpressionLanguageScript(translation, translationMap, true, false, true);
          valueObject = GrouperUtil.stringValue(valueObject);
          subjectAttributesToValues.put(subjectAttributeName, valueObject);
          
          if (StringUtils.equals(subjectAttributeName, subjectIdField)) {
           subjectID =  valueObject.toString();
          }
          
        }
        
      }
      
    }
    
    
    subject = new SubjectImpl(subjectID, name, description, this.getSubjectType().getName(), this.getId(),
        attributes, this.nameAttributeName, this.descriptionAttributeName);
    
    subject.setTranslationMap(translationMap);
    
    if (resultIdentifierToSubject != null) {
      boolean foundValue = false;
      
      if (identifiersForIdentifierToMap.contains(subject.getId())) {
        resultIdentifierToSubject.put(subject.getId(), subject);
        foundValue = true;
      } else {
      
        for (String identifierCol : this.subjectIdentifierCols) {
          
          String identifierValue = retrieveString(resultSet, identifierCol, identifierCol, query, resultSetMetaData);
          if (!StringUtils.isBlank(identifierValue)) {
            
            if (identifiersForIdentifierToMap.contains(identifierValue)) {
              resultIdentifierToSubject.put(identifierValue, subject);
              foundValue = true;
              break;
            }
            
          }
        }
        
      }
      if (!foundValue) {
        throw new RuntimeException("Why did a query by identifier return a subject " +
            "which cant be found by identifier??? " + SubjectApiUtils.subjectToString(subject)
            + ", " + query + " in source: " + this.getId());
      }
    }
    
    return subject;
  }
  
  
  

}
