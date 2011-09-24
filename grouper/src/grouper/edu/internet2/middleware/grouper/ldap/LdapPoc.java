/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ldap;

import java.util.Iterator;

import javax.naming.directory.SearchResult;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.pool.BlockingLdapPool;
import edu.vt.middleware.ldap.pool.DefaultLdapFactory;


/**
 *
 */
public class LdapPoc {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    manyObjectsOneAttribute();

  }

  public static void manyObjectsOneAttribute() throws Exception {
    LdapConfig ldapConfig = new LdapConfig("ldaps://ldap.school.edu", "dc=school,dc=edu");
    
    ldapConfig.setBindDn("uid=user,ou=people,dc=school,dc=edu");
    ldapConfig.setBindCredential("xxxxxx");

    DefaultLdapFactory factory = new DefaultLdapFactory(ldapConfig);

    BlockingLdapPool pool = new BlockingLdapPool(factory);
    
    Ldap ldap = pool.checkOut();
    

    Iterator<SearchResult> results = ldap.search("ou=people",
        new SearchFilter("(personid=12345678)"), new String[]{"personid"});
    
    while (results.hasNext()) {
      
      SearchResult searchResult = results.next();
      System.out.println(searchResult.getAttributes().get("personid"));
    }
    

    
    pool.checkIn(ldap);
    
      //pool.getLdapPoolConfig().se

  }
  
}
