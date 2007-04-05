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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.SubjectNotFoundException;

/** 
 * <i>RegistrySubject</i> DAO interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RegistrySubjectDAO.java,v 1.3 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
interface RegistrySubjectDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  RegistrySubjectDAO find(String id, String type) 
    throws  GrouperDAOException,
            SubjectNotFoundException;

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getName();

  /**
   * @since   1.2.0
   */
  public String getType();

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDAO setName(String name);

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDAO setType(String type);

} // interface RegistrySubjectDAO extends GrouperDAO

