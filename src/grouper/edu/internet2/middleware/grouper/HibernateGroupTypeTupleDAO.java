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

package edu.internet2.middleware.grouper;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;

/**
 * Stub Hibernate {@link Group} and {@link GroupType} tuple DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGroupTypeTupleDAO.java,v 1.3 2007-03-16 19:46:17 blair Exp $
 * @since   1.2.0
 */
class HibernateGroupTypeTupleDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupTypeTupleDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  groupUUID;
  private String  id;
  private String  typeUUID;


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
  } // public boolean equals(other)
  
  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getGroupUuid() )
      .append( this.getTypeUuid()  )
      .toHashCode();
  } // public int hashCode()
  
  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupUuid", this.getGroupUuid() )
      .append( "typeUuid",  this.getTypeUuid()  )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static HibernateGroupTypeTupleDAO findByGroupAndType(GroupDTO g, GroupTypeDTO type)
    throws  GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateGroupTypeTupleDAO as gtt where"
        + " gtt.groupUuid  = :group"
        + " and gtt.typeUuid   = :type"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByGroupAndType");
      qry.setString( "group", g.getUuid()        );
      qry.setString( "type",  type.getTypeUuid() );
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
  } // protected static HibernateGroupTypeTupleDAO findByGroupAndType(g, type)

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
  protected String getId() {
    return this.id;
  }
  protected String getTypeUuid() {
    return this.typeUUID;
  }


  // SETTERS //

  protected void setGroupUuid(String groupUUID) {
    this.groupUUID = groupUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setTypeUuid(String typeUUID) {
    this.typeUUID = typeUUID;
  }

} // class HibernateGroupTypeTupleDAO extends HibernateDAO 

