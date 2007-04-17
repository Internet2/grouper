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
import  edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import  edu.internet2.middleware.subject.*;

/** 
 * Privilege resolution (as root) class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RootPrivilegeResolver.java,v 1.12 2007-04-17 14:17:29 blair Exp $
 * @since   1.1.0
 */
 class RootPrivilegeResolver extends PrivilegeResolver {

  // CONSTRUCTORS //
  protected RootPrivilegeResolver() {
    super();
  } // protected RootPrivilegeResolver()



  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static boolean internal_canSTEM(Stem ns, Subject subj) {
    GrouperSession  s   = ns.getSession();  
    ns.setSession( ( (GrouperSessionDTO) s.getDTO() ).getRootSession() );
    boolean         rv  = PrivilegeResolver.internal_canSTEM(ns, subj);
    ns.setSession(s);
    return rv; 
  } // protected static boolean internal_canSTEM(ns, subj)

  // @since   1.2.0
  protected static boolean internal_canVIEW(Group g, Subject subj) {
    GrouperSession  s   = g.getSession();  
    g.setSession( ( (GrouperSessionDTO) s.getDTO() ).getRootSession() );
    boolean         rv  = PrivilegeResolver.internal_canVIEW(g, subj);
    g.setSession(s);
    return rv; 
  } // protected static boolean internal_canVIEW(g, subj)

  // @since   1.2.0
  protected static boolean internal_isRoot(GrouperSession s) {
    return internal_isRoot(s, s.getSubject());
  } // protected static boolean internal_isRoot(s)

  // @since   1.2.0
  // TODO 20070321 `PrivilegeResolver.internal_hasPriv()` still requires this variant.
  //      `TestMember???` is an example test that fails otherwise.
  protected static boolean internal_isRoot(GrouperSession s, Subject subj) {
    boolean rv = false;
    // First check to see if this is GrouperSystem
    if ( SubjectHelper.eq(subj, SubjectFinder.findRootSubject()) ) {
      rv = true;
    }  
    else {
      rv = _isWheel(s, subj);
    }
    return rv;
  } // protected static boolean internal_isRoot(s)
  

  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static boolean _isWheel(GrouperSession s, Subject subj) {
    boolean       rv  = false;
    // I keep going back-and-forth on whether this should be a one-time
    // check or a repetitive check.  
    if ( Boolean.valueOf( GrouperConfig.getProperty(GrouperConfig.GWU) ).booleanValue() ) {
      String name = GrouperConfig.getProperty(GrouperConfig.GWG);
      try {
        // I suspect this isn't great for the performance
        Group wheel = GroupFinder.findByName( ( (GrouperSessionDTO) s.getDTO() ).getRootSession(), name );
        rv          = wheel.hasMember(subj);
      }
      catch (GroupNotFoundException eGNF) {
        // Group not found.  Oh well.
        ErrorLog.error(RootPrivilegeResolver.class, E.NO_WHEEL_GROUP + name);
      }
    } 
    return rv;
  } // private static boolean _isWheel(subj)

} // class RootPrivilegeResolver

