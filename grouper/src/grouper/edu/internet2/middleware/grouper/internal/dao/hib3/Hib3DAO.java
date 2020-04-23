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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdlWorker;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHibInitBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.morphString.Morph;

/**
 * Base Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAO.java,v 1.40 2009-10-26 02:26:07 mchyzer Exp $
 * @since   @HEAD@
 */
public abstract class Hib3DAO {

  /**
   * 
   */
  private static Map<String, Configuration>  CFG = new HashMap<String, Configuration>();

  /**
   * 
   */
  private static Map<String, SessionFactory> FACTORY = new HashMap<String, SessionFactory>();

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3DAO.class);

  static {
    try {
      
      GrouperStartup.startup();
      
    } catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      LOG.fatal(msg, t);
      throw new ExceptionInInitializerError(t);
    }
  }

  /**
   * keep track of if hibernate is initted yet, allow resets... (e.g. for testing)
   */
  private static Map<String, Boolean> hibernateInittedMap = null;

  public static Map<String, Boolean> hibernateInitted() {
    if (hibernateInittedMap == null) {
      hibernateInittedMap = new HashMap<String, Boolean>();
    }
    return hibernateInittedMap;
  }

  /**
   * init hibernate if not initted
   */
  public synchronized static void initHibernateIfNotInitted() {
    initHibernateIfNotInitted("grouper");
  }

  /**
   * init hibernate if not initted
   */
  public synchronized static void initHibernateIfNotInitted(String connectionName) {
    
    if (StringUtils.isBlank(connectionName)) {
      throw new RuntimeException("connectionName is required");
    }
    
    Boolean initted = hibernateInitted().get(connectionName);
    if (initted != null && initted == Boolean.TRUE) {
      return;
    }
    
    //this might not be completely accurate
    hibernateInitted().put(connectionName, Boolean.TRUE);
    
    // Find the custom configuration file
    Properties properties = GrouperHibernateConfig.retrieveConfig().properties();
    
    if (!StringUtils.equals(connectionName, "grouper")) {
      GrouperLoaderDb grouperLoaderDb = new GrouperLoaderDb(connectionName);
      grouperLoaderDb.initProperties();
      
      if (StringUtils.isBlank(grouperLoaderDb.getUrl())) {
        throw new RuntimeException("Cant find database in grouper-loader.properties '" + connectionName + "'");
      }
      
      properties.put("hibernate.connection.url", grouperLoaderDb.getUrl());
      properties.put("hibernate.connection.username", StringUtils.defaultString(grouperLoaderDb.getUser()));
      properties.put("hibernate.connection.password", StringUtils.defaultString(grouperLoaderDb.getPass()));
      properties.put("hibernate.connection.driver_class", StringUtils.defaultString(grouperLoaderDb.getDriver()));
      properties.remove("hibernate.dialect");
    }
    initHibernateIfNotInittedHelper(connectionName, properties);
  }

  private static void initHibernateIfNotInittedHelper(String connectionName, Properties p) {
    try {
      
      //unencrypt pass
      if (p.containsKey("hibernate.connection.password")) {
        String newPass = Morph.decryptIfFile(p.getProperty("hibernate.connection.password"));
        p.setProperty("hibernate.connection.password", newPass);
      }
      
      String connectionUrl = StringUtils.defaultString(GrouperUtil.propertiesValue(p,"hibernate.connection.url"));

      {
        String dialect = StringUtils.defaultString(GrouperUtil.propertiesValue(p,"hibernate.dialect"));
        dialect = GrouperDdlUtils.convertUrlToHibernateDialectIfNeeded(connectionUrl, dialect);
        p.setProperty("hibernate.dialect", dialect);
      }
      
      {
        String driver = StringUtils.defaultString(GrouperUtil.propertiesValue(p,"hibernate.connection.driver_class"));
        driver = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(connectionUrl, driver);
        p.setProperty("hibernate.connection.driver_class", driver);
      }      
      
      // And now load all configuration information
      Configuration configuration = new Configuration().addProperties(p);
      CFG.put(connectionName, configuration);
      addClass(configuration, Hib3AttributeAssignActionDAO.class);
      addClass(configuration, Hib3AttributeAssignActionSetDAO.class);
      addClass(configuration, Hib3AttributeAssignActionSetViewDAO.class);
      addClass(configuration, Hib3AttributeAssignDAO.class);
      addClass(configuration, Hib3AttributeAssignValueDAO.class);
      addClass(configuration, Hib3AttributeDefDAO.class);
      addClass(configuration, Hib3AttributeDefNameDAO.class);
      addClass(configuration, Hib3AttributeDefNameSetDAO.class);
      addClass(configuration, Hib3AttributeDefNameSetViewDAO.class);
      addClass(configuration, Hib3AttributeDefScopeDAO.class);
      addClass(configuration, Hib3AuditEntryDAO.class);
      addClass(configuration, Hib3AuditTypeDAO.class);
      addClass(configuration, Hib3ChangeLogEntryDAO.class);
      addClass(configuration, Hib3ChangeLogEntryDAO.class, "Hib3ChangeLogEntryTempDAO");
      addClass(configuration, Hib3ChangeLogConsumerDAO.class);
      addClass(configuration, Hib3ChangeLogTypeDAO.class);
      addClass(configuration, Hib3CompositeDAO.class);
      addClass(configuration, Hib3ConfigDAO.class);
      addClass(configuration, Hib3ExternalSubjectDAO.class);
      addClass(configuration, Hib3ExternalSubjectAttributeDAO.class);
      addClass(configuration, Hib3FieldDAO.class);
      addClass(configuration, Hib3GroupDAO.class);
      addClass(configuration, GcGrouperSync.class, "Hib3GrouperSyncDAO");
      addClass(configuration, GcGrouperSyncGroup.class, "Hib3GrouperSyncGroupDAO");
      addClass(configuration, GcGrouperSyncJob.class, "Hib3GrouperSyncJobDAO");
      addClass(configuration, GcGrouperSyncLog.class, "Hib3GrouperSyncLogDAO");
      addClass(configuration, GcGrouperSyncMember.class, "Hib3GrouperSyncMemberDAO");
      addClass(configuration, GcGrouperSyncMembership.class, "Hib3GrouperSyncMembershipDAO");
      addClass(configuration, Hib3MemberDAO.class);
      addClass(configuration, Hib3MembershipDAO.class);
      addClass(configuration, Hib3MembershipDAO.class, "Hib3ImmediateMembershipDAO");
      addClass(configuration, Hib3MessageDAO.class);
      addClass(configuration, Hib3PermissionEntryDAO.class, "Hib3PermissionRoleViewDAO");
      addClass(configuration, Hib3PermissionEntryDAO.class, "Hib3PermissionRoleSubjectViewDAO");
      addClass(configuration, Hib3PermissionEntryDAO.class, "Hib3PermissionAllViewDAO");
      addClass(configuration, Hib3PermissionEntryDAO.class, "Hib3PermissionRoleAssignedViewDAO");
      addClass(configuration, Hib3RegistrySubjectDAO.class);
      addClass(configuration, Hib3RegistrySubjectAttributeDAO.class);
      addClass(configuration, Hib3RoleSetDAO.class);
      addClass(configuration, Hib3RoleSetViewDAO.class);
      addClass(configuration, Hib3StemDAO.class);
      addClass(configuration, Hib3GrouperDdl.class);
      addClass(configuration, Hib3GrouperDdlWorker.class);
      addClass(configuration, Hib3GrouperLoaderLog.class);
      addClass(configuration, Hib3GroupSetDAO.class);
      addClass(configuration, Hib3PITGroupDAO.class);
      addClass(configuration, Hib3PITStemDAO.class);
      addClass(configuration, Hib3PITAttributeDefDAO.class);
      addClass(configuration, Hib3PITMemberDAO.class);
      addClass(configuration, Hib3PITFieldDAO.class);
      addClass(configuration, Hib3PITMembershipDAO.class);
      addClass(configuration, Hib3PITGroupSetDAO.class);
      addClass(configuration, Hib3PITMembershipViewDAO.class);
      addClass(configuration, Hib3PITAttributeAssignValueDAO.class);
      addClass(configuration, Hib3PITAttributeAssignDAO.class);
      addClass(configuration, Hib3PITAttributeAssignActionDAO.class);
      addClass(configuration, Hib3PITAttributeAssignActionSetDAO.class);
      addClass(configuration, Hib3PITRoleSetDAO.class);
      addClass(configuration, Hib3PITAttributeDefNameDAO.class);
      addClass(configuration, Hib3PITAttributeDefNameSetDAO.class);
      addClass(configuration, Hib3PITPermissionAllViewDAO.class);
      addClass(configuration, Hib3PITAttributeAssignValueViewDAO.class);
      addClass(configuration, Hib3ServiceRoleViewDAO.class);
      addClass(configuration, Hib3StemSetDAO.class);
      addClass(configuration, Hib3TableIndexDAO.class);
      addClass(configuration, Hib3GrouperPasswordDAO.class);
      configuration.setInterceptor(new Hib3SessionInterceptor());
      
      //if we are testing, map these classes to the table (which may or may not exist)
      Class<?> hibernatableClass = null;
      try {
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.loader.TestgrouperLoader");
        addClass(configuration, hibernatableClass);
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      try {
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.loader.TestgrouperLoaderGroups");
        addClass(configuration, hibernatableClass);
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      try {
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.loader.TestgrouperIncrementalLoader");
        addClass(configuration, hibernatableClass);
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      try {
        
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.subj.TestgrouperSubjAttr");
        addClass(configuration, hibernatableClass);
        
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      
      try {
        
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.tableSync.TestgrouperSyncSubjectFrom");
        addClass(configuration, hibernatableClass);
        
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      
      try {
        
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.tableSync.TestgrouperSyncChangeLog");
        addClass(configuration, hibernatableClass);
        
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      
      try {
        
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.tableSync.TestgrouperSyncSubjectTo");
        addClass(configuration, hibernatableClass);
        
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_HIBERNATE_INIT, HooksLifecycleHibInitBean.class, 
          configuration, Configuration.class, null);
      
      // And finally create our session factory
      //trying to avoid warning of using the same dir
      String tmpDir = GrouperUtil.tmpDir();
      try {
        String newTmpdir = StringUtils.trimToEmpty(tmpDir);
        if (!newTmpdir.endsWith("\\") && !newTmpdir.endsWith("/")) {
          newTmpdir += File.separator;
        }
        newTmpdir += "grouper_ehcache_auto_" + GrouperUtil.uniqueId();
        System.setProperty(GrouperUtil.JAVA_IO_TMPDIR, newTmpdir);
        
        //now it should be using a unique directory
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        FACTORY.put(connectionName, sessionFactory);
      } finally {
        
        //put tmpdir back
        if (tmpDir == null) {
          System.clearProperty(GrouperUtil.JAVA_IO_TMPDIR);
        } else {
          System.setProperty(GrouperUtil.JAVA_IO_TMPDIR, tmpDir);
        }
      }

    } catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      LOG.fatal(msg, t);
      throw new RuntimeException(msg, t);
    }

  }
  
  /**
   * 
   * @param _CFG
   * @param mappedClass
   */
  private static void addClass(Configuration _CFG, Class<?> mappedClass) {
    addClass(_CFG, mappedClass, null);
  }

  /**
   * 
   * @param _CFG
   * @param mappedClass
   * @param entityNameXmlFileNameOverride send in an entity name if the entity name and xml file are different than
   * the class file.
   */
  private static void addClass(Configuration _CFG, Class<?> mappedClass, String entityNameXmlFileNameOverride) {
    String resourceName = resourceNameFromClassName(mappedClass, entityNameXmlFileNameOverride);
    String xml = GrouperUtil.readResourceIntoString(resourceName, true);
    if (xml == null) {
      // worth a shot to try the default location
      resourceName = "edu/internet2/middleware/grouper/internal/dao/hib3/" + entityNameXmlFileNameOverride + ".hbm.xml";
      xml = GrouperUtil.readResourceIntoString(resourceName, true);
      if (xml == null) {
        // go back to original error message
        resourceName = resourceNameFromClassName(mappedClass, entityNameXmlFileNameOverride);
        xml = GrouperUtil.readResourceIntoString(resourceName, false);
      }
    }
    
    if (xml.contains("<version")) {
      
      //if versioned, then make sure the setting in class is there
      String optimisiticLockVersion = "optimistic-lock=\"version\"";
      
      if (!StringUtils.contains(xml, optimisiticLockVersion)) {
        throw new RuntimeException("If there is a versioned class, it must contain " +
        		"the class level attribute: optimistic-lock=\"version\": " + mappedClass.getName() + ", " + resourceName);
      }
      
      //if versioned, then see if we are disabling
      boolean optimisiticLocking = GrouperConfig.retrieveConfig().propertyValueBoolean("dao.optimisticLocking", true);
      
      if (!optimisiticLocking) {
        xml = StringUtils.replace(xml, optimisiticLockVersion, "optimistic-lock=\"none\"");
      }
    }
    _CFG.addInputStream(IOUtils.toInputStream(xml));
    _CFG.addResource(resourceName);

  }

  /**
   * class is e.g. edu.internet2.middleware.grouper.internal.dto.Attribute,
   * must return e.g. edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDAO
   * @param theClass
   * @param entityNameXmlFileNameOverride pass in an override if the entity name and xml file are different than
   * the class file
   * @return the string of resource
   */
  public static String resourceNameFromClassName(Class theClass, String entityNameXmlFileNameOverride) {
    String daoClass = theClass.getName();
    if (!StringUtils.isBlank(entityNameXmlFileNameOverride)) {
      daoClass = StringUtils.replace(daoClass, theClass.getSimpleName(), entityNameXmlFileNameOverride);
    }
    //replace with hbm
    String result = StringUtils.replace(daoClass, ".", "/") + ".hbm.xml";
    
    return result;
  }
  
  /**
   * @return the configuration
   * @throws HibernateException
   */
  public static Configuration getConfiguration()
    throws  HibernateException {
    return getConfiguration("grouper");
  }

  /**
   * @return the configuration
   * @throws HibernateException
   */
  public static Configuration getConfiguration(String databaseName)
    throws  HibernateException {
    return CFG.get(databaseName);
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL GROUPER FRAMEWORK USE
   * ONLY.  Use the HibernateSession callback to get a hibernate Session
   * object
   * @return the session
   * @throws HibernateException
   */
  public static Session session() {
    return session("grouper");
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL GROUPER FRAMEWORK USE
   * ONLY.  Use the HibernateSession callback to get a hibernate Session
   * object
   * @return the session
   * @throws HibernateException
   */
	public static Session session(String connectionName)
    throws  HibernateException {
	  //just in case
	  initHibernateIfNotInitted(connectionName);
		return FACTORY.get(connectionName).openSession();
	} 
	
  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL GROUPER FRAMEWORK USE
   * ONLY. 
   * @return the session factor
   */
  public static SessionFactory getSessionFactory() {
    return getSessionFactory("grouper");
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL GROUPER FRAMEWORK USE
   * ONLY. 
   * @return the session factor
   */
  public static SessionFactory getSessionFactory(String connectionName) {
    //just in case
    initHibernateIfNotInitted(connectionName);
    return FACTORY.get(connectionName);
  } 

	/**
	 * evict a persistent class
	 * @param persistentClass
	 */
	public static void evict(Class persistentClass) {
	  FACTORY.get("grouper").getCache().evictEntityRegion(persistentClass);
	}
	
  /**
   * evict a persistent class
   * @param entityName
   */
  public static void evictEntity(String entityName) {
    FACTORY.get("grouper").getCache().evictEntityRegion(entityName);
  }
  
  /**
   * evict a persistent class
   * @param cacheRegion
   */
  public static void evictQueries(String cacheRegion) {
    FACTORY.get("grouper").getCache().evictQueryRegion(cacheRegion);
  }
  
} 

