/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * @version $Id: CompositeValidator.java,v 1.6 2006-09-06 19:50:21 blair Exp $
 * @since   1.0
 */
class CompositeValidator {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void validate(Composite c) 
    throws  ModelException
  {
    GrouperSessionValidator.validate(c.getSession());
    Owner o = c.getOwner();
    Owner l = c.getLeft();
    Owner r = c.getRight();
    _notNull(o, E.COMP_O);
    _rightOwnerClass(o);
    _notNull(l, E.COMP_L);
    _rightFactorClass(l, E.COMP_LC);
    _notNull(r, E.COMP_R);
    _rightFactorClass(r, E.COMP_RC);
    _notCyclic(o, l, r);
    _notNull(c.getType(), E.COMP_T);
  } // protected static void validate(Composite c)


  // PRIVATE CLASS METHODS //  

  // @since 1.0
  private static void _notCyclic(Owner o, Owner l, Owner r)
    throws  ModelException
  {
    if (l.equals(r)) {
      throw new ModelException(E.COMP_LR);
    }
    if (o.equals(l)) {
      throw new ModelException(E.COMP_CL);
    }
    if (o.equals(r)) {
      throw new ModelException(E.COMP_CR);
    }
  } // private static void _notCyclic(o, l, r)

  // @since 1.0
  private static void _notNull(Object obj, String msg) 
    throws  ModelException
  {
    if (obj == null) {
      throw new ModelException(msg);
    }
  } // private static void _notNull(obj, msg)

  // @since 1.0
  private static void _rightFactorClass(Owner f, String msg)
    throws  ModelException
  {
    if (!(f instanceof Group)) {
      throw new ModelException(msg);
    }
  } // private static void _rightFactorClass(f, msg)

  // @since 1.0
  private static void _rightOwnerClass(Owner o) 
    throws  ModelException
  {
    if ( !( (o instanceof Group) || (o instanceof Stem) ) ) {
      throw new ModelException(E.COMP_OC);
    }
  } // private static void _rightOwnerClass(o)

}

