/*--
$Id: FunctionImpl.java,v 1.23 2008-07-05 01:22:17 ddonn Exp $
$Date: 2008-07-05 01:22:17 $

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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import edu.internet2.middleware.subject.Subject;

/**
* FunctionImpl describes a group of {@link PermissionImpl}s. Each
* FunctionImpl is intended to correspond to a business-level task
* that a {@link Subject} must perform in order to accomplish some business
* operation.
* 
*/
/* Hibernate requires this class to be non-final. */

public class FunctionImpl extends EntityImpl implements Function
{
  // This field is a simple synthetic key for this record in the database.
  private Integer   key;

  private Subsystem	subsystem;
  private String	subsystemId;
  private Category  category;
  private Set		permissions;
  private String 	helpText;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public FunctionImpl()
  {
      super();
      this.permissions = new HashSet();
  }

  /**
   * Constructor
   * @param signet A Signet instance
   * @param category
   * 			The {@link Category} which contains this function.
   * 			This value is explicitly allowed to be NULL.
   * @param id
   *            A short mnemonic id which will appear in XML documents and
   *            other documents used by analysts.
   * @param name
   *            A descriptive name which will appear in UIs and documents
   *            exposed to users.
   * @param helpText
   *            A prose description which will appear in help-text and other
   *            explanatory materials.
   * @param status
   * 			The {@link Status} of this FunctionImpl.
   */
  FunctionImpl
  	(Signet			signet,
  	 Category 	category,
  	 String 		id,
  	 String 		name,
  	 String 		helpText,
  	 Status			status)
  {
      super(signet, id, name, status);
      this.subsystem = category.getSubsystem();
      this.subsystemId = this.subsystem.getId();
      this.category = category;
      this.helpText = helpText;
      this.permissions = new HashSet();
  }

  /**
   * @return Returns the category.
   */
  public Category getCategory()
  {
      return this.category;
  }
  
  /**
   * @param category The category to set.
   */
  public void setCategory(Category category)
  {
      this.category = category;
  }

  /* This method exists only for use by Hibernate. */
  void setPermissions(Set permissions)
  {
    this.permissions = permissions;
  }

  public Set getPermissions()
  {    
    return this.permissions;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#getSubsystem()
   */
  public Subsystem getSubsystem()
  {
    if (null == subsystem)
    {
        setSubsystem(signet.getPersistentDB().getSubsystem(subsystemId));
    }

	if (null != subsystem)
		((SubsystemImpl)subsystem).setSignet(signet);

    return subsystem;
  }
  
  String getSubsystemId()
  {
    return this.subsystemId;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#setSubsystem(edu.internet2.middleware.signet.Subsystem)
   */
  public void setSubsystem(Subsystem subsystem)
  {
      this.subsystem = subsystem;
      if (null == subsystem)
    	  subsystemId = null;
      else
    	  subsystemId = subsystem.getId();
  }
  
  void setSubsystemId(String subsystemId) throws ObjectNotFoundException
  {
    this.subsystemId = subsystemId;
    if (null != signet)
    {
      subsystem = signet.getPersistentDB().getSubsystem(subsystemId);
      ((SubsystemImpl)subsystem).setSignet(signet);
    }
  }

  /**
   * @param helpText A prose description which will appear in help-text and
   * 		other explanatory materials.
   */
  public void setHelpText(String helpText)
  {
      this.helpText = helpText;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#getHelpText()
   */
  public String getHelpText()
  {
      return this.helpText;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Function#addPermission(edu.internet2.middleware.signet.Permission)
   */
  public void addPermission(Permission permission)
  {
    // Do we have this Permission already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Functions to each other.
    
    if (!(this.permissions.contains(permission)))
    {
      this.permissions.add(permission);
//      permission.addFunction(this);
    }
  }
  
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.getName();
    otherName = ((Function)o).getName();
    
    return thisName.compareToIgnoreCase(otherName);
  }

//  /* (non-Javadoc)
//   * @see edu.internet2.middleware.signet.Function#getLimitsArray()
//   */
//  public Limit[] getLimitsArray()
//  {
//    Comparator displayOrderComparator
//    	= new Comparator()
//    	    {
//            public int compare(Object o1, Object o2)
//            {
//              return
//              	((Limit)o1).getDisplayOrder() - ((Limit)o2).getDisplayOrder();
//            }
//    	    };
//
//    Set limits = this.getLimits();
//    Limit[] limitArray = new Limit[0];
//    limitArray = (Limit[])(limits.toArray(limitArray));
//    	    
//    Arrays.sort(limitArray, displayOrderComparator);
//    return limitArray;
//  }
  
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Function#getLimits()
	 */
	public Set getLimits()
  {
    Set functionLimits = new HashSet();

    for (PermissionImpl permission : (Set<PermissionImpl>)permissions)
    {
      Set permissionLimits = permission.getLimits();
      
      for (LimitImpl limitImpl : (Set<LimitImpl>)permissionLimits)
      {
    	  if (null != signet)
    		  limitImpl.setSignet(signet);
      }
      
      functionLimits.addAll(permissionLimits);
    }

    return functionLimits;
  }
  
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Function#getId()
	 */
	public String getId()
  {
    return super.getStringId();
  }

  // This method is only for use by Hibernate.
  protected void setId(String id)
  {
    super.setStringId(id);
  }

  /** Hibernate "intelligently" hard-codes the xxx.id reference in HQL to map
   * to the <id> element in the mapping file (xxx.hbm.xml) regardless of
   * whether there is a <property> element that happens to have name="id".
   * Therefore, any Java class that has a property named "id" cannot be
   * accessed using HQL because the query will always reference the <id>
   * property regardless of its name. Way to go Hibernate!
   */
  public String getFuncId()
  {
	  return (this.getId());
  }

  /** Hibernate "intelligently" hard-codes the xxx.id reference in HQL to map
   * to the <id> element in the mapping file (xxx.hbm.xml) regardless of
   * whether there is a <property> element that happens to have name="id".
   * Therefore, any Java class that has a property named "id" cannot be
   * accessed using HQL because the query will always reference the <id>
   * property regardless of its name. Way to go Hibernate!
   */
  public void setFuncId(String id)
  {
	  this.setId(id);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented");
  }
  
  public Integer getKey()
  {
    return this.key;
  }


  /* This method is for use only by Hibernate.
   * 
   */
  protected void setKey(Integer key)
  {
    this.key = key;
  }


	////////////////////////////////////
	// overrides Object
	////////////////////////////////////

	/**
	 * @return A brief description of this entity. The exact details
	 * 		of the representation are unspecified and subject to change.
	 */
	public String toString()
	{
		StringBuffer outStr = new StringBuffer(super.toString());
		outStr.append(", subsystemID=");
		outStr.append((subsystemId == null ? "<<no subsystem>>" : subsystemId));
		outStr.append(", categoryID=");
		outStr.append((category == null ? "<<no category>>" : category.getId()));
		return outStr.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (!(o instanceof FunctionImpl))
		{
			return false;
		}
		FunctionImpl rhs = (FunctionImpl)o;
		return new EqualsBuilder().append(this.getSubsystemId(), rhs.getSubsystemId()).append(this.getId(), rhs.getId()).isEquals();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		// you pick a hard-coded, randomly chosen, non-zero, odd number
		// ideally different for each class
		return new HashCodeBuilder(17, 37).append(this.getSubsystemId()).append(this.getId()).toHashCode();
	}

}
