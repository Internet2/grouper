/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.examples;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;


/**
 *
 */
public class LdapExample2 {

  /**
   * 
   * @param url e.g. ldaps://ldap.school.edu/dc=school,dc=edu
   * @param user
   * @param pass
   * @return the context
   * @throws NamingException 
   */
  @SuppressWarnings("unchecked")
  public static DirContext context(String url, String user, String pass) throws NamingException {
    // Set up the environment for creating the initial context
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, 
        "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, url);

    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, "uid=" + user + ",ou=entities,dc=upenn,dc=edu");
    env.put(Context.SECURITY_CREDENTIALS, pass);

    // Create the initial context
    DirContext context = new InitialDirContext(env);
    return context;
  }
  
  /**
   * @param args
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    testLdap();
  }

  /**
   * @throws NamingException
   * @throws Exception
   */
  @SuppressWarnings({ "unused", "unchecked" })
  private static void testLdap() throws Exception {

    Properties properties = new Properties();
    
    //note this is a properties file with three lines:
    //user=someUser
    //pass=xxxxxx
    //url=ldaps://server.school.edu/dc=school,dc=edu
    
    properties.load(new FileInputStream(new File("r:/accounts/penngroups.properties")));
    String user = properties.getProperty("user");
    String pass = properties.getProperty("pass");
    String url = properties.getProperty("url");
    
    // Create the initial context
    DirContext dirContext = context(url, user, pass);
    
    
    SearchControls sc = new SearchControls();
    String[] attributeFilter = { "hasMember" };
    sc.setReturningAttributes(attributeFilter);
    
    NamingEnumeration namingEnumeration = dirContext.search("ou=groups", "(cn=test:isc:ait:apps:atlassian*)",sc);

    //print out session
    System.out.println("grouperSession = GrouperSession.startRootSession();\n");
    
    //printNamingEnumeration(namingEnumeration);
    //Search result: cn=test:isc:ait:apps:atlassian:groupsConfluence:paycard_admin,ou=groups,dc=upenn,dc=edu
    //Attribute: pennGloballyVisible: FALSE, 
    //Attribute: objectClass: pennGrouperGroup, eduMember, 
    //Attribute: hasMember: abc, 
    //Attribute: cn: test:isc:ait:apps:atlassian:groupsConfluence:paycard_admin, 

    //print out groups, save memberships
    StringBuilder memberships = new StringBuilder();
    while (namingEnumeration.hasMore()) {
      Object nextElement = namingEnumeration.next();
      SearchResult searchResult = (SearchResult)nextElement;
      //take off the initial "cn =", and the last ",ou=groups,dc=upenn,dc=edu"
      String groupName = searchResult.getNameInNamespace();
      groupName = groupName.substring("cn=".length(), groupName.length()-",ou=groups,dc=upenn,dc=edu".length()).trim();
      System.out.println("new GroupSave(grouperSession).assignName(\""
          + groupName + "\").assignCreateParentStemsIfNotExist(true).save();");
      
      //save the memberships
      Attributes attributes = searchResult.getAttributes();
      NamingEnumeration<? extends Attribute> attributeEnumeration = attributes.getAll();
      while (attributeEnumeration.hasMore()) {
        Attribute attribute = attributeEnumeration.next();
        if ("hasMember".equals(attribute.getID())) {
          NamingEnumeration attributeValues = attribute.getAll();
          while (attributeValues.hasMore()) {
            String netId = (String)attributeValues.next();
            memberships.append("addMember(\"" + groupName + "\", \"" + netId + "\");\n");
          }
        }
      }
      
    }
    System.out.println("");
    System.out.println(memberships.toString());

  }

}
