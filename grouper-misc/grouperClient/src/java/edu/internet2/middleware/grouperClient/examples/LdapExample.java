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
 * $Id: LdapExample.java,v 1.3 2008-12-04 20:59:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
public class LdapExample {

  /**
   * 
   * @param url e.g. ldaps://penngroups.upenn.edu/dc=upenn,dc=edu
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
    if (args.length == 0) {
      
      System.err.println("This program runs queries against ldap and web services");
      System.err.println("The system exit code will be 0 for success, and not 0 for failure");
      System.err.println("Output data is printed to stdout, error messages are printed to stderr");
      System.err.println("\npennname to pennid usage: java -jar grouperClient --user=kerberosPrincipal --pass=thePass --operation=pennnameToPennid --pennnameToDecode=pennname");
      System.err.println("  e.g.: java -jar grouperClient --user=penngroups/medley.isc-seo.upenn.edu --pass=xxxxxx --operation=pennnameToPennid --pennnameToDecode=jsmith");
      System.err.println("  output: pennid: 12341234");
      System.err.println("\npennid to pennname usage: java -jar grouperClient --user=kerberosPrincipal --pass=thePass --operation=pennidToPennkey --pennidToDecode=pennid");
      System.err.println("  e.g.: java -jar grouperClient --user=penngroups/medley.isc-seo.upenn.edu --pass=xxxxxx --operation=pennidToPennkey --pennidToDecode=12341234");
      System.err.println("  output: pennname: jsmith");
      System.err.println("\nis in group usage: java -jar grouperClient --user=kerberosPrincipal --pass=thePass --engine=ldap --operation=isInGroup --groupName=groupName --pennnameToCheck=pennkey");
      System.err.println("  e.g.: java -jar grouperClient --user=penngroups/medley.isc-seo.upenn.edu --pass=xxxxxx --engine=ldap --operation=isInGroup --groupName=penn:myfolder:mygroup --pennnameToCheck=jsmith");
      System.err.println("  output: isInGroup: true");
      System.err.println("\ngroup list usage: java -jar grouperClient --user=kerberosPrincipal --pass=thePass --engine=ldap --operation=groupList --groupName=groupName");
      System.err.println("  e.g.: java -jar grouperClient --user=penngroups/medley.isc-seo.upenn.edu --pass=xxxxxx --engine=ldap --operation=groupList --groupName=penn:myfolder:mygroup");
      System.err.println("  output: groupList: jsmith, tsmith, msmith");
      System.err.println("  note: extremely large group lists might not display (e.g. over 20k members)");
      System.exit(1);
    }
    Map<String, String> argMap = GrouperClientUtils.argMap(args);
    Map<String, String> argMapNotUsed = new HashMap<String, String>(argMap);

    //testLdap();
    
    // Set up the environment for creating the initial context
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, 
        "com.sun.jndi.ldap.LdapCtxFactory");
    
    Properties properties = GrouperClientUtils.propertiesFromResourceName(
        "grouper.client.properties", true, true, GrouperClientUtils.class, null);
    String ldapUrl = GrouperClientUtils.propertiesValue(properties, "grouperClient.ldap.url");
    
    env.put(Context.PROVIDER_URL, ldapUrl);

    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    String user = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "user", true);
    env.put(Context.SECURITY_PRINCIPAL, "uid=" + user + ",ou=entities,dc=upenn,dc=edu");
    String pass = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "pass", true);
    env.put(Context.SECURITY_CREDENTIALS, pass);

    // Create the initial context
    DirContext context = new InitialDirContext(env);
    String operation = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "operation", true);
    if (GrouperClientUtils.equals(operation, "pennnameToPennid")) {
      String pennnameToDecode = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "pennnameToDecode", true);
      String pennid = pennnameToPennid(context, pennnameToDecode);
      System.out.println("pennid: " + GrouperClientUtils.defaultString(pennid));
    } else if (GrouperClientUtils.equals(operation, "pennidToPennname")) {
      String pennidToDecode = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "pennidToDecode", true);
      String pennname = pennidToPennname(context, pennidToDecode);
      System.out.println("pennname: " + GrouperClientUtils.defaultString(pennname));
    } else if (GrouperClientUtils.equals(operation, "isInGroup")) {
      String groupName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "groupName", true);
      String pennnameToCheck = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "pennnameToCheck", true);
      boolean isInGroup = isInGroup(context, groupName, pennnameToCheck);
      System.out.println("isInGroup: " + isInGroup);
    } else if (GrouperClientUtils.equals(operation, "groupList")) {
      String groupName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "groupName", true);
      List<String> results = groupList(context, groupName);
      System.out.print("groupList: ");
      for (int i=0;i<GrouperClientUtils.length(results);i++) {
        if (i != 0) {
          System.out.print(", ");
        }
        System.out.print(results.get(i));
      }
      System.out.println("");
    } else {
      System.err.println("Unexpected operation: '" + operation + "'");
      System.exit(1);
    }
  }

  /**
   * 
   * @param context
   * @param pennname
   * @return the pennid
   * @throws Exception 
   */
  public static String pennnameToPennid(DirContext context, String pennname) throws Exception {
    SearchControls searchControls = new SearchControls();
    searchControls.setReturningAttributes(new String[]{"pennid"});
    searchControls.setReturningObjFlag(false);
    searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    NamingEnumeration namingEnumeration = context.search("ou=pennnames", "name=" + pennname, searchControls);
    //printNamingEnumeration(namingEnumeration);
    return retrieveAttributeStringValue(namingEnumeration, "pennid");
  }
  
