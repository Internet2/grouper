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
import  java.util.Collection;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

/** 
 * Translate between API objects, DAO objects and DTO objects.
 * <p/>
 * @author  blair christensen.
 * @since   1.2.0
 * @version $Id: Rosetta.java,v 1.2 2007-02-14 17:34:14 blair Exp $
 */
class Rosetta {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Collection getAPI(Collection c) {
    Set       apis  = new LinkedHashSet();
    Iterator  it    = c.iterator();
    while (it.hasNext()) {
      apis.add( getAPI( it.next() ) );
    }
    return apis;
  } // protected static Collection getAPI(c)

  // @since   1.2.0
  protected static GrouperAPI getAPI(BaseGrouperDTO dto) {
    if      (dto instanceof FieldDTO)     {
      Field f = new Field();
      f.setDTO(dto);
      return (Field) f;
    }
    else if (dto instanceof GroupDTO)     {
      Group g = new Group();
      g.setDTO(dto);
      return (Group) g;
    }
    else if (dto instanceof GroupTypeDTO) {
      GroupType type = new GroupType();
      type.setDTO(dto);
      return (GroupType) type;
    }
    throw new IllegalArgumentException( "cannot translate dto to api: " + dto.getClass().getName() );
  } // protected static GrouperAPI getAPI(dto)

  // @since   1.2.0
  protected static GrouperAPI getAPI(HibernateDAO dao) 
    throws  IllegalArgumentException
  {
    // TODO 20070206 do i even need to check?
    if      (dao instanceof HibernateFieldDAO)     {
      Field f = new Field();
      f.setDTO( FieldDTO.getDTO( (HibernateFieldDAO) dao ) );
      return f;
    }
    else if (dao instanceof HibernateGroupTypeDAO) {
      GroupType t = new GroupType();
      t.setDTO( GroupTypeDTO.getDTO( (HibernateGroupTypeDAO) dao ) );
      return t;
    }
    throw new IllegalArgumentException( "cannot translate dao to api: " + dao.getClass().getName() );
  } // protected static GrouperAPI getAPI(dao)

  // @since   1.2.0
  protected static GrouperAPI getAPI(Object obj) {
    if      (obj instanceof BaseGrouperDTO)  {
      return getAPI( (BaseGrouperDTO) obj );
    }
    else if (obj instanceof HibernateDAO)    {
      return getAPI( (HibernateDAO) obj );  
    }
    throw new IllegalArgumentException( "cannot translate obj to api: " + obj.getClass().getName() );
  } // protected static GrouperAPI getAPI(obj)

  // @since   1.2.0
  protected static HibernateDAO getDAO(GrouperAPI api) {
    return getDAO( api.getDTO() );
  } // protected static HibernateDAO getDAO(dto)

  // @since   1.2.0
  protected static HibernateDAO getDAO(BaseGrouperDTO dto) {
    return dto.getDAO();
  } // protected static HibernateDAO getDAO(dto)

  // @since   1.2.0
  protected static HibernateDAO getDAO(Object obj) {
    if      (obj instanceof BaseGrouperDTO) {
      return getDAO( (BaseGrouperDTO) obj );
    }
    else if (obj instanceof GrouperAPI)     {
      return getDAO( ( (GrouperAPI) obj ).getDTO() );
    }
    throw new IllegalArgumentException( "cannot translate obj to dao: " + obj.getClass().getName() );
  } // protected static HibernateDAO getDAO(obj)

  // @since   1.2.0
  protected static Collection getDTO(Collection c) {
    Set       dtos  = new LinkedHashSet();
    Iterator  it    = c.iterator();
    while (it.hasNext()) {
      dtos.add( getDTO( it.next() ) );
    }
    return dtos;
  } // protected static Collection getDTO(c)

  // @since   1.2.0
  protected static GrouperDTO getDTO(GrouperAPI api) {
    return api.getDTO();
  } // protected static GrouperDTO getDTO(api)

  // @since   1.2.0
  protected static GrouperDTO getDTO(HibernateDAO dao) {
    // TODO 20070206 do i even need to check?
    if      (dao instanceof HibernateFieldDAO)      {
      return FieldDTO.getDTO( (HibernateFieldDAO) dao );
    }
    else if (dao instanceof HibernateGroupTypeDAO)  {
      return GroupTypeDTO.getDTO( (HibernateGroupTypeDAO) dao );
    }
    else if (dao instanceof HibernateSettingsDAO)   {
      return SettingsDTO.getDTO( (HibernateSettingsDAO) dao );
    }
    throw new IllegalArgumentException( "cannot translate dao to dto: " + dao.getClass().getName() );
  } // protected static GrouperDTO getDTO(obj)

  // @since   1.2.0
  protected static GrouperDTO getDTO(Object obj) {
    if      (obj instanceof GrouperAPI)   {
      return getDTO( (GrouperAPI) obj );
    }
    else if (obj instanceof GrouperDTO)   { // TODO 20070212 this is redundant 
      return (GrouperDTO) obj;
    }
    else if (obj instanceof HibernateDAO) {
      return getDTO( (HibernateDAO) obj );
    }
    throw new IllegalArgumentException( "cannot translate obj to dto: " + obj.getClass().getName() );
  } // protected static GrouperDTO getDTO(obj)

} // class Rosetta

