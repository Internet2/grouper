/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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


import  java.util.*;


/** 
 * Privilege schema specification.
 * <p />
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.6 2005-11-15 21:03:25 blair Exp $
 */
public class Privilege {

  // Private Instance Variables
  private String list;
  private String name;


  // Constructors
  protected Privilege(String name, String list) {
    this.list = list;
    this.name = name;
  } // private Privilege(name)


  // Public Instance Methods
  public String getName() {
    return this.name;
  } // public String getName()

  public String toString() {
    return this.name;
  } // public String toString()


  // Protected Instance Methods
  protected String getList() {
    return this.list;
  } // protected String getList()

}

