/*
 * Copyright (C) 2006 blair christensen.
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
 * Pretty print results.
 * <p/>
 * @author  blair christensen.
 * @version $Id: p.java,v 1.1 2006-06-23 17:30:09 blair Exp $
 * @since   0.0.1
 */
public class p {

  // PUBLIC CLASS METHODS //

  /**
   * Pretty print results.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   obj   Object to print.
   * @since   0.0.1
   */
  public static void invoke(Interpreter i, CallStack stack, Object obj) {
    GrouperShell.setOurCommand(i, true);
    pp(i, obj);
  } // public static void invoke(i, stack, obj)


  // PROTECTED CLASS METHODS //

  // Pretty print results
  // @since   0.0.1
  protected static void pp(Interpreter i, Object obj) {
    if ( (obj != null) && (GrouperShell.isOurCommand(i)) ) {
      // FIXME Can't I do this properly with reflection?
      if      (obj instanceof Boolean)          {
        i.println(obj);
      }
      else if (obj instanceof Group)            {
        _pp(i, (Group) obj);
      }
      else if (obj instanceof HibernateSubject) {
        _pp(i, (HibernateSubject) obj);
      }
      else if (obj instanceof Member)           {
        _pp(i, (Member) obj);
      }
      else if (obj instanceof Set)              {
        _pp(i, (Set) obj); 
      }
      else if (obj instanceof Source)           {
        _pp(i, (Source) obj);
      }
      else if (obj instanceof Stem)             {
        _pp(i, (Stem) obj); 
      }
      else if (obj instanceof String)           {
        i.println(obj);
      }
      else if (obj instanceof Subject)          {
        _pp(i, (Subject) obj);
      }
      else                                      {
        _pp(i, obj);  // fall back to the default
      }
    }
  } // protected static void pp(i, obj)


  // PRIVATE CLASS METHODS //
 
  // Handle {@link Group}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Group g) {
      i.println(
        "group: "
        + "name="         + U.q(g.getName()         ) 
        + "displayName="  + U.q(g.getDisplayName()  )  
        + "uuid="         + U.q(g.getUuid()         )
      );
  } // private static void _pp(i, ns)

  // Handle {@link HibernateSubject}s
  // @since   0.0.1
  private static void _pp(Interpreter i, HibernateSubject subj) {
      i.println(
        "hibernatesubject: " 
        + "id="     + U.q(  subj.getSubjectId()       )
        + "type="   + U.q(  subj.getSubjectTypeId()   )
        + "name="   + U.q(  subj.getName()            )
      );
  } // private static void _pp(i, subj)

  // Handle {@link Member}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Member m) {
      i.println(
        "member: " 
        + "id="     + U.q(  m.getSubjectId()        )
        + "type="   + U.q(  m.getSubjectTypeId()    )
        + "source=" + U.q(  m.getSubjectSourceId()  )
        + "uuid="   + U.q(  m.getUuid()             )
      );
  } // private static void _pp(i, m)

  // Default pretty printer
  // @since   0.0.1
  private static void _pp(Interpreter i, Object obj) {
    i.println(obj.getClass().getName() + ": " + obj.toString());
  } // private static void _pp(i, obj)
    
  // Handle {@link Set}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Set obj) {
    Iterator iter = obj.iterator();
    while (iter.hasNext()) {
      pp(i, iter.next());
    }
  } // private static void _pp(i, obj)

  // Handle {@link Source}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Source src) {
      i.println(
        "source: "
        + "id="     + U.q(  src.getId()               )
        + "name="   + U.q(  src.getName()             )
        + "class="  + U.q(  src.getClass().getName()  )
      );
  } // private static void _pp(i, src)

  // Handle {@link Stem}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Stem ns) {
      i.println(
        "stem: " 
        + "name="         + U.q(ns.getName()        ) 
        + "displayName="  + U.q(ns.getDisplayName() )  
        + "uuid="         + U.q(ns.getUuid()        )
      );
  } // private static void _pp(i, ns)

  // Handle {@link Subject}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Subject subj) {
      i.println(
        "subject: " 
        + "id="     + U.q(  subj.getId()              )
        + "type="   + U.q(  subj.getType().getName()  )
        + "source=" + U.q(  subj.getSource().getId()  )
        + "name="   + U.q(  subj.getName()            )
      );
  } // private static void _pp(i, subj)

} // public class p

