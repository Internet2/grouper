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
import  java.io.*;
import  java.util.Properties;

/**
 * Export Groups Registry to XML string.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlToString.java,v 1.1 2006-10-19 15:28:59 blair Exp $
 * @since   0.1.0
 */
public class xmlToString {

  // PUBLIC CLASS METHODS //

  /**
   * Export Groups Registry to XML <tt>String</tt>.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @return  XML string.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static String invoke(Interpreter i, CallStack stack) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      Writer          w         = new StringWriter();
      exporter.export(w);
      return w.toString();
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return null;
  } // public static String invoke(i, stack, group, subjId)

} // public class xmlToString

