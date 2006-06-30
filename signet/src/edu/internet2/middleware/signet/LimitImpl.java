/*--
$Id: LimitImpl.java,v 1.16 2006-06-30 02:04:41 ddonn Exp $
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author acohen
 *
 */
public final class LimitImpl implements Limit
{
  // This field is a simple synthetic key for this record in the database.
  private Integer     key;

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
     int				displayOrder,
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
    this.displayOrder = displayOrder;
    this.helpText = helpText;
    this.status = status;
    this.renderer = renderer;
    this.permissions = new HashSet();
  }
  
  public void setSignet(Signet signet)
  {
    this.signet = signet;
  }
  
  Signet getSignet()
  {
    return (signet);
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
  {
    if ((this.subsystem == null)
        && (this.subsystemId != null)
        && (this.getSignet() != null))
    {
      try
      {
        subsystem = getSignet().getPersistentDB().getSubsystem(subsystemId);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
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
      subsystem = getSignet().getPersistentDB().getSubsystem(subsystemId);
    }
  }

  public ChoiceSet getChoiceSet()
  {    
    if ((choiceSet == null)
        && (choiceSetId != null)
        && (getSignet() != null))
    {
      try
      {
    	  Subsystem subsys = getSignet().getPersistentDB().getSubsystem(subsystemId);
        choiceSet = subsys.getChoiceSet(choiceSetId);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
    }

    return choiceSet;
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
  
  void setChoiceSetId(String choiceSetId) throws ObjectNotFoundException
  {
    this.choiceSetId = choiceSetId;
    
    if (getSignet() != null)
    {
      choiceSet = getSignet().getPersistentDB().getSubsystem(subsystemId).getChoiceSet(choiceSetId);
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
   * @param permission
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


  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getSelectionType()
   */
  public SelectionType getSelectionType()
  {
    // This implementation is just a little hack until we can get a proper
    // "selectionType" column into the database schema.
    
    if (this.renderer.startsWith("singleChoice"))
      return SelectionType.SINGLE;
    else if (this.renderer.startsWith("multipleChoice"))
      return SelectionType.MULTIPLE;
    else
      throw new SignetRuntimeException
        ("Limit.getSelectionType() encountered an unrecognized renderer '"
         + this.renderer
         + "', and so is unable to determine whether this Limit is"
         + " a single-select or a multiple-select. Renderer-names must begin"
         + " with either 'singleChoice' or 'multipleChoice' to be recognized.");
      
  }


  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#save()
   */
  public void save()
  {
    getSignet().getPersistentDB().save(this);
  }


  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    return
      (this.getDisplayOrder() - ((Limit)o).getDisplayOrder());
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
  public void setKey(Integer key)
  {
    this.key = key;
  }
}
