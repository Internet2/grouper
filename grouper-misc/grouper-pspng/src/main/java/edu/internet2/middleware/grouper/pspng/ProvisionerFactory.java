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
  

  private static Map<String, Provisioner> provisioners=new ConcurrentHashMap<String, Provisioner>();

  public static Provisioner getProvisioner(String consumerName) throws PspException {
    synchronized (provisioners) {
      if ( !provisioners.containsKey(consumerName) )
      {
        Provisioner provisioner;
        try {
          provisioner = ProvisionerFactory.createProvisionerWithName(consumerName);
          provisioners.put(consumerName, provisioner);
        } catch (PspException e) {
          LOG.error("Unable to create provisioner {}", consumerName);
          throw e;
        }
      }
    }
    
    return provisioners.get(consumerName);
  }

  
  /**
   * This constructs a provisioner based on the properties found for provisioner 'name'
   * @param name
   * @return
   * @throws PspException
   */
  public static Provisioner createProvisionerWithName(String name) throws PspException {
    final String qualifiedParameterNamespace = ProvisionerConfiguration.PARAMETER_NAMESPACE + name + ".";
  
    LOG.info("Constructing provisioner: {}", name);
    String typeName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + PROVISIONER_TYPE_PROPERTY_NAME);
    
    // TODO: Someday allow types that are not java classes, either via code-based shortcuts
    // or via a lookup table/file (eg, type=ldap-groups instead of edu.internet2.....LdapGroupProvisioner)
    String className = typeName;
    try {
      Class<? extends Provisioner> provisionerClass = (Class<? extends Provisioner>) Class.forName(className);
      Method getPropertyClassMethod = provisionerClass.getMethod("getPropertyClass");
      
      Class<? extends ProvisionerConfiguration> propertyClass = (Class<? extends ProvisionerConfiguration>) getPropertyClassMethod.invoke(null);
      
      Constructor<? extends ProvisionerConfiguration> propertyConstructor = propertyClass.getConstructor(String.class);
      
      ProvisionerConfiguration properties = propertyConstructor.newInstance(name);
      
      properties.readConfiguration();
      
      Constructor<? extends Provisioner> provisionerConstructor = provisionerClass.getConstructor(String.class, propertyClass);
      Provisioner provisioner = provisionerConstructor.newInstance(name, properties);
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
