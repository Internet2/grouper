/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.dao;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;

/** 
 * Basic <code>RegistrySubject</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: RegistrySubjectDAO.java,v 1.4 2007-05-31 17:57:45 blair Exp $
 * @since   1.2.0
 */
public interface RegistrySubjectDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  String create(RegistrySubjectDTO _subj)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(RegistrySubjectDTO _subj)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  RegistrySubjectDAO find(String id, String type) 
    throws  GrouperDAOException,
            SubjectNotFoundException;

  /**
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  String getName();

  /**
   * @since   1.2.0
   */
  String getType();

  /**
   * @since   1.2.0
   */
  RegistrySubjectDAO setId(String id);

  /**
   * @since   1.2.0
   */
  RegistrySubjectDAO setName(String name);

  /**
   * @since   1.2.0
   */
  RegistrySubjectDAO setType(String type);

} 

