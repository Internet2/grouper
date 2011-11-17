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
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHibInitBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
  private static Configuration  CFG;

  /**
   * 
   */
  private static SessionFactory FACTORY;

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
  public static boolean hibernateInitted = false;
  
  /**
   * init hibernate if not initted
   */
  public synchronized static void initHibernateIfNotInitted() {
    if (hibernateInitted) {
      return;
    }
    
    //this might not be completely accurate
    hibernateInitted = true;

    try {
      // Find the custom configuration file
      Properties  p   = GrouperUtil.propertiesFromResourceName(GrouperConfig.HIBERNATE_CF);
      
      //unencrypt pass
      if (p.containsKey("hibernate.connection.password")) {
        String newPass = Morph.decryptIfFile(p.getProperty("hibernate.connection.password"));
        p.setProperty("hibernate.connection.password", newPass);
      }
      
      // And now load all configuration information
      CFG = new Configuration()
        .addProperties(p);
      addClass(CFG, Hib3AttributeAssignActionDAO.class);
      addClass(CFG, Hib3AttributeAssignActionSetDAO.class);
      addClass(CFG, Hib3AttributeAssignActionSetViewDAO.class);
      addClass(CFG, Hib3AttributeAssignDAO.class);
      addClass(CFG, Hib3AttributeAssignValueDAO.class);
      addClass(CFG, Hib3AttributeDAO.class);
      addClass(CFG, Hib3AttributeDefDAO.class);
      addClass(CFG, Hib3AttributeDefNameDAO.class);
      addClass(CFG, Hib3AttributeDefNameSetDAO.class);
      addClass(CFG, Hib3AttributeDefNameSetViewDAO.class);
      addClass(CFG, Hib3AttributeDefScopeDAO.class);
      addClass(CFG, Hib3AuditEntryDAO.class);
      addClass(CFG, Hib3AuditTypeDAO.class);
      addClass(CFG, Hib3ChangeLogEntryDAO.class);
      addClass(CFG, Hib3ChangeLogEntryDAO.class, "Hib3ChangeLogEntryTempDAO");
      addClass(CFG, Hib3ChangeLogConsumerDAO.class);
      addClass(CFG, Hib3ChangeLogTypeDAO.class);
      addClass(CFG, Hib3CompositeDAO.class);
      addClass(CFG, Hib3ExternalSubjectDAO.class);
      addClass(CFG, Hib3ExternalSubjectAttributeDAO.class);
      addClass(CFG, Hib3FieldDAO.class);
      addClass(CFG, Hib3GroupDAO.class);
      addClass(CFG, Hib3GroupTypeDAO.class);
      addClass(CFG, Hib3GroupTypeTupleDAO.class);
      addClass(CFG, Hib3MemberDAO.class);
      addClass(CFG, Hib3MembershipDAO.class);
      addClass(CFG, Hib3MembershipDAO.class, "Hib3ImmediateMembershipDAO");
      addClass(CFG, Hib3PermissionEntryDAO.class, "Hib3PermissionRoleViewDAO");
      addClass(CFG, Hib3PermissionEntryDAO.class, "Hib3PermissionRoleSubjectViewDAO");
      addClass(CFG, Hib3PermissionEntryDAO.class, "Hib3PermissionAllViewDAO");
      addClass(CFG, Hib3PermissionEntryDAO.class, "Hib3PermissionRoleAssignedViewDAO");
      addClass(CFG, Hib3RegistrySubjectDAO.class);
      addClass(CFG, Hib3RegistrySubjectAttributeDAO.class);
      addClass(CFG, Hib3RoleSetDAO.class);
      addClass(CFG, Hib3RoleSetViewDAO.class);
      addClass(CFG, Hib3StemDAO.class);
      addClass(CFG, Hib3GrouperDdl.class);
      addClass(CFG, Hib3GrouperLoaderLog.class);
      addClass(CFG, Hib3GroupSetDAO.class);
      addClass(CFG, Hib3PITGroupDAO.class);
      addClass(CFG, Hib3PITStemDAO.class);
      addClass(CFG, Hib3PITAttributeDefDAO.class);
      addClass(CFG, Hib3PITMemberDAO.class);
      addClass(CFG, Hib3PITFieldDAO.class);
      addClass(CFG, Hib3PITMembershipDAO.class);
      addClass(CFG, Hib3PITGroupSetDAO.class);
      addClass(CFG, Hib3PITMembershipViewDAO.class);
      addClass(CFG, Hib3PITAttributeAssignValueDAO.class);
      addClass(CFG, Hib3PITAttributeAssignDAO.class);
      addClass(CFG, Hib3PITAttributeAssignActionDAO.class);
      addClass(CFG, Hib3PITAttributeAssignActionSetDAO.class);
      addClass(CFG, Hib3PITRoleSetDAO.class);
      addClass(CFG, Hib3PITAttributeDefNameDAO.class);
      addClass(CFG, Hib3PITAttributeDefNameSetDAO.class);
      addClass(CFG, Hib3PITPermissionAllViewDAO.class);
      addClass(CFG, Hib3PITAttributeAssignValueViewDAO.class);
      CFG.setInterceptor(new Hib3SessionInterceptor());
      
      //if we are testing, map these classes to the table (which may or may not exist)
      try {
        Class<?> hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.loader.TestgrouperLoader");
        addClass(CFG, hibernatableClass);
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.app.loader.TestgrouperLoaderGroups");
        addClass(CFG, hibernatableClass);
        
        hibernatableClass = Class.forName("edu.internet2.middleware.grouper.subj.TestgrouperSubjAttr");
        addClass(CFG, hibernatableClass);
        
      } catch (ClassNotFoundException cnfe) {
        //this is ok
      }
      
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_HIBERNATE_INIT, HooksLifecycleHibInitBean.class, 
          CFG, Configuration.class, null);
      
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
        FACTORY = CFG.buildSessionFactory();
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
    String xml = GrouperUtil.readResourceIntoString(resourceName, false);
    
    if (xml.contains("<version")) {
      
      //if versioned, then make sure the setting in class is there
      String optimisiticLockVersion = "optimistic-lock=\"version\"";
      
      if (!StringUtils.contains(xml, optimisiticLockVersion)) {
        throw new RuntimeException("If there is a versioned class, it must contain " +
        		"the class level attribute: optimistic-lock=\"version\": " + mappedClass.getName() + ", " + resourceName);
      }
      
      //if versioned, then see if we are disabling
      boolean optimisiticLocking = GrouperConfig.getPropertyBoolean("dao.optimisticLocking", true);
      
      if (!optimisiticLocking) {
        xml = StringUtils.replace(xml, optimisiticLockVersion, "optimistic-lock=\"none\"");
      }
    }
    _CFG.addXML(xml);

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
    return CFG;
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL GROUPER FRAMEWORK USE
   * ONLY.  Use the HibernateSession callback to get a hibernate Session
   * object
   * @return the session
   * @throws HibernateException
   */
	public static Session session()
    throws  HibernateException {
	  //just in case
	  initHibernateIfNotInitted();
		return FACTORY.openSession();
	} 

	/**
	 * evict a persistent class
	 * @param persistentClass
	 */
	public static void evict(Class persistentClass) {
	  FACTORY.evict(persistentClass);
	}
	
  /**
   * evict a persistent class
   * @param entityName
   */
  public static void evictEntity(String entityName) {
    FACTORY.evictEntity(entityName);
  }
  
  /**
   * evict a persistent class
   * @param cacheRegion
   */
  public static void evictQueries(String cacheRegion) {
    FACTORY.evictQueries(cacheRegion);
  }
  
} 

