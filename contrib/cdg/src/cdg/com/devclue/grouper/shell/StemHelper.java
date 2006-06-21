/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Stem Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemHelper.java,v 1.5 2006-06-21 22:33:54 blair Exp $
 * @since   0.0.1
 */
class StemHelper {

  // PROTECTED CLASS METHODS //

  // @return  Added {@link Stem}.
  // @throws  GrouperShellException
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
        nsP = StemFinder.findByName(s, parent);
      }
      return nsP.addChildStem(extn, displayExtn);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new GrouperShellException(eIP);
    }
    catch (StemAddException eNSA)               {
      throw new GrouperShellException(eNSA);
    }
    catch (StemNotFoundException eNSNF)         {
      throw new GrouperShellException(eNSNF);
    }
  } // protected static Stem addStem(i, parent, extn, displayExtn)

} // class StemHelper

