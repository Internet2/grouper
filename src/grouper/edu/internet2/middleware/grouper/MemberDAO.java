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
import  edu.internet2.middleware.subject.Subject;

/** 
 * <i>Member</i> DAO interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberDAO.java,v 1.1 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
interface MemberDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public String create(MemberDTO _m) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public MemberDTO findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public MemberDTO findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public MemberDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getSubjectId();

  /**
   * @since   1.2.0
   */
  public String getSubjectSourceId();

  /**
   * @since   1.2.0
   */
  public String getSubjectTypeId();

  /**
   * @since   1.2.0
   */
  public String getUuid();

  /**
   * @since   1.2.0
   */
  public MemberDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public MemberDAO setSubjectId(String subjectID);

  /**
   * @since   1.2.0
   */
  public MemberDAO setSubjectSourceId(String subjectSourceID);

  /**
   * @since   1.2.0
   */
  public MemberDAO setSubjectTypeId(String subjectTypeID);

  /**
   * @since   1.2.0
   */
  public MemberDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  public void update(MemberDTO _m) 
    throws  GrouperDAOException;

} // interface MemberDAO extends GrouperDAO

