/*--
$Id: CategoryImpl.java,v 1.9 2006-02-09 10:18:22 lmcrae Exp $
$Date: 2006-02-09 10:18:22 $

Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.Subject;

/**
* CategoryImpl organizes a group of {@link Function}s.
* Each {@link Function} is intended to correspond to a business-level task
* that a {@link Subject} must perform in order to accomplish some business
* operation.
* 
*/
/* Hibernate requires this class to be non-final. */

class CategoryImpl
extends EntityImpl
implements Category
{
  // This field is a simple synthetic key for this record in the database.
  private Integer   key;

  private Subsystem subsystem;
  private Set       functions;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public CategoryImpl()
  {
      super();
      this.functions = new HashSet();
  }

  /**
   * @param subsystem
   * 			The {@link Subsystem} which contains this function.
   * @param id
   *            A short mnemonic id which will appear in XML documents and
   *            other documents used by analysts.
   * @param name
   *            A descriptive name which will appear in UIs and documents
   *            exposed to users.
   * @param description
   *            A prose description which will appear in help-text and other
   *            explanatory materials.
   * @param status
   * 			The {@link Status} of this CategoryImpl.
   */
  CategoryImpl
  	(SubsystemImpl 	subsystem,
  	 String 				id,
  	 String 				name,
  	 Status 				status)
  {
      super(subsystem.getSignet(), id, name, status);
      this.subsystem = subsystem;
      this.functions = new HashSet();
  }
  
  /* This method exists only for use by Hibernate. */
  void setFunctions(Set functions)
  {
    this.functions = functions;
  }

  /**
   * TODO - Hibernate requires that getters and setters for collections
   * return the EXACT SAME collection, not just an identical one. Failure
   * to do this makes Hibernate think that the collection has been modified,
   * and causes the entire collection to be re-persisted in the database.
   * 
   * I need to find some way to tell Hibernate to use a specific non-public
   * getter, so that the public getter can resume returning a non-modifiable
   * copy of the collection. 
   */
  public Set getFunctions()
  {
    return this.functions;
    // return UnmodifiableSet.decorate(this.functions);
  }


  /* This method exists only for use by Hibernate.
   */  
  void add(Function function)
  {
    this.functions.add(function);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#getSubsystem()
   */
  public Subsystem getSubsystem()
  {
      return this.subsystem;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o)
  {
    if ( !(o instanceof CategoryImpl) )
    {
      return false;
    }
    
    CategoryImpl rhs = (CategoryImpl) o;
    return new EqualsBuilder()
                    .append(this.getId(), rhs.getId())
                    .isEquals();
  }

  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */   
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).   
       append(this.getId()).
       toHashCode();
   }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#setSubsystem(edu.internet2.middleware.signet.Subsystem)
   */
  public void setSubsystem(Subsystem subsystem)
  {
      this.subsystem = subsystem;
  }
  
  /**
   * @return A brief description of this entity. The exact details
   * 		of the representation are unspecified and subject to change.
   */
  public String toString()
  {
      StringBuffer outStr = new StringBuffer(super.toString());
      
      outStr.append(", subsystemID=");
      outStr.append
      	((subsystem == null? "<<no subsystem>>" : subsystem.getId()));
      
      return outStr.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.getName();
    otherName = ((Category)o).getName();
    
    return thisName.compareToIgnoreCase(otherName);
  }
  
  public String getId()
  {
    return super.getStringId();
  }
  
  // This method is only for use by Hibernate.
  private void setId(String id)
  {
    super.setStringId(id);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented");
  }
  
  Integer getKey()
  {
    return this.key;
  }

  /* This method is for use only by Hibernate.
   * 
   */
  private void setKey(Integer key)
  {
    this.key = key;
  }
}
