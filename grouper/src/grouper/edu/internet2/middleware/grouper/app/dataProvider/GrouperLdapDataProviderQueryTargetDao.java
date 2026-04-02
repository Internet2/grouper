package edu.internet2.middleware.grouper.app.dataProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldConfig;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperLdapDataProviderQueryTargetDao extends GrouperDataProviderQueryTargetDao {

  /**
   * {@inheritDoc}
   * searches LDAP with only the subject id attribute returned, then lowercases
   * and sorts the values in Java using a TreeSet (natural String order).
   */
  @Override
  public List<String> selectDistinctSubjectIds() {
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();

    String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute();

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(
        grouperDataProviderQueryConfig.getProviderQueryLdapConfigId(),
        grouperDataProviderQueryConfig.getProviderQueryLdapBaseDn(),
        LdapSearchScope.valueOfIgnoreCase(grouperDataProviderQueryConfig.getProviderQueryLdapSearchScope(), true),
        grouperDataProviderQueryConfig.getProviderQueryLdapFilter(),
        new String[] { subjectIdAttribute }, null);

    Set<String> subjectIds = new TreeSet<>();
    for (LdapEntry ldapEntry : ldapEntries) {
      LdapAttribute ldapAttribute = ldapEntry.getAttribute(subjectIdAttribute);
      if (ldapAttribute != null) {
        for (String value : ldapAttribute.getStringValues()) {
          if (value != null) {
            subjectIds.add(value.toLowerCase());
          }
        }
      }
    }

    return new ArrayList<>(subjectIds);
  }

  /**
   * {@inheritDoc}
   * uses an LDAP range filter: (&lt;baseFilter&gt;(attr&gt;=from)(attr&lt;=to))
   * to retrieve entries whose subject id falls within the range.
   * note: LDAP >= and <= comparisons are lexicographic on the attribute's matching rule
   */
  @Override
  public List<Object[]> selectDataBySubjectIdRange(Map<String, Integer> lowerColumnNameToZeroIndex, String fromSubjectIdLower, String toSubjectIdLower) {
    List<Object[]> rows = new ArrayList<Object[]>();
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();
    List<String> ldapAttributes = new ArrayList<String>();

    retrieveMetadata(ldapAttributes, lowerColumnNameToZeroIndex);

    if (ldapAttributes.size() == 0) {
      return rows;
    }

    String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute();
    String baseFilter = grouperDataProviderQueryConfig.getProviderQueryLdapFilter();
    String rangeFilter = "(&" + baseFilter + "(" + subjectIdAttribute + ">=" + GrouperUtil.ldapFilterEscape(fromSubjectIdLower) + ")(" + subjectIdAttribute + "<=" + GrouperUtil.ldapFilterEscape(toSubjectIdLower) + "))";

    List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(
        grouperDataProviderQueryConfig.getProviderQueryLdapConfigId(),
        grouperDataProviderQueryConfig.getProviderQueryLdapBaseDn(),
        LdapSearchScope.valueOfIgnoreCase(grouperDataProviderQueryConfig.getProviderQueryLdapSearchScope(), true),
        rangeFilter,
        ldapAttributes.toArray(new String[0]), null);

    processLdapEntries(ldapAttributes, rows, ldapEntries);
    return rows;
  }

  /**
   * {@inheritDoc}
   * builds an LDAP OR filter with each subject id as an equality match:
   * (&lt;baseFilter&gt;(|(attr=id1)(attr=id2)...)).
   * batches in groups of 800 to keep the filter size reasonable
   */
  @Override
  public List<Object[]> selectDataBySubjectIds(Map<String, Integer> lowerColumnNameToZeroIndex, List<String> subjectIdsLower) {
    List<Object[]> rows = new ArrayList<Object[]>();
    GrouperLdapDataProviderQueryConfig grouperDataProviderQueryConfig = (GrouperLdapDataProviderQueryConfig)this.getGrouperDataProviderQuery().retrieveGrouperDataProviderQueryConfig();
    List<String> ldapAttributes = new ArrayList<String>();

    retrieveMetadata(ldapAttributes, lowerColumnNameToZeroIndex);

    if (ldapAttributes.size() == 0) {
      return rows;
    }

    if (subjectIdsLower.size() > 0) {
      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute();
      int batchSize = 800;

      int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectIdsLower.size(), batchSize, true);
      for (int i = 0; i < numberOfBatches; i++) {
        List<String> batchSubjectIds = GrouperUtil.batchList(subjectIdsLower, batchSize, i);

        StringBuilder ldapFilter = new StringBuilder("(&" + grouperDataProviderQueryConfig.getProviderQueryLdapFilter() + "(|");
        for (String subjectId : batchSubjectIds) {
          ldapFilter.append("(" + subjectIdAttribute + "=" + GrouperUtil.ldapFilterEscape(subjectId) + ")");
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
