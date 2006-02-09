/*--
$Id: Category.java,v 1.5 2006-02-09 10:18:15 lmcrae Exp $
$Date: 2006-02-09 10:18:15 $

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

import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
* Category organizes a group of {@link Function}s. Each <code>Function</code> is
* intended to correspond to a business-level task that a {@link Subject}
* must perform in order to accomplish some business operation.
* 
*/

public interface Category
extends SubsystemPart, Name, Comparable
{
  /**
   * Gets the ID of this entity.
   * 
   * @return Returns a short mnemonic id which will appear in XML
   *    documents and other documents used by analysts.
   */
  public String getId();
  
  /**
   * Gets the {@link Function}s associated with this Category.
   * 
   * @return Returns the functions associated with this Category.
   */
  public Set getFunctions();

  /**
   * Gets the Subsystem associated with this Category.
   * 
   * @return the Subsystem associated with this Category.
   */
  public Subsystem getSubsystem();
}
