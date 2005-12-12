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


import  java.io.Serializable;
import  org.apache.commons.logging.*;


/**
 * Find sessions.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSessionFinder.java,v 1.7 2005-12-12 21:52:09 blair Exp $
 */
class GrouperSessionFinder implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(GrouperSessionFinder.class);


  // Private Class Variables
  private static GrouperSession root = null;


  // Protected Class Methods

  // TODO Deprecate
  protected static GrouperSession getRootSession() {
    if (root == null) {
      root = getTransientRootSession();
    }
    return root;
  } // protected static GrouperSession getRootSession()

  // TODO Deprecate
  protected static GrouperSession getTransientRootSession() {
    return GrouperSession.startTransient();
  } // protected static GrouperSession getTransientRootSession()


  // Protected Instance Methods
  protected void finalize()
    throws  Throwable 
  {
    try {
      if (root != null) {
        root.stop();
      }
    }
    catch (Exception e) {
      LOG.error("exception while stopping persistent root session: " + e.getMessage());
    }
    finally {
      super.finalize();
    }
  } // protected void finalize()

}

