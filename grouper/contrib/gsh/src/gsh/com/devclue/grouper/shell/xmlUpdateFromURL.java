/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  java.net.URL;
import  java.util.Properties;

/**
 * Update Groups Registry from XML at URL.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlUpdateFromURL.java,v 1.2 2006-10-19 16:17:35 blair Exp $
 * @since   0.1.0
 */
public class xmlUpdateFromURL {

  // PUBLIC CLASS METHODS //

  /**
   * Update Groups Registry from XML at URL.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   url         Use the XML at this <tt>URL</tt> for updates.
   * @return  True if successful.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(Interpreter i, CallStack stack, URL url) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      XmlImporter     importer  = new XmlImporter(s, new Properties());
      importer.update( XmlReader.getDocumentFromURL(url) );
      return true;
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return false;
  } // public static boolean invoke(i, stack, url)

} // public class xmlUpdateFromURL

