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
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: StemDAO.java,v 1.7 2007-08-02 19:25:15 blair Exp $
 * @since   1.2.0
 */
public interface StemDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  String createChildGroup(StemDTO _parent, GroupDTO _child, MemberDTO _m)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  String createChildStem(StemDTO _parent, StemDTO _child)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  String createRootStem(StemDTO _root)
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
  Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByApproximateExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByApproximateName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * Find all child groups within specified scope.
   * @since   @HEAD@
   */
  Set<GroupDTO> findAllChildGroups(StemDTO ns, Stem.Scope scope)
    throws  GrouperDAOException;

  /**
   * Find all child stems within specified scope.
   * @since   @HEAD@
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
  String getCreateSource();

  /**
   * @since   1.2.0
   */
  long getCreateTime();

  /**
   * @since   1.2.0
   */
  String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  String getDescription();

  /**
   * @since   1.2.0
   */
  String getDisplayExtension();

  /**
   * @since   1.2.0
   */
  String getDisplayName();

  /**
   * @since   1.2.0
   */
  String getExtension();

  /** 
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  String getModifierUuid();
  
  /**
   * @since   1.2.0
   */
  String getModifySource();

  /**
   * @since   1.2.0
   */
  long getModifyTime();

  /**
   * @since   1.2.0
   */
  String getName();

  /**
   * @since   1.2.0
   */
  String getParentUuid();

  /**
   * @since   1.2.0
   */
  String getUuid();

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
  StemDAO setCreateSource(String createSource);

  /**
   * @since   1.2.0
   */
  StemDAO setCreateTime(long createTime);
  
  /**
   * @since   1.2.0
   */
  StemDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  StemDAO setDescription(String description);

  /**
   * @since   1.2.0
   */
  StemDAO setDisplayExtension(String displayExtension);

  /**
   * @since   1.2.0
   */
  StemDAO setDisplayName(String displayName);

  /**
   * @since   1.2.0
   */
  StemDAO setExtension(String extension);

  /**
   * @since   1.2.0
   */
  StemDAO setId(String id);

  /**
   * @since   1.2.0
   */
  StemDAO setModifierUuid(String modifierUUID);

  /**
   * @since   1.2.0
   */
  StemDAO setModifySource(String modifySource);

  /**
   * @since   1.2.0
   */
  StemDAO setModifyTime(long modifyTime);

  /**
   * @since   1.2.0
   */
  StemDAO setName(String name);

  /**
   * @since   1.2.0
   */
  StemDAO setParentUuid(String parentUUID);

  /**
   * @since   1.2.0
   */
  StemDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  void update(StemDTO _ns)
    throws  GrouperDAOException;

} 

