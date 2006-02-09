/*--
$Id: ChoiceImpl.java,v 1.9 2006-02-09 10:18:40 lmcrae Exp $
$Date: 2006-02-09 10:18:40 $

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFound;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ChoiceImpl implements Choice
{
  // This field is a simple synthetic key for this record in the database.
  private Integer   key;
  
  private ChoiceSet choiceSet;
  private int       displayOrder;
  private String    displayValue;
  private int       rank;
  private String    value;
  
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

  ChoiceImpl
  	(ChoiceSet choiceSet,
  	 String    value,
     String    displayValue,
     int       displayOrder,
     int       rank)
  {
    super();
    this.choiceSet = choiceSet;
    this.displayOrder = displayOrder;
    this.displayValue = displayValue;
    this.rank = rank;
    this.value = value;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.Choice#getChoiceSet()
   */
  public ChoiceSet getChoiceSet()
  {
    return this.choiceSet;
  }

  /**
   * @param choiceSet The ChoiceSet to set.
   */
  void setChoiceSet(ChoiceSet choiceSet)
  {
    this.choiceSet = choiceSet;
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
    	.append(this.rank, rhs.rank)
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
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return
      "[value='"
      + this.value
      + "', displayValue='"
      + this.displayValue
      + "', rank="
      + this.rank
      + ", displayOrder='"
      + this.displayOrder
      + "']";
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    Choice choice = (Choice)o;

    return (this.getDisplayOrder() - choice.getDisplayOrder());
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
}
