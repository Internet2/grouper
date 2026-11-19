/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
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
      
      // LdapSession.list evaluates a filter that starts with ${ and ends with } a second time,
      // as a script with static classes allowed.  This filter is already the result of the
      // custom UI expression, which can carry subject or group data, so a result that looks like
      // an expression must not reach it (second-order expression injection): refuse it
      if (filterIsExpression(filter)) {
        throw new RuntimeException("Custom UI LDAP filter must be a plain LDAP filter after substitution, "
            + "expression syntax (a filter starting with '${') is not allowed, configId: '" + configId + "'");
      }
      
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
   * true if the LDAP session would evaluate this filter as an expression
   * (LdaptiveSessionImpl.processSearchRequest trims it and checks for ${...}).  Only the start
   * is checked, so a near miss is refused too.
   * @param filter
   * @return true if the filter starts with ${ after trimming
   */
  static boolean filterIsExpression(String filter) {
    return filter != null && filter.trim().startsWith("${");
  }

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
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
            
        return null;
      }
    });

  }
  
  /**
   * 
   */
  public CustomUiLdap() {
  }

}
