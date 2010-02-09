/*
 Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 Copyright 2004-2006 The University Of Bristol

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.grouper.xml.export;
import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Utility class for exporting data in XML import from the Groups Registry.
 * @author  Chris Hyzer
 * @version $Id: XmlImporter.java,v 1.24 2009-11-11 16:48:11 mchyzer Exp $
 * @since   1.0
 */
public class XmlExportGsh {

  /**
   * 
   */
  private static final String NOPROMPT_ARG = "noprompt";
  /**
   * 
   */
  public static final String RECORD_REPORT_ARG = "recordReport";
  
  /** log */
  @SuppressWarnings("unused")
  private static final Log    LOG           = GrouperUtil.getLog(XmlExportGsh.class);
  
  /**
   * Export the registry as GrouperSystem
   * <p/>
   * @param   args    args[0] = name of Xml file to write to
   * @since   1.1.0
   */
  public static void main(final String[] args) {
    
    if (XmlExportUtils.internal_wantsHelp(args)) {
      System.out.println( _getUsage() );
      //System.exit(0);
      return;
    }
    final Map<String, Object> argsMap;
    try {
      argsMap = XmlExportUtils.internal_getXmlExportArgs(args);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println();
      System.err.println( _getUsage() );
      //System.exit(1);
      return;
    }
    
    //make sure right db
    if(!Boolean.TRUE.equals(argsMap.get(NOPROMPT_ARG))) {
      GrouperUtil.promptUserAboutDbChanges("export data to xml", true);
    }
    GrouperStartup.runFromMain = true;
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    try {

      XmlExportMain xmlExportMain = new XmlExportMain();
      
      String fileName = (String)argsMap.get(XmlExportUtils.FILE_NAME_ARG);
      
      if (StringUtils.isBlank(fileName)) {
        throw new RuntimeException("Enter a file name");
      }
      
      File file = new File(fileName);
      
      xmlExportMain.writeAllTables(file);
      
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    System.exit(0);
    return;
  } 


  /**
   * 
   * @return usage
   */
  private static String _getUsage() {
    return  "Usage:"                                                                + GrouperConfig.NL
            + "args: -h,            Prints this message"                            + GrouperConfig.NL
            + "args:  "             + GrouperConfig.NL
            + "      [-noprompt] filename"           + GrouperConfig.NL
            + "e.g.  gsh -xmlexport f:/temp/prod.xml"                 + GrouperConfig.NL
            +                                                                         GrouperConfig.NL
            + "  -noprompt,         Do not prompt user to confirm the database that"+ GrouperConfig.NL
            + "                     will be updated"                                + GrouperConfig.NL
            + "  filename,          The file to import"                             + GrouperConfig.NL
            ;
  }


} 

