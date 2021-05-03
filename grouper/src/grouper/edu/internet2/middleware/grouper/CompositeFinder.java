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

package edu.internet2.middleware.grouper;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Use this class to find a composite</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * Composite c = CompositeFinder.findAsOwner(group, true)
 * </pre>
 * </blockquote>
 * 
 * </p>
 */
public class CompositeFinder {

  /**
   * parent stem id of owner group of composites to find
   */
  private String parentStemId;
  
  /**
   * 
   * @param parentStemId1
   * @return this for chaining
   */
  public CompositeFinder assignParentStemId(String parentStemId1) {
    this.parentStemId = parentStemId1;
    return this;
  }
  
  /**
   * stem scope of parent stem id
   */
  private Scope stemScope;
  /**
   * find groups where the static grouper session has certain privileges on the results
   */
  private Set<Privilege> privileges;
  /**
   * this is the subject that has certain memberships
   */
  private Subject subject;

  /**
   * 
   * @param theScope
   * @return this for chaining
   */
  public CompositeFinder assignStemScope(Scope theScope) {
    this.stemScope = theScope;
    return this;
  }
  
  /**
   * find composites
   * @return composites
   */
  public Set<Composite> findComposites() {
    return GrouperDAOFactory.getFactory().getComposite().find(GrouperSession.staticGrouperSession(), this.parentStemId, this.stemScope, this.subject, this.privileges);
  }
  
  /**
   * assign privileges to filter by that the subject has on the owner group
   * @param theGroups
   * @return this for chaining
   */
  public CompositeFinder assignPrivileges(Set<Privilege> theGroups) {
    this.privileges = theGroups;
    return this;
  }

  /**
   * this is the subject that has certain memberships in the query on owner group
   * @param theSubject
   * @return this for chaining
   */
  public CompositeFinder assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }

  /**
   * 
   */
  public CompositeFinder() {
    super();
  } 

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(CompositeFinder.class);

  /**
   * Find where the specified {@link Group} is a {@link Composite}
   * factor.
   * <pre class="eg">
   * Set composites = CompositeFinder.findAsFactor(g);
   * </pre>
   * @param   g   Search on this {@link Group}.
   * @return  A set of {@link Composite} objects.
   * @since   1.0
   */
  public static Set<Composite> findAsFactor(Group g) {
    Set<Composite>            where = new LinkedHashSet<Composite>();
    GrouperSession  s     = GrouperSession.staticGrouperSession();
    Member          m     = s.getMember();
    Composite       c;
    Iterator        it    = GrouperDAOFactory.getFactory().getComposite().findAsFactor( g ).iterator();
    while (it.hasNext()) {
      c = (Composite) it.next();
      try {
        if ( m.canView( c.getOwnerGroup() ) ) {
          where.add(c);
        }
      }
      catch (GroupNotFoundException eGNF) {
        LOG.error(E.COMPF_FINDASFACTOR + eGNF.getMessage() );
      }
    } 
    return where;
  }

  /**
   * Find {@link Composite} owned by this {@link Group}.
   * <pre class="eg">
   * Composite c = CompositeFinder.findAsOwner(g);
   * </pre>
   * @deprecated use findAsOwner(Group, boolean) instead.
   * @param   g   Search on this {@link Group}.
   * @return  c   {@link Composite} owned by this {@link Group}.
   * @throws  CompositeNotFoundException
   * @since   1.0
   */
  @Deprecated
  public static Composite findAsOwner(Group g) 
    throws  CompositeNotFoundException, GroupNotFoundException {
    return findAsOwner(g, true);
  }

  /**
   * Find {@link Composite} owned by this {@link Group}.
   * <pre class="eg">
   * Composite c = CompositeFinder.findAsOwner(g, false);
   * </pre>
   * @param   g   Search on this {@link Group}.
   * @param throwExceptionIfNotFound true to throw exception if not found
   * @return  c   {@link Composite} owned by this {@link Group}.
   * @throws  CompositeNotFoundException if throwExceptionIfNotFound is true, and composite is not found
   * @throws GroupNotFoundException if the group owner of composite cant be found (this is a problem)
   * @since   1.0
   */
  public static Composite findAsOwner(Group g, boolean throwExceptionIfNotFound) 
    throws  CompositeNotFoundException, GroupNotFoundException {
    GrouperSession  s = GrouperSession.staticGrouperSession();
    Member          m = s.getMember();
    
    Composite       c = null;
    
    c =GrouperDAOFactory.getFactory().getComposite().findAsOwner(g, throwExceptionIfNotFound);
    if (c == null) {
      return null;
    }
    if ( m.canView( c.getOwnerGroup() ) ) {
      return c;
    }
    if (throwExceptionIfNotFound) {
      throw new CompositeNotFoundException();
    }
    return null;
  } // public static Composite findAsOwner(g)

} // public class CompositeFinder

