/**
 * Copyright 2014 Internet2
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
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.filter;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/** 
 * Query by {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeFilter.java,v 1.2 2008-11-04 15:19:56 shilen Exp $
 * @since   1.2.0
 */
public class GroupTypeFilter extends BaseQueryFilter {

  // PRIVATE INSTANCE VARIABLES //
  private GroupType type  = null;
  private Stem      ns    = null;


  // CONSTRUCTORS //

  /**
   * {@link QueryFilter} that returns groups that have the specified 
   * {@link GroupType}.
   * @param   type  Find groups of this type.
   * @param   ns    Restrict results to within this stem.
   * @since   1.2.0
   */
  public GroupTypeFilter(GroupType type, Stem ns) {
    this.type = type;
    this.ns   = ns;
  } // public GroupTypeFilter(type, ns)


  // PUBLIC INSTANCE METHODS //

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set       groups  = new LinkedHashSet();
    Group     g;  
    Iterator  it;

    if (ns.isRootStem()) {
      it = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.type).iterator();
    } else {
      it = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.type, getStringForScope(ns)).iterator();
    }

    while (it.hasNext()) {
      g = (Group) it.next();
      groups.add(g);
    }
    // security: findAllByType is an unsecured lookup, keep only what the caller may see
    return filterTypeAssignmentReadable(s, groups);
  }

  /**
   * Keep only the groups where the caller may see the type assignment.  A legacy group type is an
   * attribute assignment on the group, so this is the same rule as Group.getTypes(true) and the
   * attribute query filters: ATTR_READ on the type's attribute definition, and GROUP_ATTR_READ (or
   * ADMIN) on the group.  Root and wheel see everything.
   * @param s session of the caller
   * @param groups groups that have the type
   * @return the groups the caller may see as having the type
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private Set filterTypeAssignmentReadable(GrouperSession s, Set groups) {
    Subject subject = s.getSubject();
    if (groups.size() == 0 || PrivilegeHelper.isWheelOrRoot(subject)) {
      return groups;
    }
    final GroupType theType = this.type;
    // the definition is looked up as root (the finder may check the caller), the privileges are checked for the caller
    AttributeDef typeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        AttributeDefName typeName = theType.getAttributeDefName();
        return typeName == null ? null : typeName.getAttributeDef();
      }
    });
    if (typeDef == null || !PrivilegeHelper.canAttrRead(s, typeDef, subject)) {
      return new LinkedHashSet();
    }
    return s.getAccessResolver().postHqlFilterGroups(groups, subject, AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
  }

}

