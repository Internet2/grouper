/*
 * Created on Feb 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet;

/**
 * Encapsulates the many-to-many relationship between Limits and their selected
 * values.
 */
public class LimitValue
{
  private Limit		limit;
  private String	value;
  
  /**
   * @param limit
   * @param value
   */
  LimitValue(Limit limit, String value)
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
}
