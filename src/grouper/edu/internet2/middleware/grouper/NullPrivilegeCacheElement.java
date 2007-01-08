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
import  edu.internet2.middleware.subject.*;

/** 
 * A null {@link PrivilegeCache} element.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NullPrivilegeCacheElement.java,v 1.4 2007-01-08 16:43:56 blair Exp $
 * @since   1.1.0
 */
public class NullPrivilegeCacheElement extends PrivilegeCacheElement {

  // CONSTRUCTORS //

  /**
   * @since   1.1.0
   */
  public NullPrivilegeCacheElement(Owner o, Subject subj, Privilege p) { 
    super(o, subj, p);
    this.internal_setIsCached(false);
  } // public NullPrivilegeCacheElement(o, subj, p)


  // PUBLIC INSTANCE METHODS //

  /**
   * Will always return <b>false</b>.
   * <p/>
   * @since   1.1.0
   */
  public boolean getIsCached() {
    return false;
  } // public boolean getIsCached()

} // public class NullPrivilegeCacheElement

