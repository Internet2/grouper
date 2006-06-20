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
 * @version $Id: StemHelper.java,v 1.1 2006-06-20 18:02:11 blair Exp $
 * @since   1.0
 */
class StemHelper {

  // PROTECTED CLASS METHODS //

  // @since 1.0
  protected static void addStem(
    Interpreter i, String parent, String extn, String displayExtn
  ) 
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
      Stem ns = nsP.addChildStem(extn, displayExtn);
      i.println( StemHelper.getPretty(ns) ); 
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
  } // protected static void _addStem(i, parent, extn, displayExtn)

  // @since 1.0
  protected static String getPretty(Stem ns) {
    return  "name="           + ns.getName() 
            + " displayName=" + ns.getDisplayName()
            + " uuid="        + ns.getUuid()
            ;
  } // protected static String getPretty(ns)
  
}

