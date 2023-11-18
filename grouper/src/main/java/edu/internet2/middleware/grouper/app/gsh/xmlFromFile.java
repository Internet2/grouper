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
import java.util.Properties;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.XmlImporter;
import edu.internet2.middleware.grouper.xml.XmlReader;

/**
 * Import Groups Registry from XML file.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlFromFile.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.1.0
 */
public class xmlFromFile {

  // PUBLIC CLASS METHODS //

  /**
   * Import Groups Registry from XML file.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   file        Import XML from this file.
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
      return invoke(s, file);
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return false;
  }

  /**
   * Import Groups Registry from XML file.
   * <p/>
   * @param   grouperSession
   * @param   file        Import XML from this file.
   * @return  True if successful.
   */
  public static boolean invoke(GrouperSession grouperSession, String file) {

    //make sure right db
    GrouperUtil.promptUserAboutDbChanges("import data from xml file", true);
    XmlImporter     importer  = new XmlImporter(grouperSession, new Properties());
    importer.load( XmlReader.getDocumentFromFile(file) );
    return true;
  }
} // public class xmlFromFile