  /**
   * 
   * @param context
   * @param pennid
   * @return the pennid
   * @throws Exception 
   */
  public static String pennidToPennname(DirContext context, String pennid) throws Exception {
    SearchControls searchControls = new SearchControls();
    searchControls.setReturningAttributes(new String[]{"pennname"});
    searchControls.setReturningObjFlag(false);
    searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    NamingEnumeration namingEnumeration = context.search("ou=pennnames", "pennid=" + pennid, searchControls);
    //printNamingEnumeration(namingEnumeration);
    return retrieveAttributeStringValue(namingEnumeration, "pennname");
  }
  
  /**
   * see if a user is in a group
   * @param context
   * @param groupName
   * @param pennname
   * @return true or false
   * @throws NamingException 
   */
  public static boolean isInGroup(DirContext context, String groupName, String pennname) throws NamingException {
    Attributes searchAttributes = new BasicAttributes();
    searchAttributes.put(new BasicAttribute("cn", groupName));
    searchAttributes.put(new BasicAttribute("hasMember", pennname));
    
    NamingEnumeration namingEnumeration = context.search(
        "ou=groups",
        searchAttributes, new String[]{"cn"});
    String cn = retrieveAttributeStringValue(namingEnumeration, "cn");
    //printNamingEnumeration(namingEnumeration);
    boolean isInGroup = GrouperClientUtils.equals(groupName, cn);
    return isInGroup;
  }
  
  /**
   * see if a user is in a group
   * @param context
   * @param groupName
   * @return the list
   * @throws NamingException 
   */
  public static List<String> groupList(DirContext context, String groupName) throws NamingException {
    Attributes searchAttributes = new BasicAttributes();
    searchAttributes.put(new BasicAttribute("cn", groupName));
    
    NamingEnumeration namingEnumeration = context.search(
        "ou=groups",
        searchAttributes, new String[]{"hasMember"});
    List<String> members = retrieveAttributeStringListValue(namingEnumeration, "hasMember");
    return members;
  }
  
