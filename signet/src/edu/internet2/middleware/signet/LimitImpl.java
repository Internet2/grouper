/*--
$Id: LimitImpl.java,v 1.8 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class LimitImpl implements Limit
{
  private Signet			signet;
  private Subsystem		subsystem;
  private String			subsystemId;
  private String			id;
  private DataType		dataType;
  private ChoiceSet		choiceSet;
  private String			choiceSetId;
  private String			name;
  private String			helpText;
  private Date				modifyDatetime;
  private Status			status;
  private String			renderer;
  private Set		  		permissions;
  private int					displayOrder;
  
  private final String			limitType="reserved";

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public LimitImpl()
  {
      super();
      this.permissions = new HashSet();
  }

  
  LimitImpl
    (Signet			signet,
     Subsystem	subsystem,
     String 		id,
     DataType		dataType,
     ChoiceSet	choiceSet,
     String 		name,
     String 		helpText,
     Status			status,
     String			renderer)
  {
    super();
    this.setSignet(signet);
    this.subsystem = subsystem;
    this.subsystemId = this.subsystem.getId();
    this.id = id;
    this.dataType = dataType;
    this.choiceSet = choiceSet;
    this.choiceSetId = this.choiceSet.getId();
    this.name = name;
    this.helpText = helpText;
    this.status = status;
    this.renderer = renderer;
    this.permissions = new HashSet();
  }
  
  void setSignet(Signet signet)
  {
    this.signet = signet;
  }
  
  Signet getSignet()
  {
    return this.signet;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getLimitId()
   */
  public String getId()
  {
    return this.id;
  }
  
  void setId(String id)
  {
    this.id = id;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getName()
   */
  public String getName()
  {
    return this.name;
  }
  
  void setName(String name)
  {
    this.name = name;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getHelpText()
   */
  public String getHelpText()
  {
    return this.helpText;
  }
  
  void setHelpText(String helpText)
  {
    this.helpText = helpText;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.SubsystemPart#getSubsystem()
   */
  public Subsystem getSubsystem()
  throws ObjectNotFoundException
  {
    if ((this.subsystem == null)
        && (this.subsystemId != null)
        && (this.getSignet() != null))
    {
      this.subsystem = this.getSignet().getSubsystem(this.subsystemId);
    }
    
    return this.subsystem;
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
      this.subsystemId = subsystem.getId();
  }
  
  void setSubsystemId(String subsystemId)
  throws ObjectNotFoundException
  {
    this.subsystemId = subsystemId;
    
    if (this.getSignet() != null)
    {
      this.subsystem = this.getSignet().getSubsystem(subsystemId);
    }
  }

  public ChoiceSet getChoiceSet()
  throws ObjectNotFoundException
  {
    if ((this.choiceSet == null)
        && (this.choiceSetId != null)
        && (this.getSignet() != null))
    {
      this.choiceSet
      	= this.getSignet()
      			.getSubsystem(this.subsystemId)
      				.getChoiceSet(this.choiceSetId);
    }

    return this.choiceSet;
  }
  
  String getChoiceSetId()
  {
    return this.choiceSetId;
  }

  public void setChoiceSet(ChoiceSet choiceSet)
  {
      this.choiceSet = choiceSet;
      this.choiceSetId = choiceSet.getId();
  }
  
  void setChoiceSetId(String choiceSetId)
  throws ObjectNotFoundException
  {
    this.choiceSetId = choiceSetId;
    
    if (this.getSignet() != null)
    {
      this.choiceSet
      	= this.getSignet()
      			.getSubsystem(this.subsystemId)
      				.getChoiceSet(choiceSetId);
    }
  }


  /* This method exists only for use by Hibernate.
   */
  public LimitFullyQualifiedId getFullyQualifiedId()
  {
    return new LimitFullyQualifiedId
    	(this.getSubsystemId(), this.getId());
  }
  
  /*
   * This method exists only for use by Hibernate.
   */
  void setFullyQualifiedId(LimitFullyQualifiedId lfqId)
  throws ObjectNotFoundException
  {
    this.subsystemId = lfqId.getSubsystemId();
    this.setId(lfqId.getLimitId());
    
    if (this.getSignet() != null)
    {
      this.subsystem
      	= this.getSignet().getSubsystem(lfqId.getSubsystemId());
    }
  }
  
  void setValueType(String valueType)
  {
    // This method is not yet implemented, and exists only to
    // satisfy Hibernate's mapping to the not-yet-used (but not-null)
    // column in the database.
  }
  
  String getValueType()
  {
    // This method is not yet implemented, and exists only to
    // satisfy Hibernate's mapping to the not-yet-used (but not-null)
    // column in the database.
    return "reserved";
  }
  
  /**
   * @return Returns the date and time this entity was last modified.
   */
  final Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  /**
   * @param modifyDatetime The modifyDatetime to set.
   */
  final void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
  
  /**
   * @return Returns the status.
   */
  public final Status getStatus()
  {
    return status;
  }
  
  /**
   * @param status The status to set.
   */
  public final void setStatus(Status status)
  {
    this.status = status;
  }
  
  void setRenderer(String renderer)
  {
      this.renderer = renderer;
  }
  
  public String getRenderer()
  {
      return this.renderer;
  }
  
  /**
   * @return Returns the dataType.
   */
  public DataType getDataType()
  {
    return this.dataType;
  }
  
  /**
   * @param dataType The dataType to set.
   */
  void setDataType(DataType valueType)
  {
    this.dataType = valueType;
  }
  
  /**
   * @return Returns the limitType.
   */
  String getLimitType()
  {
    return this.limitType;
  }
  
  /**
   * @param limitType The limitType to set.
   */
  void setLimitType(String limitType)
  {
    // This method does nothing, and is just a place-holder.
  }
  
  /**
   * @return Returns the permissions.
   */
  Set getPermissions() {
    return this.permissions;
  }
  
  /**
   * @param permissions The permissions to set.
   */
  void setPermissions(Set permissions) {
    this.permissions = permissions;
  }


  /**
   * @param impl
   */
  public void add(Permission permission)
  {
    // Do we have this Permission already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Limits to each other.
    
    if (!(this.permissions.contains(permission)))
    {
      this.permissions.add(permission);
      permission.addLimit(this);
    }
  }

  public int getDisplayOrder()
  {
    return this.displayOrder;
  }

  void setDisplayOrder(int displayOrder)
  {
    this.displayOrder = displayOrder;
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    // TODO Auto-generated method stub
    return "[id='" + this.getId() + "]";
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof LimitImpl))
    {
      return false;
    }

    LimitImpl rhs = (LimitImpl) obj;
    return new EqualsBuilder().append(this.getId(), rhs.getId()).isEquals();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).append(this.getId()).toHashCode();
  }
}
