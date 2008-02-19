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
import java.util.Set;

import  edu.internet2.middleware.grouper.MemberNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.subject.Source;
import  edu.internet2.middleware.subject.Subject;

/** 
 * Basic <code>Member</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: MemberDAO.java,v 1.4 2008-02-19 22:13:10 tzeller Exp $
 * @since   1.2.0
 */
public interface MemberDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  String create(MemberDTO _m) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.3.0
   */
  Set findAll() 
    throws  GrouperDAOException;
  
  /**
   * @since   1.3.0
   */
  Set findAll(Source source) 
    throws  GrouperDAOException;
  
  /**
   * @since   1.2.0
   */
  MemberDTO findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  MemberDTO findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  MemberDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  String getSubjectId();

  /**
   * @since   1.2.0
   */
  String getSubjectSourceId();

  /**
   * @since   1.2.0
   */
  String getSubjectTypeId();

  /**
   * @since   1.2.0
   */
  String getUuid();

  /**
   * @since   1.2.0
   */
  MemberDAO setId(String id);

  /**
   * @since   1.2.0
   */
  MemberDAO setSubjectId(String subjectID);

  /**
   * @since   1.2.0
   */
  MemberDAO setSubjectSourceId(String subjectSourceID);

  /**
   * @since   1.2.0
   */
  MemberDAO setSubjectTypeId(String subjectTypeID);

  /**
   * @since   1.2.0
   */
  MemberDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  void update(MemberDTO _m) 
    throws  GrouperDAOException;

} 

