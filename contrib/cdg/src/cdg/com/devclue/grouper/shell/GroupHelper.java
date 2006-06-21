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
 * Group Helper Methods.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupHelper.java,v 1.2 2006-06-21 20:28:55 blair Exp $
 * @since   0.0.1
 */
class GroupHelper {

  // PROTECTED CLASS METHODS //

  // @since 0.0.1
  protected static void addGroup(
    Interpreter i, String parent, String extn, String displayExtn
  ) 
  {
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, parent);
      Group           g   = ns.addChildGroup(extn, displayExtn);
      i.println( GroupHelper.getPretty(g) ); 
    }
    catch (GroupAddException eGA)               {
      GrouperShell.error(i, eGA);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
  } // protected static void addGroup(i, parent, extn, displayExtn)

  // @since 0.0.1 
  protected static void delGroup(Interpreter i, String name) {
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, name);
      g.delete();
    }
    catch (GroupDeleteException eGD)            {
      GrouperShell.error(i, eGD);
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
  } // protected static void delStem(i, name)

  // @since 0.0.1 
  protected static void getGroups(Interpreter i, String name) {
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Stem            root  = StemFinder.findRootStem(s);
      GrouperQuery    gq    = GrouperQuery.createQuery(
        s, 
        new GroupNameFilter(name, root)
      );
      Iterator iter = gq.getGroups().iterator();
      while (iter.hasNext()) {
        Group g = (Group) iter.next();
        i.println( GroupHelper.getPretty(g) );
      }
    }
    catch (QueryException eQ) {
      GrouperShell.error(i, eQ);
    }
  } // protected static void getStems(i, name)

  // @since 0.0.1
  protected static String getPretty(Group g) {
    return    "name="         + U.q(  g.getName()         )
            + "displayName="  + U.q(  g.getDisplayName()  )
            + "uuid="         + U.q(  g.getUuid()         )
            ;
  } // protected static String getPretty(g)
 
} // class GroupHelper

