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
import  java.io.Serializable;

/** 
 * @author  blair christensen.
 * @version $Id: CompositeValidator.java,v 1.3 2006-06-05 19:54:40 blair Exp $
 * @since   1.0
 */
class CompositeValidator implements Serializable {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void validate(Composite c) 
    throws  ModelException
  {
    // TODO DRY
    GrouperSessionValidator.validate(c.getSession());
    Owner o = c.getOwner();
    Owner l = c.getLeft();
    Owner r = c.getRight();
    if (o == null) {
      throw new ModelException(E.COMP_O);
    }
    if (! ( (o instanceof Group) || (o instanceof Stem) ) ) {
      throw new ModelException(E.COMP_OC);
    }
    if (l == null) {
      throw new ModelException(E.COMP_L);
    }
    if (!(l instanceof Group)) {
      throw new ModelException(E.COMP_LC);
    }
    if (r == null) {
      throw new ModelException(E.COMP_R);
    }
    if (!(r instanceof Group)) {
      throw new ModelException(E.COMP_RC);
    }
    if (l.equals(r)) {
      throw new ModelException(E.COMP_LR);
    }
    if (o.equals(l)) {
      throw new ModelException(E.COMP_CL);
    }
    if (o.equals(r)) {
      throw new ModelException(E.COMP_CR);
    }
    if (c.getType() == null) {
      throw new ModelException(E.COMP_T);
    }
  } // protected static void validate(Composite c)
  
}

