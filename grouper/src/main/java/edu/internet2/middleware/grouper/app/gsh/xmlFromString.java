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
 * Import Groups Registry from XML string.
 * <p/>
 * @author  blair christensen.
 * @version $Id: xmlFromString.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.1.0
 */
public class xmlFromString {

  // PUBLIC CLASS METHODS //

  /**
   * Import Groups Registry from XML string.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   xml         Import XML from this <tt>String</tt>.
   * @return  True if successful.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(Interpreter i, CallStack stack, String xml) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s         = GrouperShell.getSession(i);
      return invoke(s, xml);
    }
    catch (GrouperException eG) {
      GrouperShell.error(i, eG);
    }
    return false;
  }
  
  /**
   * Import Groups Registry from XML string.
   * <p/>
   * @param   grouperSession
   * @param   xml         Import XML from this <tt>String</tt>.
   * @return  True if successful.
   */
  public static boolean invoke(GrouperSession grouperSession, String xml) {
    //make sure right db
    GrouperUtil.promptUserAboutDbChanges("import data from xml string", true);
    XmlImporter     importer  = new XmlImporter(grouperSession, new Properties());
    importer.load( XmlReader.getDocumentFromString(xml) );
    return true;
  }

} // public class xmlFromString

