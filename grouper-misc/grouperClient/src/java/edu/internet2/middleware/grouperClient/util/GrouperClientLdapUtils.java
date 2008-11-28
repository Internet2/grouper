/**
 * 
 */
package edu.internet2.middleware.grouperClient.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import edu.internet2.middleware.grouperClient.ext.edu.internet2.middleware.morphString.Crypto;

/**
 * @author mchyzer
 *
 */
public class GrouperClientLdapUtils {

  /**
   * retrieve dircontext
   * @return the context
   */
  public static DirContext retrieveContext() {
    String ldapUrl = null;
    String ldapUser = null;
    String ldapPass = null;
    try {
      // Set up the environment for creating the initial context
      Hashtable<String, String> env = new Hashtable<String, String>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      
      Properties properties = GrouperClientUtils.propertiesFromResourceName(
          "grouper.client.properties", true, true, GrouperClientUtils.class);

      ldapUrl = GrouperClientUtils.propertiesValue(properties, "grouperClient.ldap.url");
      
      env.put(Context.PROVIDER_URL, ldapUrl);
  
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
  
      ldapUser = GrouperClientUtils.propertiesValue(properties, "grouperClient.ldap.user");
  
      String ldapUserPrefix = GrouperClientUtils.propertiesValue(properties, "grouperClient.ldap.user.prefix");
      String ldapUserSuffix = GrouperClientUtils.propertiesValue(properties, "grouperClient.ldap.user.suffix");
      
      //put all these together
      ldapUser = ldapUserPrefix + ldapUser + ldapUserSuffix;
      
      //env.put(Context.SECURITY_PRINCIPAL, "uid=" + ldapUser + ",ou=entities,dc=upenn,dc=edu");
      env.put(Context.SECURITY_PRINCIPAL, "uid=" + ldapUser + ",ou=entities,dc=upenn,dc=edu");
  
      ldapPass = GrouperClientUtils.propertiesValue(properties, "grouperClient.ldap.pass");
      
      //TODO fix this part
      ldapPass = Crypto.getThreadLocalCrypto().decrypt(ldapPass);
      
      env.put(Context.SECURITY_CREDENTIALS, ldapPass);
  
      // Create the initial context
      DirContext context = new InitialDirContext(env);
      return context;
    } catch (NamingException ne) {
      throw new RuntimeException("Problem connecting to ldap: url: " + ldapUrl + ", user: " + ldapUser );
    }

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
  public static void printNamingEnumeration(NamingEnumeration<?> namingEnumeration) throws NamingException {
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
      NamingEnumeration<?> namingEnumeration = (NamingEnumeration<?>)object;
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
      NamingEnumeration<?> namingEnumeration = attribute.getAll();
      return retrieveAttributeStringListValue(namingEnumeration, attributeName);
    } else if (object instanceof SearchResult) {
      SearchResult searchResult = (SearchResult)object;
      Attributes attributes = searchResult.getAttributes();
      Attribute attribute = attributes.get(attributeName);
      return retrieveAttributeStringListValue(attribute, attributeName);
    } else if (object instanceof NamingEnumeration) {
      int size = 0;
      NamingEnumeration<?> namingEnumeration = (NamingEnumeration<?>)object;
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
    NamingEnumeration<?> namingEnumeration3 = attribute.getAll();
    while (namingEnumeration3.hasMore()) {
      System.out.print(((String)namingEnumeration3.next()) + ", ");
    }
    System.out.println("");
    
  }
  
  
}
