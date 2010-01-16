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
    
    //test this with GSH
    /*
    addGroup("etc:a", "etc:a");
    addGroup("etc", "a", "a");
    addGroup("etc", "b", "b");
    addGroup("etc", "c", "c");
    addComposite("etc:a", CompositeType.INTERSECTION, "etc:b", "etc:c");
    typeAdd("test");
    typeAddAttr("test", "attr", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
    groupAddType("etc:b", "test");
    grouperSession = GrouperSession.startRootSession();
    groupB = GroupFinder.findByName(grouperSession, "etc:b");
    groupB.setAttribute("attr", "value");
    groupB.store();
    stem = StemFinder.findByName(grouperSession, "etc", true);
    studentsAttrDef = stem.addChildAttributeDef("students", AttributeDefType.attr);
    userSharerRole = stem.addChildRole("userSharer", "userSharer");
    userReceiverRole = stem.addChildRole("userReceiver", "userReceiver");
    userSharerRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(userReceiverRole);
    */
    
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

      XmlExportAttributeDefName.exportAttributeDefNames(writer);

      XmlExportRoleSet.exportRoleSets(writer);

      writer.write("</grouperExport>");
      writer.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
}
