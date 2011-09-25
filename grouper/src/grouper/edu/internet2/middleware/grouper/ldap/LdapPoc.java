/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ldap;

import java.util.Iterator;
import java.util.List;

import javax.naming.directory.SearchResult;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;
import edu.vt.middleware.ldap.pool.LdapPoolConfig;


/**
 *
 */
public class LdapPoc {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    //manyObjectsOneAttribute();
    
    for (int i=0;i<100;i++) {
      manyObjectsOneAttributeFramework();
    }
    

  }

  /**
   * 
   * @throws Exception
   */
  public static void manyObjectsOneAttribute() throws Exception {
    LdapConfig ldapConfig = new LdapConfig("ldaps://ldap.school.edu", "dc=school,dc=edu");
    
    ldapConfig.setBindDn("uid=user,ou=people,dc=school,dc=edu");
    ldapConfig.setBindCredential("xxxxxx");

    DefaultLdapFactory factory = new DefaultLdapFactory(ldapConfig);
    
    LdapPoolConfig ldapPoolConfig = null;
    
    BlockingLdapPool pool = new BlockingLdapPool(factory);
    
    Ldap ldap = pool.checkOut();
    

    Iterator<SearchResult> results = ldap.search("ou=people",
        new SearchFilter("(personid=12345678)"), new String[]{"personid"});
    
    while (results.hasNext()) {
      
      SearchResult searchResult = results.next();
      searchResult.getAttributes().get("whatever").size();
      System.out.println(searchResult.getAttributes().get("personid"));
    }
    

    
    pool.checkIn(ldap);
    
      //pool.getLdapPoolConfig().se

  }
  
  /**
   * 
   */
  public static void manyObjectsOneAttributeFramework() {
    
    List<String> results = LdapSession.list(String.class, "personLdap", "ou=pennnames", 
        null, "(|(pennname=mchyzer)(pennname=choate))", "pennid");
    
    for (String result : results) {
      System.out.println(result);
    }
    
    results = LdapSession.list(String.class, "personLdap", "ou=groups", 
        null, "(|(cn=test:testGroup)(cn=test:ldaptesting:test1))", "hasMember");
    
    for (String result : results) {
      System.out.println(result);
    }
    
    results = LdapSession.list(String.class, "personLdap", "ou=groups", 
        null, "(|(cn=test:testGroup)(cn=test:ldaptesting:test1))", "dn");

    for (String result : results) {
      System.out.println(result);
    }

    
  }
  
}
