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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;

/**
 * Basic Hibernate <code>Group</code> and <code>GroupType</code> tuple DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateGroupTypeTupleDAO.java,v 1.5 2007-04-19 14:31:20 blair Exp $
 * @since   1.2.0
 */
public class HibernateGroupTypeTupleDAO extends HibernateDAO {
  // TODO 20070418 public until i refactor "Test_Integration_HibernateGroupDAO_delete#testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset()"

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupTypeTupleDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  groupUUID;
  private String  id;
  private String  typeUUID;


  // PUBLIC CLASS METHODS //

  // @since   1.2.0
  // TODO 20070418 public until i refactor "Test_Integration_HibernateGroupDAO_delete#testDelete_GroupTypeTuplesDeletedWhenRegistryIsReset()"
  public static HibernateGroupTypeTupleDAO findByGroupAndType(GroupDTO g, GroupTypeDTO type)
    throws  GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateGroupTypeTupleDAO as gtt where"
        + " gtt.groupUuid    = :group"
        + " and gtt.typeUuid = :type"
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByGroupAndType");
      qry.setString( "group", g.getUuid()        );
      qry.setString( "type",  type.getUuid() );
      HibernateGroupTypeTupleDAO dao = (HibernateGroupTypeTupleDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new GrouperDAOException("HibernateGroupTypeTupleDAO not found");       
      }
      return dao;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  }


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof HibernateGroupTypeTupleDAO)) {
      return false;
    }
    HibernateGroupTypeTupleDAO that = (HibernateGroupTypeTupleDAO) other;
    return new EqualsBuilder()
      .append( this.getGroupUuid(), that.getGroupUuid() )
      .append( this.getTypeUuid(),  that.getTypeUuid()  )
      .isEquals();
  }
  
  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getGroupUuid() )
      .append( this.getTypeUuid()  )
      .toHashCode();
  }
  
  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupUuid", this.getGroupUuid() )
      .append( "typeUuid",  this.getTypeUuid()  )
      .toString();
  }


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateGroupTypeTupleDAO");
  } // protected static void reset(hs)


  // GETTERS //

  protected String getGroupUuid() {
    return this.groupUUID;
  }
  public String getId() {
    return this.id;
  }
  protected String getTypeUuid() {
    return this.typeUUID;
  }


  // SETTERS //

  protected HibernateGroupTypeTupleDAO setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
    return this;
  }
  protected HibernateGroupTypeTupleDAO setId(String id) {
    this.id = id;
    return this;
  }
  protected HibernateGroupTypeTupleDAO setTypeUuid(String typeUUID) {
    this.typeUUID = typeUUID;
    return this;
  }

} 

