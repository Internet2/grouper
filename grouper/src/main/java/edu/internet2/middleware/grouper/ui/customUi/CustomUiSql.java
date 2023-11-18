/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;


/**
 * 
 */
public class CustomUiSql extends CustomUiUserQueryBase {


  /**
   * 
   * @param configId 
   * @param query 
   * @param group
   * @param subject 
   * @param bindVar0 
   * @param bindVar0type 
   * @param bindVar1 
   * @param bindVar1type 
   * @param bindVar2 
   * @param bindVar2type 
   * @return result
   */
  public static boolean hasSqlResultStatic(String configId, String query, Group group, Subject subject, String bindVar0, 
      String bindVar0type, String bindVar1, String bindVar1type, String bindVar2, String bindVar2type) {
    CustomUiSql customUiSql = new CustomUiSql();
    return (Boolean)customUiSql.sqlResult(configId, query, group, null, null, subject, bindVar0, bindVar0type, bindVar1, bindVar1type, bindVar2, bindVar2type, CustomUiVariableType.BOOLEAN);
  }

  /**
   * 
   * @param configId 
   * @param query 
   * @param group
   * @param subject 
   * @param bindVar0 
   * @param bindVar0type 
   * @param bindVar1 
   * @param bindVar1type 
   * @param bindVar2 
   * @param bindVar2type 
   * @return result
   */
  public Object sqlResult(String configId, String query, Group group, Stem stem, AttributeDef attributeDef, Subject subject, String bindVar0, 
      String bindVar0type, String bindVar1, String bindVar1type, String bindVar2, String bindVar2type, CustomUiVariableType customUiVariableType) {
    
    long startedNanos = System.nanoTime();

    try {

      // dont substitute the sql, for security reasons
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(configId).sql(query);
  
      attachBindVar(gcDbAccess, group, stem, attributeDef, subject, "0", bindVar0, bindVar0type);
      attachBindVar(gcDbAccess, group, stem, attributeDef, subject, "1", bindVar1, bindVar1type);
      attachBindVar(gcDbAccess, group, stem, attributeDef, subject, "2", bindVar2, bindVar2type);
      
      customUiVariableType = GrouperUtil.defaultIfNull(customUiVariableType, CustomUiVariableType.BOOLEAN);
      
      Object result = gcDbAccess.select(customUiVariableType.sqlResultClass());

      result = customUiVariableType.sqlConvertResult(result);
      
      this.debugMapPut("sqlResult", result);
      
      return result;
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("sqlError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("sqlTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }

  }
  
  /**
   * @param gcDbAccess
   * @param group
   * @param stem 
   * @param attributeDef 
   * @param subject
   * @param bindVarLabel 
   * @param bindVar
   * @param bindVarType
   */
  private void attachBindVar(GcDbAccess gcDbAccess, Group group, Stem stem, AttributeDef attributeDef, Subject subject, String bindVarLabel,
      String bindVar, String bindVarType) {
    if (bindVarType != null) {
      String originalBindVar = bindVar;
      bindVar = CustomUiUtil.substituteExpressionLanguage(bindVar, group, stem, attributeDef, subject, null);
      if (!StringUtils.isBlank(bindVar)) {
        this.debugMapPut("bindVar_" + bindVarLabel, bindVar);
      }
      if (bindVar == null) {
        throw new RuntimeException("bindVar is null! '" + originalBindVar + "'");
      }
      Object bindVarObject = null;
      if ("string".equalsIgnoreCase(bindVarType)) {
        bindVarObject = bindVar;
      } else if ("integer".equalsIgnoreCase(bindVarType)) {
        bindVarObject = GrouperUtil.longValue(bindVar);
      } else {
        throw new RuntimeException("Invalid bindVarType!  must be string or integer but was '" + bindVarType + "'");
      }
      gcDbAccess.addBindVar(bindVarObject);
    }
  }

  public static void main(String[] args) throws Exception {
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject1 = SubjectFinder.findById("10021368", true);
    Subject subject2 = SubjectFinder.findById("13228666", true);
    Subject subject3 = SubjectFinder.findById("10002177", true);
    Subject subject4 = SubjectFinder.findById("15251428", true);
    
    
    Group group = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod", true);

    CustomUiSql customUiSql = new CustomUiSql();
    for (Subject subject : new Subject[]{subject1, subject2, subject3, subject4}) {
      boolean hasMembership = (Boolean)customUiSql.sqlResult("grouper", "select 1 from grouper_memberships_lw_v where group_name = ? and subject_id = ?"
          + " and subject_source = 'pennperson' and list_name = 'members'", group, null, null, subject, "${group.getName()}", "string", 
          "${subject.getId()}", "string", null, null, CustomUiVariableType.BOOLEAN);
          
      System.out.println(hasMembership);
    }
        
    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * 
   */
  public CustomUiSql() {
  }

}
