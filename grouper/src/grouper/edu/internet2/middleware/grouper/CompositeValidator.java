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

/** 
 * @author  blair christensen.
 * @version $Id: CompositeValidator.java,v 1.9 2007-02-08 16:25:25 blair Exp $
 * @since   1.0
 */
class CompositeValidator {

  // PROTECTED CLASS METHODS //

  //  @since 1.2.0
  protected static void internal_validate(CompositeDTO c) 
    throws  ModelException
  {
    GroupDTO o, l, r = null;
    if (c.getCreateTime() <= 0) {
      throw new ModelException("composite has invalid createTime");
    }
    if (c.getCreatorUuid() == null) {
      throw new ModelException("composite has null creator");
    }
    if (c.getUuid() == null) {
      throw new ModelException("composite has null uuid");
    }
    try {
      o = HibernateGroupDAO.findByUuid( c.getFactorOwnerUuid() );
    }
    catch (GroupNotFoundException eGNF) {
      throw new ModelException("invalid owner class");
    }
    try {
      l = HibernateGroupDAO.findByUuid( c.getLeftFactorUuid() );
    }
    catch (GroupNotFoundException eGNF) {
      throw new ModelException("invalid left factor class");
    }
    try {
      r = HibernateGroupDAO.findByUuid( c.getRightFactorUuid() );
    }
    catch (GroupNotFoundException eGNF) {
      throw new ModelException("invalid right factor class");
    }
    _notCyclic(c);
    if ( c.getType() == null ) {
      throw new ModelException(E.COMP_T);
    }
  } // protected static void validate(Composite c)


  // PRIVATE CLASS METHODS //  

  // @since   1.2.0
  private static void _notCyclic(CompositeDTO c) 
    throws  ModelException
  {
    if ( c.getLeftFactorUuid().equals( c.getRightFactorUuid() ) )   {
      throw new ModelException(E.COMP_LR);
    }
    if ( c.getFactorOwnerUuid().equals( c.getLeftFactorUuid() ) )   {
      throw new ModelException(E.COMP_CL);
    }
    if ( c.getFactorOwnerUuid().equals( c.getRightFactorUuid() ) )  {
      throw new ModelException(E.COMP_CR);
    }
  } // private static void _notCyclic(c)

} // class CompositeValidator

