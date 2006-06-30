/*--
 $Id: PermissionImpl.java,v 1.16 2006-06-30 02:04:41 ddonn Exp $
 $Date: 2006-06-30 02:04:41 $
 
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
import java.util.Set;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import edu.internet2.middleware.subject.Subject;

/**
 * PermissionImpl describes an application-level action that a {@link Subject}
 * may be allowed to perform.
 * 
 */
/* Hibernate requires this class to be non-final. */

public class PermissionImpl extends EntityImpl
		implements Permission, Comparable
{
  // This field is a simple synthetic key for this record in the database.
  private Integer     key;

  private Subsystem	subsystem;
  private Set				functions;
  private Set				limits;
  
  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public PermissionImpl()
  {
    super();
    this.functions = new HashSet();
    this.limits = new HashSet();
  }
  
  /**
   * @param id
   *            A short mnemonic id which will appear in XML documents and
   *            other documents used by analysts.
   */
  PermissionImpl
    (SubsystemImpl 	subsystem,
     String 				id,
     Status					status)
  {
    super(subsystem.getSignet(), id, null, status);
    this.setSubsystem(subsystem);
    this.functions = new HashSet();
    this.limits = new HashSet();
  }
  
  /* This method exists only for use by Hibernate. */
  void setFunctions(Set functions)
  {
    this.functions = functions;
  }

  public Set getFunctions()
  {
    return this.functions;
  }
  
  /* This method exists only for use by Hibernate. */
  void setLimits(Set limits)
  {
    this.limits = limits;
  }

  public Set getLimits()
  {
    return this.limits;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Permission#addFunction(edu.internet2.middleware.signet.Function)
   */
  public void addFunction(Function function)
  {
    // Do we have this Function already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Functions to each other.
    
    if (!(this.functions.contains(function)))
    {
      this.functions.add(function);
      ((FunctionImpl)function).addPermission(this);
    }
  }

  public void addLimit(Limit limit)
  {
    // Do we have this Limit already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Limits to each other.
    
    if (!(this.limits.contains(limit)))
    {
      this.limits.add(limit);
      ((LimitImpl)limit).add(this);
    }
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#getSubsystem()
   */
  public Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#setSubsystem(edu.internet2.middleware.signet.Subsystem)
   */
  public void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
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

  public boolean equals(Object obj)
  {
    if ( !(obj instanceof PermissionImpl) )
    {
      return false;
    }
    
    PermissionImpl rhs = (PermissionImpl) obj;
    return new EqualsBuilder()
      .append(this.getId(), rhs.getId())
      .isEquals();
  }
  
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37)
      .append(this.getId())
      .toHashCode();
  }
  
  /* This method is for use only by Hibernate.
   * 
   */
  private Integer getKey()
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
  
  public int compareTo(Object o)
  {
    PermissionImpl rhs = (PermissionImpl) o;
    return new CompareToBuilder()
      // .appendSuper(super.compareTo(o)
      .append(this.getId(), rhs.getId())
      .toComparison();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented");
  }
}
