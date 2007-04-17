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

package edu.internet2.middleware.grouper.internal.util;
import  edu.internet2.middleware.grouper.Field;       // FIXME 20070417 no!
import  edu.internet2.middleware.grouper.Group;       // FIXME 20070417 no!
import  edu.internet2.middleware.grouper.GrouperAPI;  // FIXME 20070417 no!
import  edu.internet2.middleware.grouper.GroupType;   // FIXME 20070417 no!
import  edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import  edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import  edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GrouperDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  edu.internet2.middleware.grouper.internal.dto.BaseGrouperDTO;
import  java.util.Collection;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

/** 
 * Translate between API objects, DAO objects and DTO objects.
 * <p/>
 * @author  blair christensen.
 * @since   1.2.0
 * @version $Id: Rosetta.java,v 1.2 2007-04-17 18:45:13 blair Exp $
 */
public class Rosetta {
  // FIXME 20070416 visibility! - including methods!

  // PUBLIC CLASS METHODS //

  /**
   * @since   1.2.0
   */
  public static GrouperDAO getDAO(GrouperAPI api) {
    return getDAO( api.getDTO() );
  }

  /**
   * @since   1.2.0
   */
  public static GrouperDAO getDAO(BaseGrouperDTO dto) {
    return dto.getDAO();
  }

  /**
   * @since   1.2.0
   */
  public static GrouperDAO getDAO(Object obj) {
    if      (obj instanceof BaseGrouperDTO) {
      return getDAO( (BaseGrouperDTO) obj );
    }
    else if (obj instanceof GrouperAPI)     {
      return getDAO( ( (GrouperAPI) obj ).getDTO() );
    }
    throw new IllegalArgumentException( "cannot translate obj to dao: " + obj.getClass().getName() );
  }

  /**
   * @since   1.2.0
   */
  public static Collection getDTO(Collection c) {
    Set       dtos  = new LinkedHashSet();
    Iterator  it    = c.iterator();
    while (it.hasNext()) {
      dtos.add( getDTO( it.next() ) );
    }
    return dtos;
  } // public static Collection getDTO(c)

  /**
   * @since   1.2.0
   */
  public static GrouperDTO getDTO(GrouperAPI api) {
    return api.getDTO();
  } // public static GrouperDTO getDTO(api)

  /**
   * @since   1.2.0
   */
  public static GrouperDTO getDTO(GrouperDAO dao) {
    // TODO 20070416 ???
    if      (dao instanceof FieldDAO)      {
      return FieldDTO.getDTO( (FieldDAO) dao );
    }
    else if (dao instanceof GroupTypeDAO)  {
      return GroupTypeDTO.getDTO( (GroupTypeDAO) dao );
    }
    throw new IllegalArgumentException( "cannot translate dao to dto: " + dao.getClass().getName() );
  } // public static GrouperDTO getDTO(obj)

  /**
   * @since   1.2.0
   */
  public static GrouperDTO getDTO(Object obj) {
    if      (obj instanceof GrouperAPI) {
      return getDTO( (GrouperAPI) obj );
    }
    else if (obj instanceof GrouperDTO) { // TODO 20070314 this is redundant 
      return (GrouperDTO) obj;
    }
    else if (obj instanceof GrouperDAO) {
      return getDTO( (GrouperDAO) obj );
    }
    throw new IllegalArgumentException( "cannot translate obj to dto: " + obj.getClass().getName() );
  } 

} 

