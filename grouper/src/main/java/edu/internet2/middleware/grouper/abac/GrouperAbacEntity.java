package edu.internet2.middleware.grouper.abac;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperAbacEntity {

  private Map<String, String> singleValuedGroupExtensionInFolder = null;

  public Map<String, String> getSingleValuedGroupExtensionInFolder() {
    return singleValuedGroupExtensionInFolder;
  }

  private Map<Long, Map<String, Set<Object>>> dataRowAssignInternalIdToDataFieldAliasToValues = null;
  

  
  
  
  // ${ entity.singleValuedEntityAttribute('personLdap', 'activeFlag') == 'T' }
  

  public Map<Long, Map<String, Set<Object>>> getDataRowAssignInternalIdToDataFieldAliasToValues() {
    return dataRowAssignInternalIdToDataFieldAliasToValues;
  }

  
  public void setDataRowAssignInternalIdToDataFieldAliasToValues(
      Map<Long, Map<String, Set<Object>>> dataRowAssignInternalIdToDataFieldAliasToValues) {
    this.dataRowAssignInternalIdToDataFieldAliasToValues = dataRowAssignInternalIdToDataFieldAliasToValues;
  }

  public void setSingleValuedGroupExtensionInFolder(Map<String, String> singleValuedGroupExtensionInFolder) {
    this.singleValuedGroupExtensionInFolder = singleValuedGroupExtensionInFolder;
  }

  
  // basis:affiliation:staff
  // basis:affiliation:student
  
  // ${ entity.multiValuedGroupExtensionInFolder('basis:affiliation').containsRegex('^(stu)|(fac).*$') }
  
  private Map<String, Set<String>> multiValuedGroupExtensionInFolder = null;

  public Map<String, Set<String>> multiValuedGroupExtensionInFolder(String folderName) {
    return multiValuedGroupExtensionInFolder;
  }

  public void setMultiValuedGroupExtensionInFolder(Map<String, Set<String>> multiValuedGroupExtensionInFolder) {
    this.multiValuedGroupExtensionInFolder = multiValuedGroupExtensionInFolder;
  }
  
  private Set<String> memberOfGroupNames;
  
  
  
  public void setMemberOfGroupNames(Set<String> memberOfGroupNames) {
    this.memberOfGroupNames = memberOfGroupNames;
  }

  public boolean memberOf(String groupName) {
    return this.memberOfGroupNames.contains(groupName);
  }

  private Map<String, Set<Object>> dataAliasToValues = new HashMap<>();

  public Map<String, Set<Object>> getDataAliasToValues() {
    return dataAliasToValues;
  }
  
  public void setDataAliasToValues(
      Map<String, Set<Object>> dataFieldInternalIdToValues) {
    this.dataAliasToValues = GrouperUtil.nonNull(dataFieldInternalIdToValues);
  }

  private String memberId;
  
  public String getMemberId() {
    return memberId;
  }
  
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }


  /**
   * 
   * @param aliasName
   * @param valueOrScript
   * @return
   */
  public boolean hasAttribute(String aliasName, String valueOrScript) {

    String aliasLowerCase = aliasName.toLowerCase();
    GrouperDataFieldConfig grouperDataFieldConfig = this.grouperDataEngine.getFieldConfigByAlias().get(aliasLowerCase);
    GrouperDataRowConfig grouperDataRowConfig = this.grouperDataEngine.getRowConfigByAlias().get(aliasLowerCase);
    if (grouperDataFieldConfig != null) {
      Object value = grouperDataFieldConfig.getFieldDataType().convertValue(valueOrScript);
      return this.dataAliasToValues.get(aliasLowerCase).contains(value);
    } else if (grouperDataRowConfig != null) {
      
      if (this.dataRowAssignInternalIdToDataFieldAliasToValues != null) {
        
        for (Long rowAssignId : this.dataRowAssignInternalIdToDataFieldAliasToValues.keySet()) {

          GrouperDataRowWrapper grouperDataRowWrapper = this.grouperDataEngine.getGrouperDataProviderIndex().getRowWrapperByLowerAlias().get(aliasLowerCase);
          if (StringUtils.equals(grouperDataRowConfig.getConfigId(), grouperDataRowWrapper.getGrouperDataRowConfig().getConfigId())) {
            Map<String, Object> variableMap = new HashMap<String, Object>();
            
            Map<String, Set<Object>> dataFieldAliasToValues = this.dataRowAssignInternalIdToDataFieldAliasToValues.get(rowAssignId);
            GrouperAbacRow grouperAbacRow = new GrouperAbacRow();
            grouperAbacRow.setMemberId(this.memberId);
            grouperAbacRow.setGrouperDataEngine(this.grouperDataEngine);
            grouperAbacRow.setDataAliasToValues(dataFieldAliasToValues);
            variableMap.put(aliasName, grouperAbacRow);
            if (!valueOrScript.trim().startsWith("${")) {
              valueOrScript = "${" + valueOrScript + "}";
            }
            Object result = GrouperUtil.substituteExpressionLanguageScript(valueOrScript, variableMap, true, false, true);
            boolean isTrue = GrouperUtil.booleanValue(result);
            return isTrue;
          }
        }
      }
    }
    return false;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperAbacEntity.class);

  private GrouperDataEngine grouperDataEngine;


  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
    
  }
  
}
