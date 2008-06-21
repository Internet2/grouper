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

import edu.internet2.middleware.grouper.ErrorLog;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dto.AttributeDTO;
import edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeTupleDTO;
import edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import edu.internet2.middleware.grouper.internal.dto.RegistrySubjectAttributeDTO;
import edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import edu.internet2.middleware.grouper.internal.dto.StemDTO;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Base Hibernate DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3DAO.java,v 1.5 2008-06-21 04:16:12 mchyzer Exp $
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
        .addResource(resourceNameFromClassName(AttributeDTO.class))
        .addResource(resourceNameFromClassName(CompositeDTO.class))
        .addResource(resourceNameFromClassName(FieldDTO.class))
        .addResource(resourceNameFromClassName(GroupDTO.class))
        .addResource(resourceNameFromClassName(GroupTypeDTO.class))
        .addResource(resourceNameFromClassName(GroupTypeTupleDTO.class))
        .addResource(resourceNameFromClassName(GrouperSessionDTO.class))
        .addResource(resourceNameFromClassName(MemberDTO.class))
        .addResource(resourceNameFromClassName(MembershipDTO.class))
        .addResource(resourceNameFromClassName(RegistrySubjectDTO.class))
        .addResource(resourceNameFromClassName(RegistrySubjectAttributeDTO.class))
        .addResource(resourceNameFromClassName(StemDTO.class));
      
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
   * class is e.g. edu.internet2.middleware.grouper.internal.dto.AttributeDTO,
   * must return e.g. edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeDAO
   * @param theClass
   * @return the string of resource
   */
  private static String resourceNameFromClassName(Class theClass) {
    String simpleName = theClass.getSimpleName();
    //get before DTO
    String beforeDto = GrouperUtil.prefixOrSuffix(simpleName, "DTO", true);
    String daoPackage = Hib3GroupDAO.class.getPackage().getName();
    //replace with slashes
    String result = StringUtils.replace(daoPackage, ".", "/") + "/Hib3" + beforeDto + "DAO.hbm.xml";
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

