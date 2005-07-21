/*--
$Id: LimitValue.java,v 1.10 2005-07-21 22:48:06 acohen Exp $
$Date: 2005-07-21 22:48:06 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Comparator;
import java.util.HashSet;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFound;

/**
 * Encapsulates the many-to-many relationship between Limits and their selected
 * values.
 */
public class LimitValue
implements Comparable
{
  private static Comparator displayOrderComparator;
  
  private Limit		limit;
  private String	value;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public LimitValue()
  {
    super();
  }
  
  /**
   * @param limit
   * @param value
   */
  public LimitValue(Limit limit, String value)
  {
    super();
    this.limit = limit;
    this.value = value;
  }
  
  /**
   * @return Returns the limit.
   */
  public Limit getLimit()
  {
    return this.limit;
  }
  
  /**
   * @param limit The limit to set.
   */
  void setLimit(Limit limit)
  {
    this.limit = limit;
  }
  
  /**
   * @return Returns the value.
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
  
  /**
   * Gets the display-value for this limit-value, if it's available in the
   * database. Otherwise, it returns the internal value.
   */
  public String getDisplayValue()
  {
    String displayValue;
    
    try
    {
      displayValue
      	= this
      			.limit
      				.getChoiceSet()
      					.getChoiceByValue(this.value)
      						.getDisplayValue();
    }
    catch (ChoiceNotFoundException e)
    {
      displayValue = this.value + " (display-value not available)";
    }
    
    return displayValue;
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof LimitValue))
    {
      return false;
    }

    LimitValue rhs = (LimitValue) obj;
    return new EqualsBuilder()
    	.append(this.getLimit(), rhs.getLimit())
    	.append(this.getValue(), rhs.getValue())
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
    	.append(this.getLimit())
    	.append(this.getValue())
    	.toHashCode();
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "[" + this.limit + " : " + this.value + "]";
  }
  
  public int compareTo(Object o)
  {
    LimitValue rhs = (LimitValue) o;
    Limit thisLimit = this.getLimit();
    Limit rhsLimit = rhs.getLimit();
    
    Choice thisChoice;
    Choice rhsChoice;
    
    try
    {
      thisChoice
        = thisLimit.getChoiceSet().getChoiceByValue(this.getValue());
      rhsChoice
        = rhsLimit.getChoiceSet().getChoiceByValue(rhs.getValue());
    }
    catch (ChoiceNotFoundException cnfe)
    {
      throw new SignetRuntimeException(cnfe);
    }
    
    return new CompareToBuilder()
      // .appendSuper(super.compareTo(o)
      .append(thisLimit, rhsLimit)
      .append(thisChoice, rhsChoice)
      .toComparison();
  }
}
