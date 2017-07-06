/**
 * Copyright 2014 Internet2
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
 */
/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.Iterator;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Pretty print results.
 * <p/>
 * @author  blair christensen.
 * @version $Id: p.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
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

  /**
   * Pretty print results.
   * <p/>
   * @param   obj   Object to print.
   */
  public static void invoke(Object obj) {
    StringBuilder builder = new StringBuilder();
    pp(builder, obj);
    System.out.print(builder.toString());
  }

  // PROTECTED CLASS METHODS //

  // Pretty print results
  protected static void pp(Interpreter i, Object obj) {
    StringBuilder builder = new StringBuilder();
    pp(builder, obj);
    i.print(builder.toString());
  }
  
  // Pretty print results
  protected static void pp(StringBuilder builder, Object obj) {
    //if ( (obj != null) && (GrouperShell.isOurCommand(i)) ) {
    if (obj != null) {
      if      (obj instanceof Boolean)          {
        builder.append(obj + "\n");
      }
      else if (obj instanceof Field)            {
        _pp(builder, (Field) obj);
      }
      else if (obj instanceof Group)            {
        _pp(builder, (Group) obj);
      }
      else if (obj instanceof GroupType)        {
        _pp(builder, (GroupType) obj);
      }
      else if (obj instanceof RegistrySubject) {
        _pp(builder, (RegistrySubject) obj);
      }
      else if (obj instanceof Integer)          {
        builder.append(obj + "\n");
      }
      else if (obj instanceof Member)           {
        _pp(builder, (Member) obj);
      }
      else if (obj instanceof Set)              {
        _pp(builder, (Set) obj); 
      }
      else if (obj instanceof Source)           {
        _pp(builder, (Source) obj);
      }
      else if (obj instanceof Stem)             {
        _pp(builder, (Stem) obj); 
      }
      else if (obj instanceof String)           {
        builder.append(obj + "\n");
      }
      else if (obj instanceof Subject)          {
        _pp(builder, (Subject) obj);
      }
      else                                      {
        _pp(builder, obj);  // fall back to the default
      }
    }
  } // protected static void pp(i, obj)


  // PRIVATE CLASS METHODS //
 
  // Handle {@link Field}s
  // @since   0.1.0
  private static void _pp(StringBuilder builder, Field f) {
    String type = f.getType().toString();  
    if      (f.getType().equals(FieldType.ACCESS))    {
      type = "access privilege";
    }
    else if (f.getType().equals(FieldType.LIST))      {
      type = "list";
    }
    else if (f.getType().equals(FieldType.NAMING))    {
      type = "naming privilege";
    }
    builder.append(type + ": " + GshUtil.q(f.getName()) + "\n");
  } // private static void _pp(i, f)

  // Handle {@link Group}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Group g) {
    builder.append(
        "group: "
        + "name="        + GshUtil.q(g.getName()        ) 
        + "displayName=" + GshUtil.q(g.getDisplayName() )  
        + "uuid="        + GshUtil.q(g.getUuid()        )
      + "\n");
  } // private static void _pp(i, g)

  // Handle {@link GroupType}s
  // @since   0.1.0
  private static void _pp(StringBuilder builder, GroupType t) {
    builder.append("type: " + GshUtil.q(t.getName()) + "\n");
  } // private static void _pp(i, t)

  // Handle {@link RegistrySubject}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, RegistrySubject subj) {
    builder.append(
        "hibernatesubject: " 
        + "id="   + GshUtil.q(  subj.getId()             )
        + "type=" + GshUtil.q(  subj.getType().getName() )
        + "name=" + GshUtil.q(  subj.getName()           )
      + "\n");
  } // private static void _pp(i, subj)

  // Handle {@link Member}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Member m) {
    builder.append(
        "member: " 
        + "id="     + GshUtil.q(  m.getSubjectId()       )
        + "type="   + GshUtil.q(  m.getSubjectTypeId()   )
        + "source=" + GshUtil.q(  m.getSubjectSourceId() )
        + "uuid="   + GshUtil.q(  m.getUuid()            )
      + "\n");
  } // private static void _pp(i, m)

  // Default pretty printer
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Object obj) {
    builder.append(obj.getClass().getName() + ": " + obj.toString() + "\n");
  } // private static void _pp(i, obj)
    
  // Handle {@link Set}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Set obj) {
    Iterator iter = obj.iterator();
    while (iter.hasNext()) {
      pp(builder, iter.next());
    }
  } // private static void _pp(i, obj)

  // Handle {@link Source}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Source src) {
    builder.append(
        "source: "
        + "id="     + GshUtil.q(  src.getId()               )
        + "name="   + GshUtil.q(  src.getName()             )
        + "class="  + GshUtil.q(  src.getClass().getName()  )
      + "\n");
  } // private static void _pp(i, src)

  // Handle {@link Stem}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Stem ns) {
    builder.append(
        "stem: " 
        + "name="         + GshUtil.q(ns.getName()        ) 
        + "displayName="  + GshUtil.q(ns.getDisplayName() )  
        + "uuid="         + GshUtil.q(ns.getUuid()        )
      + "\n");
  } // private static void _pp(i, ns)

  // Handle {@link Subject}s
  // @since   0.0.1
  private static void _pp(StringBuilder builder, Subject subj) {
    builder.append(
        "subject: " 
        + "id="     + GshUtil.q(  subj.getId()              )
        + "type="   + GshUtil.q(  subj.getType().getName()  )
        + "source=" + GshUtil.q(  subj.getSource().getId()  )
        + "name="   + GshUtil.q(  subj.getName()            )
      + "\n");
  } // private static void _pp(i, subj)

} // public class p

