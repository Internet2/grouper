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

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;


/** 
 * {@link Subject} returned by the {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.1.2.1 2005-11-07 01:03:10 blair Exp $
 */
public class GrouperSubject implements Subject {

  // Private Instance Methods
  private GrouperSourceAdapter  adapter = null;
  private Map                   attrs   = null; 
  private String                id      = null;
  private String                name    = null;
  private SubjectType           type    = SubjectTypeEnum.valueOf("group");


  // Constructors
  protected GrouperSubject(Group g, GrouperSourceAdapter sa) {
    this.id       = g.getUuid();
    this.name     = g.getName();
    this.adapter  = sa;
  } // protected GrouperSubject(g, sa)


  // Public Instance Methods
  public Map getAttributes() {
    throw new RuntimeException("Not implemented");
  } // public Map getAttributes()

  public String getAttributeValue(String name) {
    throw new RuntimeException("Not implemented");
  } // public String getAttributevalue(name)

  public Set getAttributeValues(String name) {
    throw new RuntimeException("Not implemented");
  } // public Set getAttributeValues(name)

  public String getDescription() {
    // TODO Is this still part of the API?
    throw new RuntimeException("Not implemented");
  } // public String getDescription()

  public String getId() {
    return this.id;
  } // public String getId()

  public String getName() {
    return this.name;
  } // public String getName()

  public Source getSource() {
    return this.adapter;
  } // public Source getSource()

  public SubjectType getType() {
    return this.type;
  } // public SubjectType getType()

}

