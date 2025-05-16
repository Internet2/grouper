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
/**
 * 
 */
package edu.internet2.middleware.grouperClient.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.logging.Log;
import edu.internet2.middleware.morphString.Crypto;

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
   * @param ldapUrl 
   * @return the context
   */
  @SuppressWarnings("unchecked")
  public static DirContext retrieveContext(String ldapUrl) {

    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    if (debugMap != null) {
      debugMap.put("method", "GrouperClientLdapUtils.retrieveContext");
    }
    
    String ldapUser = null;
    String ldapPass = null;
    try {
      // Set up the environment for creating the initial context
      Hashtable<String, String> env = new Hashtable<String, String>();
      
      //ldapUrl = GrouperClientUtils.propertiesValue("grouperClient.ldap.url", false);
      
      //see if invalid SSL
      String ldapsSocketFactoryName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.ldaps.customSocketFactory");
      
      if (!GrouperClientUtils.isBlank(ldapsSocketFactoryName)) {
        if (ldapUrl.startsWith("ldaps")) {
          Class<? extends SocketFactory> ldapsSocketFactoryClass = GrouperClientUtils.forName(ldapsSocketFactoryName);
          env.put("java.naming.ldap.factory.socket", ldapsSocketFactoryClass.getName());
        }
      }
      
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      
      
      env.put(Context.PROVIDER_URL, ldapUrl);

      if (debugMap != null) {
        debugMap.put("LDAP url", ldapUrl);
        debugMap.put("LDAP authentication type", "simple");
      }

      String userLabel = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.ldap.user.label");

      ldapUser = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.ldap." + userLabel);
  
      String ldapUserPrefix = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.ldap.user.prefix");
      String ldapUserSuffix = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.ldap.user.suffix");
      
      //put all these together
      ldapUser = ldapUserPrefix + ldapUser + ldapUserSuffix;
      
      //env.put(Context.SECURITY_PRINCIPAL, "uid=" + ldapUser + ",ou=entities,dc=upenn,dc=edu");
      if (debugMap != null) {
        debugMap.put("LDAP user", ldapUser);
      }

      env.put(Context.SECURITY_PRINCIPAL, ldapUser);
  
      boolean disableExternalFileLookup = GrouperClientConfig.retrieveConfig().propertyValueBooleanRequired(
          "encrypt.disableExternalFileLookup");
      
      //lets lookup if file
      ldapPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.ldap.password");
      String ldapPassFromFile = GrouperClientUtils.readFromFileIfFile(ldapPass, disableExternalFileLookup);

      String passPrefix = null;
      
      if (!GrouperClientUtils.equals(ldapPass, ldapPassFromFile)) {
        
        passPrefix = "LDAP pass: reading encrypted value from file: " + ldapPass;
        
        String encryptKey = GrouperClientUtils.encryptKey();
        
        ldapPass = new Crypto(encryptKey).decrypt(ldapPassFromFile);
        
      } else {
        passPrefix = "LDAP pass: reading scalar value from grouper.client.properties";
      }
      
      if (GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.logging.logMaskedPassword", false)) {
        if (debugMap != null) {
          debugMap.put("Pass", passPrefix + ": " + GrouperClientUtils.repeat("*", ldapPass.length()));
        }
      }
      
      env.put(Context.SECURITY_CREDENTIALS, ldapPass);
       
      // Create the initial context
      DirContext context = new InitialDirContext(env);
      return context;
    } catch (NamingException ne) {
      throw new RuntimeException("Problem connecting to ldap: url: " + ldapUrl + ", user: " + ldapUser, ne );
    } finally {
      if (debugMap != null) {
        LOG.debug(GrouperClientUtils.mapToString(debugMap));
      }
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
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    if (debugLog != null) {
      debugLog.put("method", "GrouperClientLdapUtils.retrieveAttributeStringValue");
    }
    try {
      if (object instanceof Attribute) {
        Attribute attribute = (Attribute)object;
        if (GrouperClientUtils.equals(attribute.getID(), attributeName)) {
          String value = (String)attribute.get();
          if (debugLog != null) {
            debugLog.put("LDAP found attribute: '" + attributeName + "' with value", "'" + value + "'");
          }
          return value;
        }
        if (debugLog != null) {
          debugLog.put("LDAP didnt find attribute: '" + attributeName + "'", "instead found attribute: '" + attribute.getID() + "'");
        }
        return null;
      } else if (object instanceof SearchResult) {
        if (debugLog != null) {
          debugLog.put("LDAP found ", "SearchResult");
        }
        SearchResult searchResult = (SearchResult)object;
        Attributes attributes = searchResult.getAttributes();
        Attribute attribute = attributes.get(attributeName);
        if (debugLog != null) {
          debugLog.put("LDAP SearchResult attributes has attribute", "'" + attributeName + "'? " + (attribute!=null));
        }
        return retrieveAttributeStringValue(attribute, attributeName);
      } else if (object instanceof NamingEnumeration<?>) {
        NamingEnumeration<?> namingEnumeration = (NamingEnumeration<?>)object;
        if (!namingEnumeration.hasMore()) {
          if (debugLog != null) {
            debugLog.put("LDAP found empty", "NamingEnumeration");
          }
          return null;
        }
        Object next = namingEnumeration.next();
        if (namingEnumeration.hasMore()) {
          if (debugLog != null) {
            debugLog.put("LDAP found more than one element in", "NamingEnumeration");
          }
          throw new RuntimeException("Expecting one result");
        }
        if (debugLog != null) {
          debugLog.put("LDAP processing NamingEnumeration of size", "one");
        }
        return retrieveAttributeStringValue(next, attributeName);
      } else {
        throw new RuntimeException("Not expecting type: " + object);
      }
    } finally {
      if (debugLog != null) {
        LOG.debug(GrouperClientUtils.mapToString(debugLog));
      }
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
    } else if (object instanceof NamingEnumeration<?>) {
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
