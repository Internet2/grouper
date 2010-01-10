/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class XmlExportMain {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    StringWriter stringWriter = new StringWriter();
    XmlExportMain xmlExportMain = new XmlExportMain();
    xmlExportMain.writeAllTables(stringWriter);
    System.out.println(stringWriter);
  }
  
  /**
   * write the xml to a writer
   * @param writer
   */
  public void writeAllTables(Writer writer) {
    
    try {
      //note, cant use stax since you cant mix stax and non stax since it wont close elements
      writer.write("<?xml version=\"1.0\" ?>\n<grouperExport");
      GrouperUtil.xmlAttribute(writer, "version", GrouperVersion.GROUPER_VERSION);
      GrouperUtil.xmlAttribute(writer, "folderRoot", ":");
      GrouperUtil.xmlAttribute(writer, "members", "all");
      writer.write(">\n");

      XmlExportMember.exportMembers(writer);

      XmlExportStem.exportStems(writer);
      
      XmlExportGroup.exportGroups(writer);
      
      XmlExportGroupType.exportGroupTypes(writer);

      XmlExportField.exportFields(writer);

      writer.write("</grouperExport>");
      writer.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
}
