/*--
$Id: CategoryImpl.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
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
  private Subsystem 	subsystem;
  private Set					functions;

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
  	(Subsystem 	subsystem,
  	 String 		id,
  	 String 		name,
  	 Status 		status)
  {
      super(id, name, status);
      this.subsystem = subsystem;
      this.functions = new HashSet();
  }

  /**
   * @return Returns the functions.
   */
  public Function[] getFunctionsArray()
  {
    Function[] functionsArray;
    
    if (this.functions == null)
    {
      functionsArray = new Function[0];
    }
    else
    {
      functionsArray = new Function[this.functions.size()];
      Iterator functionsIterator = this.functions.iterator();
      int i = 0;
      while (functionsIterator.hasNext())
      {
        functionsArray[i] = (Function)(functionsIterator.next());
        i++;
      }
    }
    
    return functionsArray;
  }

  /**
   * @param functions The functions to set.
   */
  public void setFunctionsArray(Function[] functions)
  {
    int functionCount = (functions == null ? 0 : functions.length);
    this.functions = new HashSet(functionCount);
      
    for (int i = 0; i < functionCount; i++)
    {
      this.functions.add(functions[i]);
    }
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
}