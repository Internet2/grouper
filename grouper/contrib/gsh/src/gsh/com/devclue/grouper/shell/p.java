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
import  java.util.*;

/**
 * Pretty print results.
 * <p/>
 * @author  blair christensen.
 * @version $Id: p.java,v 1.6 2006-09-06 15:30:40 blair Exp $
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
    //if ( (obj != null) && (GrouperShell.isOurCommand(i)) ) {
    if (obj != null) {
      if      (obj instanceof Boolean)          {
        i.println(obj);
      }
      else if (obj instanceof Field)            {
        _pp(i, (Field) obj);
      }
      else if (obj instanceof Group)            {
        _pp(i, (Group) obj);
      }
      else if (obj instanceof GroupType)        {
        _pp(i, (GroupType) obj);
      }
      else if (obj instanceof HibernateSubject) {
        _pp(i, (HibernateSubject) obj);
      }
      else if (obj instanceof Integer)          {
        i.println(obj);
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
 
  // Handle {@link Field}s
  // @since   0.0.2
  private static void _pp(Interpreter i, Field f) {
    String type = f.getType().toString();  
    if      (f.getType().equals(FieldType.ACCESS))    {
      type = "access privilege";
    }
    else if (f.getType().equals(FieldType.ATTRIBUTE)) {
      type = "attribute";
    }
    else if (f.getType().equals(FieldType.LIST))      {
      type = "list";
    }
    else if (f.getType().equals(FieldType.NAMING))    {
      type = "naming privilege";
    }
    i.println(type + ": " + U.q(f.getName()));
  } // private static void _pp(i, f)

  // Handle {@link Group}s
  // @since   0.0.1
  private static void _pp(Interpreter i, Group g) {
      i.println(
        "group: "
        + "name="         + U.q(g.getName()         ) 
        + "displayName="  + U.q(g.getDisplayName()  )  
        + "uuid="         + U.q(g.getUuid()         )
      );
  } // private static void _pp(i, g)

  // Handle {@link GroupType}s
  // @since   0.0.2
  private static void _pp(Interpreter i, GroupType t) {
      i.println("type: " + U.q(t.getName()));
  } // private static void _pp(i, t)

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

