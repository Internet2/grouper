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
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

/**
 * @author  blair christensen.
 * @version $Id: CompositeFinder.java,v 1.15 2007-01-08 16:43:56 blair Exp $
 * @since   1.0
 */
public class CompositeFinder {
    
  // CONSTRUCTORS //
  // @since   1.0
  private CompositeFinder() {
    super();
  } // private CompositeFinder()


  // PUBLIC CLASS METHODS //

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
  public static Set findAsFactor(Group g) {
    Set             where = new LinkedHashSet();
    GrouperSession  s     = g.internal_getSession();
    Member          m     = s.getMember();
    Composite       c;
    Iterator        it    = internal_findAsFactor(g).iterator();
    while (it.hasNext()) {
      c = (Composite) it.next();
      c.internal_setSession(s);
      try {
        if ( m.canView( c.getOwnerGroup() ) ) {
          where.add(c);
        }
      }
      catch (GroupNotFoundException eGNF) {
        ErrorLog.error( CompositeFinder.class, E.COMPF_FINDASFACTOR + eGNF.getMessage() );
      }
    } 
    return where;
  } // public static Set findAsFactor(g)

  /**
   * Find {@link Composite} owned by this {@link Group}.
   * <pre class="eg">
   * Composite c = CompositeFinder.findAsOwner(g);
   * </pre>
   * @param   g   Search on this {@link Group}.
   * @return  c   {@link Composite} owned by this {@link Group}.
   * @throws  CompositeNotFoundException
   * @since   1.0
   */
  public static Composite findAsOwner(Group g) 
    throws  CompositeNotFoundException
  {
    Composite       c = internal_findAsOwner(g);
    GrouperSession  s = g.internal_getSession();
    Member          m = s.getMember();
    try {
      if ( m.canView( c.getOwnerGroup() ) ) {
        return c;
      }
      throw new CompositeNotFoundException();
    }
    catch (GroupNotFoundException eGNF) {
      throw new CompositeNotFoundException();
    }
  } // public static Composite findAsOwner(g)


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set internal_findAsFactor(Owner o) {
    // @filtered  false
    // @session   true
    Set       composites  = new LinkedHashSet();
    Composite c;
    Iterator  it          = HibernateCompositeDAO.findAsFactor(o).iterator();
    while (it.hasNext()) {
      c = (Composite) it.next();
      c.internal_setSession( o.internal_getSession() );
      composites.add(c);
    }
    return composites;
  } // protected static Set internal_findAsFactor(o)

  // @since   1.2.0
  protected static Composite internal_findAsOwner(Owner o)
    throws  CompositeNotFoundException
  {
    // @filtered  false
    // @session   true
    Composite c = HibernateCompositeDAO.findAsOwner(o);
    c.internal_setSession( o.internal_getSession() );
    return c;
  } // protected static Composite internal_findAsOwner(o)

} // public class CompositeFinder

