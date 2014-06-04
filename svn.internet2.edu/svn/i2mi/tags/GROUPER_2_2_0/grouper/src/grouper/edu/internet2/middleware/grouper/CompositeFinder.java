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

import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: CompositeFinder.java,v 1.26 2009-03-15 06:37:21 mchyzer Exp $
 * @since   1.0
 */
public class CompositeFinder {

  /**
   * 
   */
  private CompositeFinder() {
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

