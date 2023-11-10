package edu.internet2.middleware.grouper.app.dataProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldConfig;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperLdapDataProviderQueryTargetDao extends GrouperDataProviderQueryTargetDao {

  @Override
  public List<Object[]> selectData(Map<String, Integer> lowerColumnNameToZeroIndex) {
    List<Object[]> rows = new ArrayList<Object[]>();
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();
    List<String> ldapAttributes = new ArrayList<String>();

    retrieveMetadata(ldapAttributes, lowerColumnNameToZeroIndex);
   
    if (ldapAttributes.size() == 0) {
      //??
      return rows;
    }
    
    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(
        grouperDataProviderQueryConfig.getProviderQueryLdapConfigId(), 
        grouperDataProviderQueryConfig.getProviderQueryLdapBaseDn(), 
        LdapSearchScope.valueOfIgnoreCase(grouperDataProviderQueryConfig.getProviderQueryLdapSearchScope(), true), 
        grouperDataProviderQueryConfig.getProviderQueryLdapFilter(), 
        ldapAttributes.toArray(new String[0]), null);
    
    processLdapEntries(ldapAttributes, rows, ldapEntries);
    return rows;
  }
  
  private void processLdapEntries(List<String> ldapAttributes, List<Object[]> rows, List<LdapEntry> ldapEntries) {
    for (LdapEntry ldapEntry : ldapEntries) {
      Object[] row = new Object[ldapAttributes.size()];
      
      for (int i = 0; i < ldapAttributes.size(); i++) {
        Object value = null;

        String ldapAttributeString = ldapAttributes.get(i);
        LdapAttribute ldapAttribute = ldapEntry.getAttribute(ldapAttributeString);
        if (ldapAttribute != null && ldapAttribute.getStringValues().size() > 0) {
          if (ldapAttribute.getStringValues().size() == 1) {
            value = ldapAttribute.getStringValues().iterator().next();
          } else {
            value = new HashSet<String>(ldapAttribute.getStringValues()); 
          }
        }
        
        row[i] = value;
      }
      
      rows.add(row);
    }
  }
  
  private void retrieveMetadata(List<String> ldapAttributes, Map<String, Integer> lowerColumnNameToZeroIndex) {
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs()) {
      ldapAttributes.add(grouperDataProviderQueryFieldConfig.getProviderDataFieldAttribute());
    }
    
    if (ldapAttributes.size() == 0) {
      //??
      return;
    }
    
    if (!ldapAttributes.contains(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute())) {
      ldapAttributes.add(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute());
    }
    
    for (int i = 0; i < ldapAttributes.size(); i++) {
      lowerColumnNameToZeroIndex.put(ldapAttributes.get(i).toLowerCase(), i);
    }
  }
  
  @Override
  public List<Object[]> selectChangeLogData(Map<String, Integer> lowerColumnNameToZeroIndex, Timestamp changesFromTimestamp, Timestamp changesToTimestamp) {
    throw new RuntimeException("Not implemented");
  }
  
  @Override
  public List<Object[]> selectDataByMembers(Map<String, Integer> lowerColumnNameToZeroIndex, Set<Member> members) {
    List<Object[]> rows = new ArrayList<Object[]>();
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();
    List<String> ldapAttributes = new ArrayList<String>();

    retrieveMetadata(ldapAttributes, lowerColumnNameToZeroIndex);
   
    if (ldapAttributes.size() == 0) {
      //??
      return rows;
    }
    
    if (members.size() > 0) {
      int batchSize = 200;
      List<Member> membersList = new ArrayList<Member>(members);
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(membersList.size(), batchSize, true);
      for (int i=0;i<numberOfBatches;i++) {
        List<Member> batchMembers = GrouperUtil.batchList(membersList, batchSize, i);
        
        StringBuilder ldapFilter = new StringBuilder("(&" + grouperDataProviderQueryConfig.getProviderQueryLdapFilter() + "(|");
        for (Member member : batchMembers) {
          ldapFilter.append("(" + grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute() + "=");

          if ("subjectIdentifier".equals(grouperDataProviderQueryConfig.getProviderQuerySubjectIdType())) {
            // we probably shouldn't assume this is subjectIdentifier0???
            ldapFilter.append(GrouperUtil.ldapFilterEscape(member.getSubjectIdentifier0()));
          } else {
            ldapFilter.append(GrouperUtil.ldapFilterEscape(member.getSubjectId()));
          }
          
          ldapFilter.append(")");
        }
        ldapFilter.append("))");
        List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(
            grouperDataProviderQueryConfig.getProviderQueryLdapConfigId(), 
            grouperDataProviderQueryConfig.getProviderQueryLdapBaseDn(), 
            LdapSearchScope.valueOfIgnoreCase(grouperDataProviderQueryConfig.getProviderQueryLdapSearchScope(), true), 
            ldapFilter.toString(), 
            ldapAttributes.toArray(new String[0]), null);
        
        processLdapEntries(ldapAttributes, rows, ldapEntries);
      }
    }
    
    return rows;
  }
}
