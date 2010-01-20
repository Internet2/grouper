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

  /** if comments should be included for foreign keys, note, this slows down the export */
  private boolean includeComments;
  
  /**
   * if comments should be included for foreign keys, note, this slows down the export
   * @return the includeComments
   */
  public boolean isIncludeComments() {
    return this.includeComments;
  }

  
  /**
   * if comments should be included for foreign keys, note, this slows down the export
   * @param includeComments1 the includeComments to set
   */
  public void setIncludeComments(boolean includeComments1) {
    this.includeComments = includeComments1;
  }

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    
    //test this with GSH
    /*
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
    action = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction");
    action2 = studentsAttrDef.getAttributeDefActionDelegate().addAction("someAction2");
    action.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(action2);

    studentsAttrDef.setAssignToGroup(true);
    studentsAttrDef.store();

    studentsAttrDef2 = stem.addChildAttributeDef("students2", AttributeDefType.attr);
    studentsAttrDef2.setAssignToGroupAssn(true);
    studentsAttrDef2.store();

    
    studentsAttrName = stem.addChildAttributeDefName(studentsAttrDef, "studentsName", "studentsName");
    studentsAttrName2 = stem.addChildAttributeDefName(studentsAttrDef2, "studentsName2", "studentsName2");

    attributeAssignResult = groupB.getAttributeDelegate().assignAttribute(studentsAttrName);
    attributeAssignResult.getAttributeAssign().getAttributeDelegate().assignAttribute(studentsAttrName2);

    AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
    attributeAssignValue.setId(edu.internet2.middleware.grouper.internal.util.GrouperUuid.getUuid());
    attributeAssignValue.setAttributeAssignId(attributeAssignResult.getAttributeAssign().getId());
    attributeAssignValue.setValueString("string");
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
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

      XmlExportGroupTypeTuple.exportGroupTypeTuples(writer, this);

      XmlExportComposite.exportComposites(writer, this);

      XmlExportAttribute.exportAttributes(writer, this);

      XmlExportAttributeDef.exportAttributeDefs(writer);

      XmlExportMembership.exportMemberships(writer, this);

      XmlExportAttributeDefName.exportAttributeDefNames(writer);

      XmlExportRoleSet.exportRoleSets(writer, this);

      XmlExportAttributeAssignAction.exportAttributeAssignActions(writer);

      XmlExportAttributeAssignActionSet.exportAttributeAssignActionSets(writer, this);

      XmlExportAttributeAssign.exportAttributeAssigns(writer);

      XmlExportAttributeAssignValue.exportAttributeAssignValues(writer);

      XmlExportAttributeDefNameSet.exportAttributeDefNameSets(writer);

      XmlExportAttributeDefScope.exportAttributeDefScopes(writer);

      writer.write("</grouperExport>\n");
      writer.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
  
}