  /**
   * @throws NamingException
   * @throws Exception
   */
  @SuppressWarnings({ "unused", "unchecked" })
  private static void testLdap() throws NamingException, Exception {
    // Set up the environment for creating the initial context
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, 
        "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldaps://penngroups.upenn.edu/dc=upenn,dc=edu");

    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, "uid=penngroups/medley.isc-seo.upenn.edu,ou=entities,dc=upenn,dc=edu");
    env.put(Context.SECURITY_CREDENTIALS, "xxxxxxxxx");

    // Create the initial context
    DirContext ctx = new InitialDirContext(env);

    SearchControls searchControls = new SearchControls();
    searchControls.setReturningAttributes(new String[]{"pennid"});
    searchControls.setReturningObjFlag(false);
    searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    NamingEnumeration namingEnumeration3 = ctx.search("ou=pennnames", "name=jorj", searchControls);
    printNamingEnumeration(namingEnumeration3);
    
    
    //Attribute: pennGloballyVisible: TRUE, 
    //Attribute: objectClass: pennGrouperGroup, eduMember, 
    //Attribute: hasMember: penngroups/medley.isc-seo.upenn.edu, penn_groups/medley.isc-seo.upenn.edu, mchyzer, 
    //Attribute: cn: penn:etc:webServiceClientUsers, 
    System.out.println("Group: ");
    Attributes attributes = ctx.getAttributes("cn=penn:isc:ait:apps:fast:pennCommunity,ou=groups", new String[]{"hasMember"});
    NamingEnumeration<? extends Attribute> namingEnumeration2 = attributes.getAll();
    printNamingEnumeration(namingEnumeration2);
    System.out.println("Group2: ");
    attributes = ctx.getAttributes("cn=penn:isc:ait:apps:fast:pennCommunity,ou=groups");
    namingEnumeration2 = attributes.getAll();
    printNamingEnumeration(namingEnumeration2);

    System.out.println("Group by hasMember: ");
    Attributes searchAttributes = new BasicAttributes();
    searchAttributes.put(new BasicAttribute("cn", "penn:isc:ait:apps:fast:pennCommunity"));
    searchAttributes.put(new BasicAttribute("hasMember", "mchyzer1"));
    
    NamingEnumeration namingEnumeration7 = ctx.search(
        "ou=groups",
        searchAttributes, new String[]{"cn"});
    printNamingEnumeration(namingEnumeration7);
    
    //cant list entities
//    NamingEnumeration namingEnumeration5 = ctx.search((String)"", "ou=entities", null);
//    printNamingEnumeration(namingEnumeration5);

    //doesnt work
//  NamingEnumeration namingEnumeration6 = ctx.list("ou=entities");
//  System.out.println("ou=entities: ");
//  printNamingEnumeration(namingEnumeration6);

    // ... do something useful with ctx
    
//    NamingEnumeration namingEnumeration4 = ctx.list("pennid=10018604,ou=pennnames");
//    System.out.println("pennid lookup: ");
//    printNamingEnumeration(namingEnumeration4);
    
//    Attributes attributes = new BasicAttributes("pennid", 10021368);
//    NamingEnumeration namingEnumeration = ctx.list("ou=groups");
//    System.out.println("ou=groups: ");
    //javax.naming.directory.DirContext, cn=penn:etc:webServiceClientUsers, cn=penn:etc:webServiceClientUsers,ou=groups,dc=upenn,dc=edu
    //javax.naming.directory.DirContext, cn=penn:etc:webServiceActAsGroup, cn=penn:etc:webServiceActAsGroup,ou=groups,dc=upenn,dc=edu
//    printNamingEnumeration(namingEnumeration);
    
//    Attributes attributes = ctx.getAttributes("cn=penn:etc:webServiceClientUsers,ou=groups");
//    NamingEnumeration<? extends Attribute> namingEnumeration2 = attributes.getAll();
//    printNamingEnumeration(namingEnumeration2);
      //Attribute: pennGloballyVisible: TRUE, 
      //Attribute: objectClass: pennGrouperGroup, eduMember, 
      //Attribute: hasMember: penngroups/medley.isc-seo.upenn.edu, penn_groups/medley.isc-seo.upenn.edu, mchyzer, 
      //Attribute: cn: penn:etc:webServiceClientUsers, 
    
    
