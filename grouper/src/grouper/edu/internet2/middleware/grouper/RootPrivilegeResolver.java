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
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/** 
 * Privilege resolution (as root) class.
 * <p/>
 * @author  blair christensen.
 * @version $Id: RootPrivilegeResolver.java,v 1.1 2006-08-30 15:36:59 blair Exp $
 * @since   1.1.0
 */
 class RootPrivilegeResolver extends PrivilegeResolver {

  // CONSTRUCTORS //
  protected RootPrivilegeResolver() {
    super();
  } // protected RootPrivilegeResolver()



  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static boolean canVIEW(Group g, Subject subj) {
    GrouperSession  s   = g.getSession();  
    g.setSession( s.getRootSession() );
    boolean         rv  = PrivilegeResolver.canVIEW(g, subj);
    g.setSession(s);
    return rv; 
  } // protected static boolean canVIEW(g, subj)

} // class RootPrivilegeResolver

