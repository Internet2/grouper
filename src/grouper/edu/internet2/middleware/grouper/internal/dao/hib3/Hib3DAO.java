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
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.RegistrySubjectAttribute;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHibInitBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleHooksInitBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;

/**
 * Base Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAO.java,v 1.11 2008-07-21 18:05:44 mchyzer Exp $
 * @since   @HEAD@
 */
abstract class Hib3DAO {

  // PRIVATE CLASS CONSTANTS //
  private static final Configuration  CFG;

  private static final SessionFactory FACTORY;

  /** logger */
  private static final Log LOG = LogFactory.getLog(Hib3DAO.class);

  // STATIC //
  static {
    try {
      
      //Grouper startup hook (is this the right place?)
      GrouperHooksUtils.fireGrouperStartupHooksIfNotFiredAlready();
      
      // Find the custom configuration file
      if (Hib3DAO.class.getResource(GrouperConfig.HIBERNATE_CF) == null) {
        throw new RuntimeException("Cant find resource " + GrouperConfig.HIBERNATE_CF + ", make sure it is on the classpath.");
      }
      InputStream in  = Hib3DAO.class.getResourceAsStream(GrouperConfig.HIBERNATE_CF);  
      Properties  p   = new Properties();
      p.load(in);
      // And now load all configuration information
      CFG = new Configuration()
        .addProperties(p)
        .addResource(resourceNameFromClassName(Hib3AttributeDAO.class))
        .addResource(resourceNameFromClassName(Hib3CompositeDAO.class))
        .addResource(resourceNameFromClassName(Hib3FieldDAO.class))
        .addResource(resourceNameFromClassName(Hib3GroupDAO.class))
        .addResource(resourceNameFromClassName(Hib3GroupTypeDAO.class))
        .addResource(resourceNameFromClassName(Hib3GroupTypeTupleDAO.class))
        .addResource(resourceNameFromClassName(Hib3GrouperSessionDAO.class))
        .addResource(resourceNameFromClassName(Hib3MemberDAO.class))
        .addResource(resourceNameFromClassName(Hib3MembershipDAO.class))
        .addResource(resourceNameFromClassName(Hib3RegistrySubjectDAO.class))
        .addResource(resourceNameFromClassName(Hib3RegistrySubjectAttributeDAO.class))
        .addResource(resourceNameFromClassName(Hib3StemDAO.class))
        .addResource(resourceNameFromClassName(Hib3GrouperDdl.class))
        .addResource(resourceNameFromClassName(Hib3GrouperLoaderLog.class));
      
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_HIBERNATE_INIT, HooksLifecycleHibInitBean.class, 
          CFG, Configuration.class, null);
      
      // And finally create our session factory
      FACTORY = CFG.buildSessionFactory();
    } 
    catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      LOG.fatal(msg);
      throw new ExceptionInInitializerError(t);
    }
  } // static


  /**
   * class is e.g. edu.internet2.middleware.grouper.internal.dto.Attribute,
   * must return e.g. edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDAO
   * @param theClass
   * @return the string of resource
   */
  public static String resourceNameFromClassName(Class theClass) {
    String daoClass = theClass.getName();
    //replace with hbm
    String result = StringUtils.replace(daoClass, ".", "/") + ".hbm.xml";
    
    return result;
  }
  
  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  public static Configuration getConfiguration()
    throws  HibernateException
  {
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
    throws  HibernateException
  {
		return FACTORY.openSession();
	} 

} 