//    NamingEnumeration namingEnumeration3 = ctx.search("ou=pennnames", "name=jorj", null);
//    printNamingEnumeration(namingEnumeration3);
      //Search result: pennid=10018604,ou=pennnames,dc=upenn,dc=edu
      //Attribute: pennid: 10018604, 
      //Attribute: objectClass: pennidTranslation, 
      //Attribute: pennname: jorj, 
  }

  /**
   * print attributes
   * @param attributes
   * @throws NamingException
   */
  public static void printAttributes(Attributes attributes) throws NamingException {
    NamingEnumeration<? extends Attribute> namingEnumeration = attributes.getAll();
    printNamingEnumeration(namingEnumeration);
  }
   
  /**
   * print out a naming enumeration
   * @param namingEnumeration
   * @throws NamingException 
   */
  public static void printNamingEnumeration(NamingEnumeration namingEnumeration) throws NamingException {
    while (namingEnumeration.hasMore()) {
      Object nextElement = namingEnumeration.next();
      if (nextElement instanceof Attribute) {
        Attribute attribute = (Attribute)nextElement;
        printAttribute(attribute);
      } else if (nextElement instanceof SearchResult) {
        SearchResult searchResult = (SearchResult)nextElement;
        System.out.println("Search result: " + searchResult.getNameInNamespace());
        Attributes attributes = searchResult.getAttributes();
        printAttributes(attributes);
      } else if (nextElement instanceof NameClassPair) {
        NameClassPair nameClassPair = (NameClassPair)namingEnumeration.nextElement();
        System.out.println("Name class pair: " + nameClassPair.getClassName() + ", " + nameClassPair.getNameInNamespace());
      } else {
        throw new RuntimeException("Not expecting type: " + nextElement);
      }
      
    }
    
  }
  
  /**
   * retrieve a single valued attribute as string
   * @param object
   * @param attributeName 
   * @throws NamingException 
   * @return the attribute value or null if not there
   */
  public static String retrieveAttributeStringValue(Object object, 
      String attributeName) throws NamingException {
    if (object == null) {
      return null;
    }
    if (object instanceof Attribute) {
      Attribute attribute = (Attribute)object;
      if (GrouperClientUtils.equals(attribute.getID(), attributeName)) {
        return (String)attribute.get();
      }
      return null;
    } else if (object instanceof SearchResult) {
      SearchResult searchResult = (SearchResult)object;
      Attributes attributes = searchResult.getAttributes();
      Attribute attribute = attributes.get(attributeName);
      return retrieveAttributeStringValue(attribute, attributeName);
    } else if (object instanceof NamingEnumeration) {
      NamingEnumeration namingEnumeration = (NamingEnumeration)object;
      if (!namingEnumeration.hasMore()) {
        return null;
      }
      Object next = namingEnumeration.next();
      if (namingEnumeration.hasMore()) {
        throw new RuntimeException("Expecting one result");
      }
      return retrieveAttributeStringValue(next, attributeName);
    } else {
      throw new RuntimeException("Not expecting type: " + object);
    }
  }
  
  /**
   * retrieve a string array of values
   * @param object
   * @param attributeName 
   * @throws NamingException 
   * @return the attribute value or null if not there
   */
  public static List<String> retrieveAttributeStringListValue(Object object, 
      String attributeName) throws NamingException {
    if (object == null) {
      return null;
    }
    if (object instanceof Attribute) {
      Attribute attribute = (Attribute)object;
      NamingEnumeration namingEnumeration = attribute.getAll();
      return retrieveAttributeStringListValue(namingEnumeration, attributeName);
    } else if (object instanceof SearchResult) {
      SearchResult searchResult = (SearchResult)object;
      Attributes attributes = searchResult.getAttributes();
      Attribute attribute = attributes.get(attributeName);
      return retrieveAttributeStringListValue(attribute, attributeName);
    } else if (object instanceof NamingEnumeration) {
      int size = 0;
      NamingEnumeration namingEnumeration = (NamingEnumeration)object;
      List<String> resultList = new ArrayList<String>();
      while (namingEnumeration.hasMore()) {
        Object next = namingEnumeration.next();
        if (next instanceof SearchResult) {
          if (size == 0 && !namingEnumeration.hasMore()) {
            return retrieveAttributeStringListValue(next, attributeName);
          }
          throw new RuntimeException("Error: multiple search results found!");
        }
        resultList.add((String)next);
        size++;
      }
      if (size == 0) {
        return null;
      }
      return resultList;
    } else {
      throw new RuntimeException("Not expecting type: " + object);
    }
  }
  
  /**
   * 
   * @param attribute
   * @throws NamingException 
   */
  public static void printAttribute(Attribute attribute) throws NamingException {
    System.out.print("Attribute: " + attribute.getID() + ": ");
    NamingEnumeration namingEnumeration3 = attribute.getAll();
    while (namingEnumeration3.hasMore()) {
      System.out.print(((String)namingEnumeration3.next()) + ", ");
    }
    System.out.println("");
    
  }
}
