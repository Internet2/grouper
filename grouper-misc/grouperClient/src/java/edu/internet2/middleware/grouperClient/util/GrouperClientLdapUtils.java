/**
 * 
 */
package edu.internet2.middleware.grouperClient.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.net.SocketFactory;

import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * @author mchyzer
 *
 */
public class GrouperClientLdapUtils {

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperClientLdapUtils.class);

  /**
   * retrieve dircontext
   * @return the context
   */
  @SuppressWarnings("unchecked")
  public static DirContext retrieveContext() {
    String ldapUrl = null;
    String ldapUser = null;
    String ldapPass = null;
    try {
      // Set up the environment for creating the initial context
      Hashtable<String, String> env = new Hashtable<String, String>();
      
      ldapUrl = GrouperClientUtils.propertiesValue("grouperClient.ldap.url", true);

      //see if invalid SSL
      String ldapsSocketFactoryName = GrouperClientUtils.propertiesValue("grouperClient.ldaps.customSocketFactory", false);
      
      if (!GrouperClientUtils.isBlank(ldapsSocketFactoryName)) {
        if (ldapUrl.startsWith("ldaps")) {
          Class<? extends SocketFactory> ldapsSocketFactoryClass = GrouperClientUtils.forName(ldapsSocketFactoryName);
          env.put("java.naming.ldap.factory.socket", ldapsSocketFactoryClass.getName());
        }
      }
      
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      
      
      env.put(Context.PROVIDER_URL, ldapUrl);

      LOG.debug("LDAP url: " + ldapUrl);
      
      LOG.debug("LDAP authentication type: " + "simple");

      String userLabel = GrouperClientUtils.propertiesValue("grouperClient.ldap.user.label", true);

      ldapUser = GrouperClientUtils.propertiesValue("grouperClient.ldap." + userLabel, true);
  
      String ldapUserPrefix = GrouperClientUtils.propertiesValue("grouperClient.ldap.user.prefix", true);
      String ldapUserSuffix = GrouperClientUtils.propertiesValue("grouperClient.ldap.user.suffix", true);
      
      //put all these together
      ldapUser = ldapUserPrefix + ldapUser + ldapUserSuffix;
      
      //env.put(Context.SECURITY_PRINCIPAL, "uid=" + ldapUser + ",ou=entities,dc=upenn,dc=edu");
      LOG.debug("LDAP user: " + ldapUser);
      env.put(Context.SECURITY_PRINCIPAL, ldapUser);
  
      boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
          "encrypt.disableExternalFileLookup", false, true);
      
      //lets lookup if file
      ldapPass = GrouperClientUtils.propertiesValue("grouperClient.ldap.password", true);
      String ldapPassFromFile = GrouperClientUtils.readFromFileIfFile(ldapPass, disableExternalFileLookup);

      String passPrefix = null;
      
      if (!GrouperClientUtils.equals(ldapPass, ldapPassFromFile)) {
        
        passPrefix = "LDAP pass: reading encrypted value from file: " + ldapPass;
        
        String encryptKey = GrouperClientUtils.encryptKey();
        
        ldapPass = new Crypto(encryptKey).decrypt(ldapPassFromFile);
        
      } else {
        passPrefix = "LDAP pass: reading scalar value from grouper.client.properties";
      }
      
      if (GrouperClientUtils.propertiesValueBoolean("grouperClient.logging.logMaskedPassword", false, false)) {
        LOG.debug(passPrefix + ": " + GrouperClientUtils.repeat("*", ldapPass.length()));
      }
      
      env.put(Context.SECURITY_CREDENTIALS, ldapPass);
       
      // Create the initial context
      DirContext context = new InitialDirContext(env);
      return context;
    } catch (NamingException ne) {
      throw new RuntimeException("Problem connecting to ldap: url: " + ldapUrl + ", user: " + ldapUser, ne );
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
        String value = (String)attribute.get();
        LOG.debug("LDAP found attribute: '" + attributeName + "' with value: '" + value + "'");
        return value;
      }
      LOG.debug("LDAP didnt find attribute: '" + attributeName + "' instead found attribute: '" + attribute.getID() + "'");
      return null;
    } else if (object instanceof SearchResult) {
      LOG.debug("LDAP found SearchResult");
      SearchResult searchResult = (SearchResult)object;
      Attributes attributes = searchResult.getAttributes();
      Attribute attribute = attributes.get(attributeName);
      LOG.debug("LDAP SearchResult attributes has attribute: '" + attributeName + "'? " + (attribute!=null));
      return retrieveAttributeStringValue(attribute, attributeName);
    } else if (object instanceof NamingEnumeration) {
      NamingEnumeration<?> namingEnumeration = (NamingEnumeration<?>)object;
      if (!namingEnumeration.hasMore()) {
        LOG.debug("LDAP found empty NamingEnumeration");
        return null;
      }
      Object next = namingEnumeration.next();
      if (namingEnumeration.hasMore()) {
        LOG.debug("LDAP found more than one element in NamingEnumeration");
        throw new RuntimeException("Expecting one result");
      }
      LOG.debug("LDAP processing NamingEnumeration of size one");
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
      LOG.debug("LDAP object is null looking for list of string attributes: '" + attributeName + "'");
      return null;
    }
    if (object instanceof Attribute) {
      LOG.debug("LDAP found attribute: '" + attributeName + "'");
      Attribute attribute = (Attribute)object;
      NamingEnumeration<?> namingEnumeration = attribute.getAll();
      return retrieveAttributeStringListValue(namingEnumeration, attributeName);
    } else if (object instanceof SearchResult) {
      SearchResult searchResult = (SearchResult)object;
      Attributes attributes = searchResult.getAttributes();
      Attribute attribute = attributes.get(attributeName);
      LOG.debug("LDAP found SearchResult for attribute: '" + attributeName + "', found attribute? " + (attribute!= null));
      return retrieveAttributeStringListValue(attribute, attributeName);
    } else if (object instanceof NamingEnumeration) {
      LOG.debug("LDAP found NamingEnumeration for attribute: '" + attributeName + "'");
      int size = 0;
      NamingEnumeration<?> namingEnumeration = (NamingEnumeration<?>)object;
      List<String> resultList = new ArrayList<String>();
      while (namingEnumeration.hasMore()) {
        Object next = namingEnumeration.next();
        if (next instanceof SearchResult) {
          if (size == 0 && !namingEnumeration.hasMore()) {
            LOG.debug("LDAP found SearchResult in NamingEnumeration for attribute: '" + attributeName + "'");
            return retrieveAttributeStringListValue(next, attributeName);
          }
          LOG.debug("LDAP found multiple SearchResults in NamingEnumeration for attribute: '" + attributeName + "'");
          throw new RuntimeException("Error: multiple search results found!");
        }
        resultList.add((String)next);
        size++;
      }
      if (size == 0) {
        LOG.debug("LDAP did not found SearchResult in NamingEnumeration for attribute: '" + attributeName + "'");
        return null;
      }
      return resultList;
    } else {
      throw new RuntimeException("Not expecting type: " + object.getClass() + ", " + object);
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
