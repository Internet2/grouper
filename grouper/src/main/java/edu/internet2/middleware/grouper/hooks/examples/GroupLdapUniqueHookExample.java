package edu.internet2.middleware.grouper.hooks.examples;
import java.util.List;

import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * this is just an example
 * compile this class using the Grouper jar on classpath: javac -cp grouper-2.6.5.jar GroupLdapUniqueHook.java
 * put the classfile in the container: /opt/grouper/grouperWebapp/WEB-INF/classes/GroupLdapUniqueHook.class
 * register the class in grouper.properties:
 * hooks.group.class=GroupLdapUniqueHook
 * @author mchyzer
 *
 */
public class GroupLdapUniqueHookExample extends GroupHooks {

  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    
    String groupName = preInsertBean.getGroup().getName();
    
    // assume flat provisioned group... could be bushy if you like
    List<String> existingCns = LdapSessionUtils.ldapSession().list(String.class, 
        "myLdapServer", "ou=groups,dc=school", LdapSearchScope.ONELEVEL_SCOPE, 
        "cn=" + GrouperUtil.ldapEscapeRdnValue(groupName), "cn");
    
    // veto this action and show reason in UI for user
    if (GrouperUtil.length(existingCns) > 0) {
      throw new HookVeto("externalized.text.key.for.ldap.group.exists", 
          "This group name exists in LDAP, please use a different folder or name");
    }
  }
}
