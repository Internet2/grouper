/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;


import  java.io.Serializable;
import  java.util.*;


/** 
 * QueueStatus schema specification.
 * <p />
 * @author  blair christensen.
 * @version $Id: QueueStatus.java,v 1.1 2006-02-07 20:46:44 blair Exp $
 */
public class QueueStatus implements Serializable {

  // Private Class Constants
  private static final QueueStatus FAIL   = new QueueStatus("fail");
  private static final QueueStatus WAIT   = new QueueStatus("wait");


  // Private Class Constants
  private static final Map TYPES  = new HashMap();


  // Private Instance Variables
  private String name;


  static {
    TYPES.put( FAIL.toString(), FAIL );
    TYPES.put( WAIT.toString(), WAIT );
  } // static


  // Constructors
  private QueueStatus(String name) {
    this.name = name;
  } // private QueueStatus(name)


  // Public Class Methods
  // TODO Should this be public?
  public static QueueStatus getInstance(String name) {
    return (QueueStatus) TYPES.get(name);
  } // public static QueueStatus getInstance(name)

  
  // Public Instance Methods
  public String getName() {
    return this.name;
  } // public String getName()

  // Public Instance Methods
  public String toString() {
    return this.getName();
  } // public String toString()


  Object readResolve() {
    return getInstance(name);
  } // Object readResolve()

}

