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
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;

/**
 * Stem Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemHelper.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.0.1
 */
class StemHelper {

  // PROTECTED CLASS METHODS //

  // @return  Added {@link Stem}.
  // @since   0.0.1
  protected static Stem addStem(
    Interpreter i, String parent, String extn, String displayExtn
  ) 
    throws  GrouperShellException
  {
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            nsP = null;
      if (parent == null) {
        nsP = StemFinder.findRootStem(s);
      }
      else {
        nsP = StemFinder.findByName(s, parent, true);
      }
      return nsP.addChildStem(extn, displayExtn);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemAddException eNSA)               {
      GrouperShell.error(i, eNSA);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    return null;
  } // protected static Stem addStem(i, parent, extn, displayExtn)

} // class StemHelper

