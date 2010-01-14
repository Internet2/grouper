/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import edu.internet2.middleware.grouper.Group;
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
    
    //test this with GSH
    //gsh 0% addGroup("etc:a", "etc:a");
    //gsh 1% addGroup("etc", "a", "a");
    //gsh 2% addGroup("etc", "b", "b");
    //gsh 3% addGroup("etc", "c", "c");
    //gsh 5% addComposite("etc:a", CompositeType.INTERSECTION, "etc:b", "etc:c");
    //gsh 6% typeAdd("test");
    //gsh 8% typeAddAttr("test", "attr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
    //gsh 9% groupAddType("etc:b", "test");
    //gsh 10% grouperSession = GrouperSession.startRootSession();
    //edu.internet2.middleware.grouper.GrouperSession: 56bd9b456d08410590f56582df8b84ee,'GrouperSystem','application'
    //gsh 11% groupB = GroupFinder.findByName(grouperSession, "etc:b");
    //group: name='etc:b' displayName='etc:b' uuid='6b94cf5a8cc44dfea7f6e4450be133f8'
    //gsh 12% groupB.setAttribute("attr", "value");
    //gsh 13% groupB.store();
    //gsh 14% stem = StemFinder.findByName(grouperSession, "etc", true);
    //gsh 15% studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
    //gsh 16%

    
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

      XmlExportGroupTypeTuple.exportGroupTypeTuples(writer);

      XmlExportComposite.exportComposites(writer);

      XmlExportAttribute.exportAttributes(writer);

      XmlExportAttributeDef.exportAttributeDefs(writer);

      XmlExportMembership.exportMemberships(writer);

      writer.write("</grouperExport>");
      writer.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
}
