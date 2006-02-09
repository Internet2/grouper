/*--
$Id: LimitShape.java,v 1.3 2006-02-09 10:21:55 lmcrae Exp $
$Date: 2006-02-09 10:21:55 $
 
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

/**
 * This is a typesafe enumeration that identifies the various shapes
 * a Signet Limit may have.
 *  
 */
class LimitShape
	extends TypeSafeEnumeration
{
  /**
   * Constructor is private to prevent instantiation except during
   * class loading.
   * 
   * @param name
   *          the external name of the LimitShape value.
   * @param description
   *          the human readable description of the LimitShape value,
   * 					by which it is presented in the user interface.
   */
  private LimitShape(String name, String description)
  {
    super(name, description);
  }

  /**
   * The instance that represents a ChoiceSet.
   */
  public static final LimitShape CHOICE_SET
  	= new LimitShape("choice_set", "A set of discrete choices");

  /**
   * The instance that represents a Tree.
   */
  public static final LimitShape TREE
  	= new LimitShape
  			("tree",
  			 "A hierarchical tree, with the possibility of multiple roots and multiple parents for each node.");
}
