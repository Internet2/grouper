package edu.internet2.middleware.grouper.subj;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.JDBCSourceAdapter2;
import edu.internet2.middleware.subject.provider.JdbcSubjectAttributeSet;

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
  
  
  

}
