/*--
$Id: ChoiceImpl.java,v 1.3 2005-03-01 20:42:49 acohen Exp $
$Date: 2005-03-01 20:42:49 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFoundException;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ChoiceImpl implements Choice
{
  private Signet		signet;
  private ChoiceSet choiceSet;
  private String		choiceSetId;
  private int		 		displayOrder;
  private String 		displayValue;
  private int		 		rank;
  private String 		value;
  
  /* The date and time this Choice was last modified. */
  private Date	modifyDatetime;
  
  /**
   * Every Hibernate-persistable entity must have a default,
   * parameterless constructor.
   */
  ChoiceImpl()
  {
    super();
  }
  
  /**
   * @param displayOrder The displayOrder to set.
   */
  void setDisplayOrder(int displayOrder)
  {
    this.displayOrder = displayOrder;
  }
  
  /**
   * @param displayValue The displayValue to set.
   */
  void setDisplayValue(String displayValue)
  {
    this.displayValue = displayValue;
  }
  
  /**
   * @param rank The rank to set.
   */
  void setRank(int rank)
  {
    this.rank = rank;
  }
  
  /**
   * @param id
   * @param value
   * @param displayValue
   * @param displayOrder
   * @param rank
   */
  ChoiceImpl
  	(Signet			signet,
  	 ChoiceSet	choiceSet,
  	 String 		value,
     String 		displayValue,
     int 				displayOrder,
     int 				rank)
  {
    super();
    this.choiceSet = choiceSet;
    this.choiceSetId = choiceSet.getId();
    this.displayOrder = displayOrder;
    this.displayValue = displayValue;
    this.rank = rank;
    this.value = value;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.Choice#getChoiceSet()
   */
  public ChoiceSet getChoiceSet()
  throws ChoiceSetNotFoundException
  {
    if ((this.choiceSet == null) && (this.choiceSetId != null)
        && (this.getSignet() != null))
    {
      try
      {
        this.choiceSet
        	= (ChoiceSetImpl)
        			(this.getSignet().getChoiceSet(this.choiceSetId));
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new ChoiceSetNotFoundException(onfe);
      }
    }

    return this.choiceSet;
  }

  /**
   * @param choiceSet The ChoiceSet to set.
   */
  void setChoiceSet(ChoiceSet choiceSet)
  {
    this.choiceSet = (ChoiceSetImpl) choiceSet;
    this.choiceSetId = choiceSet.getId();
  }

  String getChoiceSetId()
  {
    return this.choiceSetId;
  }

  void setChoiceSetId(String choiceSetId)
  throws ObjectNotFoundException
  {
    this.choiceSetId = choiceSetId;

    if (this.getSignet() != null)
    {
      this.choiceSet
      	= (ChoiceSetImpl)
      			(this.getSignet().getChoiceSet(choiceSetId));
    }
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.Choice#getValue()
   */
  public String getValue()
  {
    return this.value;
  }
  
  /**
   * @param value The value to set.
   */
  void setValue(String value)
  {
    this.value = value;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.Choice#getLabel()
   */
  public String getDisplayValue()
  {
    return this.displayValue;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.Choice#getDisplayOrder()
   */
  public int getDisplayOrder()
  {
    return this.displayOrder;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.Choice#getRank()
   */
  public int getRank()
  {
    return this.rank;
  }

  /* This method exists only for use by Hibernate.
   */
  public ChoiceFullyQualifiedId getFullyQualifiedId()
  {
    return new ChoiceFullyQualifiedId
    	(this.getChoiceSetId(), this.getValue());
  }

  /*
   * This method exists only for use by Hibernate.
   */
  void setFullyQualifiedId(ChoiceFullyQualifiedId cfqId)
      throws ObjectNotFoundException
  {
    this.choiceSetId = cfqId.getChoiceSetId();
    this.value = cfqId.getChoiceValue();

    if (this.getSignet() != null)
    {
      this.choiceSet
      	= (ChoiceSetImpl)
      			(this.getSignet().getChoiceSet(cfqId.getChoiceSetId()));
    }
  }

  /**
   * @return Returns the signet.
   */
  Signet getSignet()
  {
    return this.signet;
  }
  /**
   * @param signet The signet to set.
   */
  void setSignet(Signet signet)
  {
    this.signet = signet;
  }
  
  /**
   * @return Returns the modifyDatetime.
   */
  Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  /**
   * @param modifyDatetime The modifyDatetime to set.
   */
  void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof ChoiceImpl) )
    {
      return false;
    }
    
    ChoiceImpl rhs = (ChoiceImpl) obj;
    return new EqualsBuilder()
    	.append(this.displayOrder, rhs.displayOrder)
    	.append(this.rank, rank)
      .append(this.displayValue, rhs.displayValue)
      .append(this.value, rhs.value)
      .isEquals();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37)
  		.append(this.displayOrder)
  		.append(this.rank)
  		.append(this.displayValue)
  		.append(this.value)
      .toHashCode();
  }
}
