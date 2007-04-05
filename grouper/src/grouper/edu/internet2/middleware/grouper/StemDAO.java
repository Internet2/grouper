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
import  java.util.Date;
import  java.util.Set;

/** 
 * <i>Stem</i> DAO interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemDAO.java,v 1.1 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
interface StemDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public String createChildGroup(StemDTO _parent, GroupDTO _child, MemberDTO _m)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public String createChildStem(StemDTO _parent, StemDTO _child)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public String createRootStem(StemDTO _root)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllChildGroups(Stem ns)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllChildStems(Stem ns)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public StemDTO findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public StemDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public String getCreateSource();

  /**
   * @since   1.2.0
   */
  public long getCreateTime();

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  public String getDescription();

  /**
   * @since   1.2.0
   */
  public String getDisplayExtension();

  /**
   * @since   1.2.0
   */
  public String getDisplayName();

  /**
   * @since   1.2.0
   */
  public String getExtension();

  /** 
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getModifierUuid();
  
  /**
   * @since   1.2.0
   */
  public String getModifySource();

  /**
   * @since   1.2.0
   */
  public long getModifyTime();

  /**
   * @since   1.2.0
   */
  public String getName();

  /**
   * @since   1.2.0
   */
  public String getParentUuid();

  /**
   * @since   1.2.0
   */
  public String getUuid();

  /**
   * @since   1.2.0
   */
  public void renameStemAndChildren(Stem ns, Set children)
    throws  GrouperDAOException;

  /** 
   * TODO 20070403 expect this to change.
   * <p/>
   * @since   1.2.0
   */
  public void revokePriv(StemDTO _ns, MemberOf mof)
    throws  GrouperDAOException;

  /**
   * TODO 20070403 expect this to change.
   * <p/>
   * @since   1.2.0
   */
  public void revokePriv(StemDTO _ns, Set toDelete)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public StemDAO setCreateSource(String createSource);

  /**
   * @since   1.2.0
   */
  public StemDAO setCreateTime(long createTime);
  
  /**
   * @since   1.2.0
   */
  public StemDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  public StemDAO setDescription(String description);

  /**
   * @since   1.2.0
   */
  public StemDAO setDisplayExtension(String displayExtension);

  /**
   * @since   1.2.0
   */
  public StemDAO setDisplayName(String displayName);

  /**
   * @since   1.2.0
   */
  public StemDAO setExtension(String extension);

  /**
   * @since   1.2.0
   */
  public StemDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public StemDAO setModifierUuid(String modifierUUID);

  /**
   * @since   1.2.0
   */
  public StemDAO setModifySource(String modifySource);

  /**
   * @since   1.2.0
   */
  public StemDAO setModifyTime(long modifyTime);

  /**
   * @since   1.2.0
   */
  public StemDAO setName(String name);

  /**
   * @since   1.2.0
   */
  public StemDAO setParentUuid(String parentUUID);

  /**
   * @since   1.2.0
   */
  public StemDAO setUuid(String uuid);

} // interface StemDAO extends GrouperDAO

