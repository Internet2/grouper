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
import  org.w3c.dom.*;

/**
 * Update Groups Registry from XML file.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlUpdateFromFile.java,v 1.1 2006-10-19 15:28:59 blair Exp $
 * @since   0.1.0
 */
public class xmlUpdateFromFile {

  // PUBLIC CLASS METHODS //

  /**
   * Update Groups Registry from XML file.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   file        Use this XML in this file for updates.
   * @return  True if successful.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(Interpreter i, CallStack stack, String file) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      XmlImporter     importer  = new XmlImporter(s, new Properties());
      importer.update( XmlReader.getDocumentFromFile(file) );
      return true;
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return false;
  } // public static boolean invoke(i, stack, file)

} // public class xmlUpdateFromFile

