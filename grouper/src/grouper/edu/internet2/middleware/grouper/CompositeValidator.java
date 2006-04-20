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
import  org.apache.commons.logging.*;


/** 
 * @author  blair christensen.
 * @version $Id: CompositeValidator.java,v 1.1.2.2 2006-04-20 14:55:35 blair Exp $
 *     
*/
class CompositeValidator implements Serializable {

  // Protected Class Constants //
  protected static final String ERR_CL  = "left factor is owner";
  protected static final String ERR_CR  = "right factor is owner";
  protected static final String ERR_L   = "no composite left factor";
  protected static final String ERR_LC  = "composite left factor is not a group";
  protected static final String ERR_LR  = "same left and right composite factors";
  protected static final String ERR_O   = "no composite owner";
  protected static final String ERR_OC  = "invalid owner class";
  protected static final String ERR_R   = "no composite right factor";
  protected static final String ERR_RC  = "composite right factor is not a group";
  protected static final String ERR_T   = "no composite type";

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(CompositeValidator.class);

  // Protected Class Methods //
  protected static void validate(Composite c) 
    throws  ModelException
  {
    // TODO DRY
    GrouperSessionValidator.validate(c.getSession());
    Owner o = c.getOwner();
    Owner l = c.getLeft();
    Owner r = c.getRight();
    if (o == null) {
      throw new ModelException(ERR_O);
    }
    if (! ( (o instanceof Group) || (o instanceof Stem) ) ) {
      throw new ModelException(ERR_OC);
    }
    if (l == null) {
      throw new ModelException(ERR_L);
    }
    if (!(l instanceof Group)) {
      throw new ModelException(ERR_LC);
    }
    if (r == null) {
      throw new ModelException(ERR_R);
    }
    if (!(r instanceof Group)) {
      throw new ModelException(ERR_RC);
    }
    if (l.equals(r)) {
      throw new ModelException(ERR_LR);
    }
    if (o.equals(l)) {
      throw new ModelException(ERR_CL);
    }
    if (o.equals(r)) {
      throw new ModelException(ERR_CR);
    }
    if (c.getType() == null) {
      throw new ModelException(ERR_T);
    }
  } // protected static void validate(Composite c)
  
}

