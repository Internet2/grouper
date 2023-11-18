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

public class GrouperAbacRow {

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
    if (grouperDataFieldConfig != null) {
      Object value = grouperDataFieldConfig.getFieldDataType().convertValue(valueOrScript);
      return this.dataAliasToValues.get(aliasLowerCase).contains(value);
    }
    return false;
  }

  private GrouperDataEngine grouperDataEngine;


  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
    
  }
  
}
