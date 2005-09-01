/*--
$Id: DataEntryException.java,v 1.1 2005-09-01 17:59:58 acohen Exp $
$Date: 2005-09-01 17:59:58 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class DataEntryException extends Exception
{
  private Exception rootCause;
  private String    fieldNameRoot;
  private String    badStr;
  private String    goodExample;
  
  DataEntryException
    (Exception rootCause,
     String    fieldNameRoot,
     String    badStr,
     String    goodExample)
  {
    this.rootCause = rootCause;
    this.fieldNameRoot = fieldNameRoot;
    this.badStr = badStr;
    this.goodExample = goodExample;
  }
  
  String getBadStr()
  {
    return this.badStr;
  }
  
  String getFieldNameRoot()
  {
    return this.fieldNameRoot;
  }
  
  Exception getRootCause()
  {
    return this.rootCause;
  }
  
  String getGoodExample()
  {
    return this.goodExample;
  }
  
  public String toString()
  {
    StringBuffer outStr = new StringBuffer();
    
    outStr.append("Date values must follow the pattern '");
    outStr.append(this.goodExample);
    outStr.append("'. Your previous entry, '");
    outStr.append(this.badStr);
    outStr.append("', did not follow this pattern. Please correct your entry.");
    
    return outStr.toString();
  }
}
