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
 * @version $Id: StemHelper.java,v 1.3 2006-06-20 19:53:17 blair Exp $
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
  } // protected static void addStem(i, parent, extn, displayExtn)

  // @since 1.0 
  protected static void delStem(Interpreter i, String name) {
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, name);
      ns.delete();
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemDeleteException eNSD)            {
      GrouperShell.error(i, eNSD);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
  } // protected static void delStem(i, name)

  // @since 1.0
  protected static String getPretty(Stem ns) {
    return    "name="         + U.q(  ns.getName()        )
            + "displayName="  + U.q(  ns.getDisplayName() )
            + "uuid="         + U.q(  ns.getUuid()        )
            ;
  } // protected static String getPretty(ns)
 
  // @since 1.0 
  protected static void getStems(Interpreter i, String name) {
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Stem            root  = StemFinder.findRootStem(s);
      GrouperQuery    gq    = GrouperQuery.createQuery(
        s, 
        new StemNameAnyFilter(name, root)
      );
      Iterator iter = gq.getStems().iterator();
      while (iter.hasNext()) {
        Stem ns = (Stem) iter.next();
        i.println( StemHelper.getPretty(ns) );
      }
    }
    catch (QueryException eQ) {
      GrouperShell.error(i, eQ);
    }
  } // protected static void getStems(i, name)

} // class StemHelper

