/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * 
 */
public class CustomUiLdap extends CustomUiUserQueryBase {

  /**
   * 
   * @param configId 
   * @param searchDn 
   * @param filter 
   * @param attributeToRetrieve 
   * @param group
   * @param subject 
   * @param customUiVariableType 
   * @return value
   */
  public Object ldapFilter(String configId, String searchDn, String filter, String attributeToRetrieve, Group group, Subject subject, CustomUiVariableType customUiVariableType) {
    
    long startedNanos = System.nanoTime();

    try {
      filter = CustomUiUtil.substituteExpressionLanguage(filter, group, null, null, subject, null);
      
      this.debugMapPut("ldapFilter", filter);
      
      List<String> value = LdapSessionUtils.ldapSession().list(String.class, configId, searchDn, LdapSearchScope.SUBTREE_SCOPE, 
          filter,attributeToRetrieve);

      customUiVariableType = GrouperUtil.defaultIfNull(customUiVariableType, CustomUiVariableType.BOOLEAN);
      if (customUiVariableType == CustomUiVariableType.BOOLEAN) {
        return value != null && value.size() > 0 && !StringUtils.isBlank(value.get(0));
      }
      if (value == null || value.size() == 0) {
        return customUiVariableType.convertTo(value);
      }
      if (value.size() > 1) {
        throw new RuntimeException("Found more than one result! '" + filter + "', '" + SubjectHelper.getPretty(subject) + "', attribute: '" + attributeToRetrieve + "'");
      }
      Object result = customUiVariableType.convertTo(value.get(0));

      this.debugMapPut("ldapResult", result);

      return result;
      
    } catch (RuntimeException re) {
      
      this.debugMapPut("ldapError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("ldapTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }

    
  }
  
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject1 = SubjectFinder.findById("10021368", true);
    Subject subject2 = SubjectFinder.findById("13228666", true);
    Subject subject3 = SubjectFinder.findById("10002177", true);
    Subject subject4 = SubjectFinder.findById("15251428", true);
    
    
    Group group = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:O365:twoStepProd:o365_two_step_prod", true);
    CustomUiLdap customUiLdap = new CustomUiLdap();
    
    for (Subject subject : new Subject[]{subject1, subject2, subject3, subject4}) {
      boolean hasMembership = (Boolean)customUiLdap.ldapFilter("oneProdAd", "DC=one,DC=upenn,DC=edu", 
          "(&(objectclass=user)(employeeID=${subject.getId()})(memberof=CN=${group.getName()},OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu))", 
          "employeeID", group, subject, null);
          
      System.out.println(hasMembership);
    }
        
    GrouperSession.stopQuietly(grouperSession);

  }
  
  /**
   * 
   */
  public CustomUiLdap() {
  }

}
