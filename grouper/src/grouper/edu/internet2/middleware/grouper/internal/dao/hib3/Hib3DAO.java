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
import  edu.internet2.middleware.grouper.ErrorLog;
import  edu.internet2.middleware.grouper.GrouperConfig;
import  java.io.InputStream;
import  java.util.Properties;
import  org.hibernate.*;
import  org.hibernate.cfg.*;

/**
 * Base Hibernate DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3DAO.java,v 1.4 2008-03-19 20:43:24 mchyzer Exp $
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
        .addClass(Hib3AttributeDAO.class)
        .addClass(Hib3CompositeDAO.class)
        .addClass(Hib3FieldDAO.class)
        .addClass(Hib3GroupDAO.class)
        .addClass(Hib3GroupTypeDAO.class)
        .addClass(Hib3GroupTypeTupleDAO.class)
        .addClass(Hib3GrouperSessionDAO.class)
        .addClass(Hib3MemberDAO.class)
        .addClass(Hib3MembershipDAO.class)
        .addClass(Hib3RegistrySubjectDAO.class)
        .addClass(Hib3RegistrySubjectAttributeDAO.class)
        .addClass(Hib3StemDAO.class)
        ;
      // And finally create our session factory
      FACTORY = CFG.buildSessionFactory();
    } 
    catch (Throwable t) {
      String msg = "unable to initialize hibernate: " + t.getMessage();
      ErrorLog.fatal(Hib3DAO.class, msg);
      throw new ExceptionInInitializerError(t);
    }
  } // static


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static Configuration getConfiguration()
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


  // PROTECTED ABSTRACT METHODS //

  // @since   @HEAD@
  protected abstract String getId();

} 

