/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class LimitImpl implements Limit
{
  private String		limitId;
  private ValueType valueType;
  private String		limitTypeId;
  private String		name;
  private String		helpText;
  private String		limitType;
  
  /**
   * @param limitId
   * @param limitType
   * @param limitTypeId
   * @param name
   * @param helpText
   * @param valueType
   */
  LimitImpl
    (String 		name,
     String 		limitType,
     String 		limitId,
     ValueType 	valueType,
     String 		limitTypeId,
     String 		helpText)
  {
    super();
    this.limitId = limitId;
    this.limitType = limitType;
    this.limitTypeId = limitTypeId;
    this.name = name;
    this.helpText = helpText;
    this.valueType = valueType;
  }
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getLimitId()
   */
  public String getLimitId()
  {
    return this.limitId;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getLimitType()
   */
  public String getLimitType()
  {
    return this.limitType;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getLimitTypeId()
   */
  public String getLimitTypeId()
  {
    return this.limitTypeId;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getName()
   */
  public String getName()
  {
    return this.name;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getHelpText()
   */
  public String getHelpText()
  {
    return this.helpText;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Limit#getValueType()
   */
  public ValueType getValueType()
  {
    return this.valueType;
  }

}
