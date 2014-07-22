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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import javax.net.SocketFactory;

import edu.internet2.middleware.grouperClient.discovery.DiscoveryClient;
import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
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

  /** how often should we reconfigure the failover client */
  private static Integer configureEverySeconds = null;
  /** when was the failover client last configured */
  private static Long lastFailoverConfigure = null;
  /** readonly failover config name */
  public static final String LDAP_FAILOVER_CONFIG_NAME = "grouperLdap";
  /** cache this so we know if we need to reconfigure */
  public static File lastDiscoveryConfigFile = null;

  /**
   * see if needs reconfigure
   * @return true or false
   */
  private static boolean needsReconfigure() {
    boolean needsReconfigure = lastFailoverConfigure == null || (System.currentTimeMillis() - lastFailoverConfigure) / 1000 > configureEverySeconds;
    if (!DiscoveryClient.hasDiscovery() && lastFailoverConfigure != null) {
      needsReconfigure = false;
    }
    return needsReconfigure;
  }

  /**
   * configure the failover client every so often
   */
  public static void configureFailoverClient() {
  
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
  
    if (debugLog != null) {
      debugLog.put("method", "GrouperClientLdapUtils.configureFailoverClient");
    }
  
    //see if we know how often to check for new config
    if (configureEverySeconds == null) {
  
      //configure every x/5 (at least 20 seconds)
      int cacheForSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.cacheDiscoveryPropertiesForSeconds", 120);
      configureEverySeconds = cacheForSeconds / 5;
      if (configureEverySeconds < 20) {
        configureEverySeconds = 20;
      }
    }
  
    //if the amount of time since the last configure is greater than the max, then reconfigure
    boolean needsReconfigure = needsReconfigure();
  
    if (debugLog != null) {
      debugLog.put("needsReconfigure", needsReconfigure);
    }
  
    if (needsReconfigure) {
      try {
  
        synchronized (GrouperClientWs.class) {
          
          if (needsReconfigure()) {
            
            //see if the discovery file has changed...
            String fileName = "grouper.client.discovery.properties";
            String directoryName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discoveryGrouperClientPropertiesDirectory");
            if (!GrouperClientUtils.isBlank(directoryName)) {
              directoryName = GrouperClientUtils.stripLastSlashIfExists(directoryName);
              fileName = directoryName + "/" + fileName;
            }
            File discoveryFile = DiscoveryClient.retrieveFile(fileName, false);

            if (discoveryFile == null) {

              if (debugLog != null) {
                if (DiscoveryClient.hasDiscovery()) {
                  debugLog.put("discoveryFile", "not found");
                } else {
                  debugLog.put("discoveryFile", "not configured to use");
                }
              }

              //if we have reconfigured before, we dont need to do this again
              if (lastFailoverConfigure != null) {
                needsReconfigure = false;
              }
              
              if (DiscoveryClient.hasDiscovery()) {
                LOG.error("Cant find discovery file: '" + fileName + "'!!!!!!!");
              }
            } else {
  
              if (debugLog != null) {
                debugLog.put("discoveryFile", discoveryFile.getAbsolutePath());
              }
            
              //see if the same as before
              if (lastDiscoveryConfigFile != null && lastDiscoveryConfigFile.equals(discoveryFile)) {
                needsReconfigure = false;
              }
            }
            
            if (debugLog != null) {
              debugLog.put("needsReconfigureFile", needsReconfigure);
            }
            
            if (needsReconfigure) {
              
              //register the failover client
              FailoverConfig failoverConfig = new FailoverConfig();
              
              //lets get the defaults
              
              
              {
                boolean foundOne = false;
                //grouperClient.discoveryDefault.ldap.0.url = 
                List<String> ldapUrls = new ArrayList<String>();
                for (int i=0;i<100;i++) {
                  String ldapUrl = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discoveryDefault.ldap." + i + ".url");
                  if (GrouperClientUtils.isBlank(ldapUrl)) {
                    break;
                  }
                  foundOne = true;
                  if (!GrouperClientUtils.isBlank(ldapUrl)) {
                    ldapUrls.add(ldapUrl);
                  }
                }
                if (foundOne) {
                  failoverConfig.setConnectionNames(ldapUrls);
                }
              }
              
              
              //grouperClient.discoveryDefault.ldap.loadBalancing = active/active
              FailoverStrategy failoverStrategy = FailoverStrategy.valueOfIgnoreCase(
                  GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discoveryDefault.ldap.loadBalancing"), false);
              if (failoverStrategy != null) {
                failoverConfig.setFailoverStrategy(failoverStrategy);
              }
              
              //grouperClient.discoveryDefault.ldap.affinitySeconds = 28800
              int affinitySeconds = failoverConfig.getAffinitySeconds();
              affinitySeconds = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.discoveryDefault.ldap.affinitySeconds", affinitySeconds);
              failoverConfig.setAffinitySeconds(affinitySeconds);
              
              //grouperClient.discoveryDefault.ldap.lowerConnectionPriorityOnErrorForMinutes = 3
              int lowerConnectionPriorityOnErrorForMinutes = failoverConfig.getMinutesToKeepErrors();
              lowerConnectionPriorityOnErrorForMinutes = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.discoveryDefault.ldap.lowerConnectionPriorityOnErrorForMinutes", lowerConnectionPriorityOnErrorForMinutes);
              failoverConfig.setMinutesToKeepErrors(lowerConnectionPriorityOnErrorForMinutes);
              
              //grouperClient.discoveryDefault.ldap.timeoutSeconds = 30
              int timeoutSeconds = failoverConfig.getTimeoutSeconds();
              timeoutSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.discoveryDefault.ldap.timeoutSeconds", timeoutSeconds);
              failoverConfig.setTimeoutSeconds(timeoutSeconds);
              
              //grouperClient.discoveryDefault.ldap.extraTimeoutSeconds = 15
              int extraTimeoutSeconds = failoverConfig.getExtraTimeoutSeconds();
              extraTimeoutSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.discoveryDefault.ldap.extraTimeoutSeconds", extraTimeoutSeconds);
              failoverConfig.setExtraTimeoutSeconds(extraTimeoutSeconds);
              
              //if there is a discovery file, then use it
              if (discoveryFile != null) {
                Properties properties = GrouperClientUtils.propertiesFromFile(discoveryFile);
                
                {
                  boolean foundOne = false;
                  //grouperClient.discovery.ldap.0.url = 
                  List<String> ldapUrls = new ArrayList<String>();
                  for (int i=0;i<100;i++) {
                    String ldapUrl = GrouperClientUtils.propertiesValue(properties, "grouperClient.discovery.ldap." + i + ".url");
                    if (GrouperClientUtils.isBlank(ldapUrl)) {
                      break;
                    }
                    foundOne = true;
                    if (!GrouperClientUtils.isBlank(ldapUrl)) {
                      ldapUrls.add(ldapUrl);
                    }
                  }
                  if (foundOne) {
                    failoverConfig.setConnectionNames(ldapUrls);
                  }
                }
                
                //grouperClient.discovery.ldap.loadBalancing = active/active
                failoverStrategy = FailoverStrategy.valueOfIgnoreCase(
                    GrouperClientUtils.propertiesValue(properties, "grouperClient.discovery.ldap.loadBalancing"), false);
                if (failoverStrategy != null) {
                  failoverConfig.setFailoverStrategy(failoverStrategy);
                }
                
                //grouperClient.discovery.ldap.affinitySeconds = 600
                affinitySeconds = GrouperClientUtils.propertiesValueInt(properties, null, "grouperClient.discovery.ldap.affinitySeconds", affinitySeconds);
                failoverConfig.setAffinitySeconds(affinitySeconds);
                
                //grouperClient.discovery.ldap.lowerConnectionPriorityOnErrorForMinutes = 3
                lowerConnectionPriorityOnErrorForMinutes = GrouperClientUtils.propertiesValueInt(properties, 
                    null, "grouperClient.discovery.ldap.lowerConnectionPriorityOnErrorForMinutes", lowerConnectionPriorityOnErrorForMinutes);
                failoverConfig.setMinutesToKeepErrors(lowerConnectionPriorityOnErrorForMinutes);
                
                //grouperClient.discovery.ldap.timeoutSeconds = 30
                timeoutSeconds = GrouperClientUtils.propertiesValueInt(properties, null, "grouperClient.discovery.ldap.timeoutSeconds", timeoutSeconds);
                failoverConfig.setTimeoutSeconds(timeoutSeconds);
                
                //grouperClient.discovery.ldap.extraTimeoutSeconds = 15
                extraTimeoutSeconds = GrouperClientUtils.propertiesValueInt(properties, null, "grouperClient.discovery.ldap.extraTimeoutSeconds", extraTimeoutSeconds);
                failoverConfig.setExtraTimeoutSeconds(extraTimeoutSeconds);
                
              }
  
              {
                boolean foundOne = false;
                //#grouperClient.discoveryOverride.ldap.0.url = 
                List<String> ldapUrls = new ArrayList<String>();
                for (int i=0;i<100;i++) {
                  String ldapUrl = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discoveryOverride.ldap." + i + ".url");
                  if (GrouperClientUtils.isBlank(ldapUrl)) {
                    break;
                  }
                  foundOne = true;
                  if (!GrouperClientUtils.isBlank(ldapUrl)) {
                    ldapUrls.add(ldapUrl);
                  }
                }
                if (foundOne) {
                  failoverConfig.setConnectionNames(ldapUrls);
                }
              }
              
              //#grouperClient.discoveryOverride.ldap.loadBalancing = active/active
              failoverStrategy = FailoverStrategy.valueOfIgnoreCase(
                  GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.discoveryOverride.ldap.loadBalancing"), false);
              if (failoverStrategy != null) {
                failoverConfig.setFailoverStrategy(failoverStrategy);
              }
              
              //#grouperClient.discoveryOverride.ldap.affinitySeconds = 28800
              affinitySeconds = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.discoveryOverride.ldap.affinitySeconds", affinitySeconds);
              failoverConfig.setAffinitySeconds(affinitySeconds);
              
              //#grouperClient.discoveryOverride.ldap.lowerConnectionPriorityOnErrorForMinutes = 3
              lowerConnectionPriorityOnErrorForMinutes = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.discoveryOverride.ldap.lowerConnectionPriorityOnErrorForMinutes", lowerConnectionPriorityOnErrorForMinutes);
              failoverConfig.setMinutesToKeepErrors(lowerConnectionPriorityOnErrorForMinutes);
              
              //#grouperClient.discoveryOverride.ldap.timeoutSeconds = 30
              timeoutSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt(
                  "grouperClient.discoveryOverride.ldap.timeoutSeconds", timeoutSeconds);
              failoverConfig.setTimeoutSeconds(timeoutSeconds);
              
              //#grouperClient.discoveryOverride.ldap.extraTimeoutSeconds = 15
              extraTimeoutSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt(
                  "grouperClient.discoveryOverride.ldap.extraTimeoutSeconds", extraTimeoutSeconds);
              failoverConfig.setExtraTimeoutSeconds(extraTimeoutSeconds);
  
              if (debugLog != null) {
                int i=0;
                for (String ldapUrl : GrouperClientUtils.nonNull(failoverConfig.getConnectionNames())) {
                  debugLog.put("ldapUrl." + i, ldapUrl);
                  i++;
                }
                debugLog.put("affinitySeconds", failoverConfig.getAffinitySeconds());
                debugLog.put("extraTimeoutSeconds", failoverConfig.getExtraTimeoutSeconds());
                debugLog.put("errorsForMinutes", failoverConfig.getMinutesToKeepErrors());
                debugLog.put("failoverStrategy", failoverConfig.getFailoverStrategy());
                debugLog.put("timeoutSeconds", failoverConfig.getTimeoutSeconds());
              }
  
              
              //if there are no urls, then add the default one
              if (GrouperClientUtils.length(failoverConfig.getConnectionNames()) == 0) {
                failoverConfig.setConnectionNames(GrouperClientUtils.toList(
                    GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.ldap.url")));
              }
              failoverConfig.setConnectionType(LDAP_FAILOVER_CONFIG_NAME);
              FailoverClient.initFailoverClient(failoverConfig);
              
            }
          }
        }
      } finally {
        if (debugLog != null) {
          LOG.debug(GrouperClientUtils.mapToString(debugLog));
        }
      }
    }
  }
  
  
}
