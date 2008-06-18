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
import  edu.internet2.middleware.grouper.DefaultMemberOf;
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.StemNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.StemDTO;
import  java.util.Date;
import  java.util.Set;

/** 
 * Basic <code>Stem</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: StemDAO.java,v 1.8.6.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   1.2.0
 */
public interface StemDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  void createChildGroup(StemDTO _parent, GroupDTO _child, MemberDTO _m)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void createChildStem(StemDTO _parent, StemDTO _child)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void createRootStem(StemDTO _root)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(StemDTO _ns)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByApproximateExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByApproximateName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<StemDTO> findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * Find all child groups within specified scope.
   * @since   1.2.1
   */
  Set<GroupDTO> findAllChildGroups(StemDTO ns, Stem.Scope scope)
    throws  GrouperDAOException;

  /**
   * Find all child stems within specified scope.
   * @since   1.2.1
   */
  Set<StemDTO> findAllChildStems(StemDTO ns, Stem.Scope scope)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  StemDTO findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  StemDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  void renameStemAndChildren(StemDTO _ns, Set children)
    throws  GrouperDAOException;

  /** 
   * @since   1.2.0
   */
  void revokePriv(StemDTO _ns, DefaultMemberOf mof)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void revokePriv(StemDTO _ns, Set toDelete)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void update(StemDTO _ns)
    throws  GrouperDAOException;

} 

