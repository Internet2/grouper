/*--
$Id: DataEntryException.java,v 1.2 2006-02-09 10:30:44 lmcrae Exp $
$Date: 2006-02-09 10:30:44 $

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
