/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.misc;
import  java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;

/**
 * 
 * All immediate subjects, and effective members are members.  
 * No duplicates will be returned (e.g. if immediate and effective).
 * 
 * <b>THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b> 
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.3 2009-06-09 22:55:40 shilen Exp $
 * @since   1.2.0
 */
public interface MemberOf {

  /**
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * @since   1.2.0
   * @throws IllegalStateException
   */
  void addComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException;

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * @since   1.2.0
   * @throws IllegalStateException
   */
  void addImmediate(GrouperSession s, Group g, Field f, Member _m)
    throws  IllegalStateException;  

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * @since   1.2.0
   * @throws IllegalStateException
   */
  void addImmediate(GrouperSession s, Stem ns, Field f, Member _m)
    throws  IllegalStateException; 

  /**
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * @since   1.2.0
   * @throws IllegalStateException
   */
  void deleteComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException;

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * @since   1.2.0
   * @throws IllegalStateException
   */
  void deleteImmediate(GrouperSession s, Group g, Membership _ms, Member _m)
    throws  IllegalStateException;

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * @since   1.2.0
   * @throws IllegalStateException
   */
  void deleteImmediate(GrouperSession s, Stem ns, Membership _ms, Member _m)
    throws  IllegalStateException;

  /**
   * @since   1.2.0
   */
  Set getSaves();
  
  /**
   * @since   1.2.0
   */
  Set getModifiedGroups();
  
  
  /**
   * @since    1.2.0
   */
  Set getModifiedStems();
  
  /**
   * @since   1.2.0
   */
  Set getDeletes();
  
  /**
   * @since   1.5.0
   * @return updates to objects
   */
  Set<GrouperAPI> getUpdates();
  
  /**
   * @return set of group ids that have modified memberships
   */
  Set<String> getGroupIdsWithNewMemberships();
  
  /**
   * @return set of stem ids that have modified memberships
   */
  Set<String> getStemIdsWithNewMemberships();
  
}
