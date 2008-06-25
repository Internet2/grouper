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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.ErrorLog;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.RegistrySubjectAttribute;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Base Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAO.java,v 1.6 2008-06-25 05:46:05 mchyzer Exp $
 * @since   @HEAD@
 */
abstract class Hib3DAO {

  // PRIVATE CLASS CONSTANTS //
  private static final Configuration  CFG;

  private static final SessionFactory FACTORY;

  // STATIC //
  static {
    try {
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
        .addResource(resourceNameFromClassName(Attribute.class))
        .addResource(resourceNameFromClassName(Composite.class))
        .addResource(resourceNameFromClassName(Field.class))
        .addResource(resourceNameFromClassName(Group.class))
        .addResource(resourceNameFromClassName(GroupType.class))
        .addResource(resourceNameFromClassName(GroupTypeTuple.class))
        .addResource(resourceNameFromClassName(GrouperSession.class))
        .addResource(resourceNameFromClassName(Member.class))
        .addResource(resourceNameFromClassName(Membership.class))
        .addResource(resourceNameFromClassName(RegistrySubject.class))
        .addResource(resourceNameFromClassName(RegistrySubjectAttribute.class))
        .addResource(resourceNameFromClassName(Stem.class));
      
      // And finally create our session factory
      FACTORY = CFG.buildSessionFactory();
    } 
    catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      ErrorLog.fatal(Hib3DAO.class, msg);
      throw new ExceptionInInitializerError(t);
    }
  } // static


  /**
   * class is e.g. edu.internet2.middleware.grouper.internal.dto.Attribute,
   * must return e.g. edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDAO
   * @param theClass
   * @return the string of resource
   */
  private static String resourceNameFromClassName(Class theClass) {
    String simpleName = theClass.getSimpleName();
    String daoPackage = Hib3GroupDAO.class.getPackage().getName();
    //replace with slashes
    String result = StringUtils.replace(daoPackage, ".", "/") + "/Hib3" + simpleName + "DAO.hbm.xml";
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

