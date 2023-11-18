/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * 
 */
public class CustomUiExpressionLanguage extends CustomUiUserQueryBase {

  /**
   * 
   * @param script 
   * @param group
   * @param subject 
   * @param customUiVariableType
   * @return result
   */
  public Object expression(String script, Group group, Subject subject, CustomUiVariableType customUiVariableType, Stem stem, AttributeDef attributeDef) {
    
    long startedNanos = System.nanoTime();

    try {

      customUiVariableType = GrouperUtil.defaultIfNull(customUiVariableType, CustomUiVariableType.BOOLEAN);
      
      // dont substitute the sql, for security reasons
      String result = CustomUiUtil.substituteExpressionLanguage(script, group, stem, attributeDef, subject, null);
      
      Object resultObject = customUiVariableType.convertTo(result);

      this.debugMapPut("resultObject", resultObject);
      
      return resultObject;
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("expressionLanguageError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("expressionLanguageTookMillis", (System.nanoTime()-startedNanos)/1000000);
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

    CustomUiExpressionLanguage customUiExpressionLanguage = new CustomUiExpressionLanguage();
    for (Subject subject : new Subject[]{subject1, subject2, subject3, subject4}) {
      
      boolean hasMembership = (Boolean)customUiExpressionLanguage.expression("${ edu.internet2.middleware.grouper.ui.customUi.CustomUiSql.hasSqlResultStatic(\"grouper\","
          + " \"select 1 from grouper_memberships_lw_v where group_name = ? and subject_id = ? and subject_source = 'pennperson' and "
          + "list_name = 'members'\", group, subject, group.getName(), \"string\", subject.getId(), \"string\", null, null)}", group, 
          subject, CustomUiVariableType.BOOLEAN, null, null);
          
      System.out.println(hasMembership);
    }
        
    GrouperSession.stopQuietly(grouperSession);

  }

  /**
   * 
   */
  public CustomUiExpressionLanguage() {
  }

}
