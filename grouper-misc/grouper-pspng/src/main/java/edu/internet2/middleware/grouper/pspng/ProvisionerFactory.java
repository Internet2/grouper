package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;


/**
 * This class helps construct and cache Provisioners. 
 * 
 * 1) Because Provisioners have lots of state (caches and configurations), we don't
 * want to construct them each time the changelog sends us events. Therefore, this
 * class keeps a (static) HashMap that allows all the changelog's event batches to
 * be sent to the same Provisioner instance.
 * 
 * 2) There's a bunch of Reflection to get Provisioners and ProvisionerConfiguration
 * constructed. This class encapsulates these steps.
 * 
 * @author bert
 *
 */
public class ProvisionerFactory {
  private final static Logger LOG = LoggerFactory.getLogger(ProvisionerFactory.class);
  final static String PROVISIONER_TYPE_PROPERTY_NAME = "type";
  

  // Indexed by configName (ie, the configuration paragraph found in grouper_loader.properties
  private static Map<String, Provisioner> incrementalProvisioners=new ConcurrentHashMap<String, Provisioner>();

  private static ConcurrentMap<String, ProvisionerCoordinator> provisionerCoordinators=new ConcurrentHashMap<>();


  public static Provisioner getIncrementalProvisioner(String configName) throws PspException {
    synchronized (incrementalProvisioners) {
      if ( !incrementalProvisioners.containsKey(configName) )
      {
        Provisioner provisioner;
        try {
          provisioner = ProvisionerFactory.createProvisioner(configName, false);
          incrementalProvisioners.put(configName, provisioner);
        } catch (PspException e) {
          LOG.error("Unable to create incremental provisioner {}", configName, e);
          throw e;
        }
      }
    }
    
    return incrementalProvisioners.get(configName);
  }



  public static ProvisionerCoordinator getProvisionerCoordinator(Provisioner<?,?,?> provisioner) {
    String provisionerConfigName = provisioner.getConfigName();

    if ( !provisionerCoordinators.containsKey(provisionerConfigName) ) {
      provisionerCoordinators.putIfAbsent(provisionerConfigName, new ProvisionerCoordinator(provisioner));
    }

    return provisionerCoordinators.get(provisionerConfigName);
  }

  /**
   * This constructs a provisioner based on the properties found for provisioner 'configName'
   *
   * This should only be called internally and from FullSyncProvisionerFactory
   * @param configName
   * @param fullSyncMode
   * @return
   * @throws PspException
   */
  static Provisioner createProvisioner(String configName, boolean fullSyncMode) throws PspException {
    final String qualifiedParameterNamespace = ProvisionerConfiguration.PARAMETER_NAMESPACE + configName + ".";
  
    LOG.info("Constructing provisioner: {}", configName);
    String typeName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + PROVISIONER_TYPE_PROPERTY_NAME);
    
    // TODO: Someday allow types that are not java classes, either via code-based shortcuts
    // or via a lookup table/file (eg, type=ldap-groups instead of edu.internet2.....LdapGroupProvisioner)
    String className = typeName;
    try {
      Class<? extends Provisioner> provisionerClass = (Class<? extends Provisioner>) Class.forName(className);
      Method getPropertyClassMethod = provisionerClass.getMethod("getPropertyClass");
      
      Class<? extends ProvisionerConfiguration> propertyClass = (Class<? extends ProvisionerConfiguration>) getPropertyClassMethod.invoke(null);
      
      Constructor<? extends ProvisionerConfiguration> propertyConstructor = propertyClass.getConstructor(String.class);
      
      ProvisionerConfiguration properties = propertyConstructor.newInstance(configName);
      
      properties.readConfiguration();
      
      Constructor<? extends Provisioner> provisionerConstructor = provisionerClass.getConstructor(String.class, propertyClass, Boolean.TYPE);
      Provisioner provisioner = provisionerConstructor.newInstance(configName, properties, fullSyncMode);
      return provisioner;
    } catch (ClassNotFoundException e) {
      Provisioner.STATIC_LOG.error("Unable to find provisioner class: {}", className);
      throw new PspException("Unknown provisioner class %s", className);
    } catch (IllegalAccessException e) {
      Provisioner.STATIC_LOG.error("Problem constructing provisioner & properties: {}", className, e);
      throw new PspException("IllegalAccessException while constructing provisioner & properties: %s", className);
    } catch (IllegalArgumentException e) {
      Provisioner.STATIC_LOG.error("Problem constructing provisioner & properties: {}", className, e);
      throw new PspException("IllegalArgumentException while constructing provisioner & properties: %s", className);
    } catch (InvocationTargetException e) {
      Provisioner.STATIC_LOG.error("Problem constructing provisioner & properties: {}", className, e);
      throw new PspException("Problem while constructing provisioner & properties: %s: %s", className, e.getCause().getMessage());
    } catch (NoSuchMethodException e) {
      Provisioner.STATIC_LOG.error("Problem constructing provisioner & properties: {}", className, e);
      throw new PspException("NoSuchMethodException while constructing provisioner & properties: %s: %s", className, e.getMessage());
    } catch (SecurityException e) {
      Provisioner.STATIC_LOG.error("Problem constructing provisioner & properties: {}", className, e);
      throw new PspException("SecurityException while constructing provisioner & properties: %s: %s", className, e.getMessage());
    } catch (InstantiationException e) {
      Provisioner.STATIC_LOG.error("Problem constructing provisioner & properties: {}", className, e);
      throw new PspException("InstantiationException while constructing provisioner & properties: %s: %s", className, e.getMessage());
    }
  
    
  }
  

}
