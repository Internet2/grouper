/*--
  $Id: SubjectAttributeValue.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

class SubjectAttributeValue
{
  private SubjectAttrKey 	key;
  private String 					value;
  private String 					searchValue;

  /**
   * 
   */
  public SubjectAttributeValue()
  {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param value
   * @param searchValue
   */
  public SubjectAttributeValue
  	(SubjectAttrKey key,
  	 String 				value,
  	 String 				searchValue)
  {
    this.key = key;
    this.value = value;
    this.searchValue = searchValue;
  }

  /**
   * @return Returns the key.
   */
  SubjectAttrKey getKey()
  {
    return this.key;
  }
  /**
   * @param key The key to set.
   */
  void setKey(SubjectAttrKey key)
  {
    this.key = key;
  }
  /**
   * @return Returns the searchValue.
   */
  String getSearchValue()
  {
    return this.searchValue;
  }
  /**
   * @param searchValue The searchValue to set.
   */
  void setSearchValue(String searchValue)
  {
    this.searchValue = searchValue;
  }
  /**
   * @return Returns the value.
   */
  String getValue()
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
