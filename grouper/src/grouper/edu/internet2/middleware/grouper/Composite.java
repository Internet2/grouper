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
import  java.util.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * A composite membership definition within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Composite.java,v 1.1.2.1 2006-04-17 18:19:54 blair Exp $
 *     
*/
public class Composite extends Owner implements Serializable {

  // Private Class Constants //
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(Composite.class);

  // Hibernate Properties //
  private Owner         left  = null;
  private Owner         owner = null;
  private Owner         right = null;
  private CompositeType type  = null;


  // Constructors //
  private Composite() {
    // Default constructor for Hibernate
  } // private Composite()

  protected Composite(GrouperSession s, Owner o, Owner l, Owner r, CompositeType type) 
    throws  ModelException
  {
    this.setSessionNew(   s                     ); // FIXME
    this.setCreator_id(   s.getMember()         );
    this.setCreate_time(  new Date().getTime()  );
    this.setUuid(         GrouperUuid.getUuid() );
    this.setOwner(        o                     );
    this.setLeft(         l                     );
    this.setRight(        r                     );
    this.setType(         type                  );
    CompositeValidator.validate(this);
  } // protected Composite(s, o, l, r, type)  
  

  // Public Instance Methods //
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Composite)) {
      return false;
    }
    Composite otherComposite = (Composite) other;
    return new EqualsBuilder()
      .append(this.getUuid()        , otherComposite.getUuid()       )
      .append(this.getCreator_id()  , otherComposite.getCreator_id() )
      .isEquals();
  } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getUuid()        )
      .append(this.getCreator_id()  )
      .toHashCode()
      ;
  } // public int hashCode()

  public void setModified() {
    // As composites can only be created and deleted at this time,
    // marking as modified is irrelevant. 
  } // public void setModified()

  public String toString() {
    return new ToStringBuilder(this)
      .append("owner"       , this.getOwner() )
      .append("left"        , this.getLeft()  )
      .append("right"       , this.getRight() )
      .append("type"        , this.getType()  )
      .append("uuid"        , this.getUuid()  )
      .toString();
  } // public String toString()


  // Getters //
  protected Owner getLeft() {
    return this.left;
  }
  protected Owner getOwner() {
    return this.owner;
  }
  protected Owner getRight() {
    return this.right;
  }
  protected CompositeType getType() {
    return this.type;
  }


  // Setters //
  private void setLeft(Owner l) {
    this.left = l;
  }
  private void setOwner(Owner o) {
    this.owner = o;
  }
  private void setRight(Owner r) {
    this.right = r;
  }
  private void setType(CompositeType type) {
    this.type = type;
  }

}

