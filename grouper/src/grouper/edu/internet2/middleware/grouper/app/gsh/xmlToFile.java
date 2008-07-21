/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.xml.XmlExporter;

import  java.io.*;
import  java.util.Properties;

/**
 * Export Groups Registry to XML file.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlToFile.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.1.0
 */
public class xmlToFile {

  // PUBLIC CLASS METHODS //

  /**
   * Export Groups Registry to XML file.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   file        Export XML to file with this name.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(Interpreter i, CallStack stack, String file) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      XmlExporter     exporter  = new XmlExporter(s, new Properties());
      Writer          w         = new BufferedWriter( new FileWriter( new File(file) ) );
      exporter.export(w);
      return true;
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    catch (IOException eIO)     {
      GrouperShell.error(i, eIO);
    }
    return false;
  } // public static boolean invoke(i, stack, file)

} // public class xmlToFile

