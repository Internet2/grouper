import edu.internet2.middleware.grouper.app.loader.ldap.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;

Map<String, Object> elVariableMap = new HashMap<String, Object>();

elVariableMap.put("loaderLdapElUtils", new GrouperUtil());
elVariableMap.put("grouperUtil", new GrouperUtil());
elVariableMap.put("subjectId", "cn=testGroup2,ou=test4b,ou=test3,ou=test2,ou=test,ou=Groups,dc=example,dc=edu");