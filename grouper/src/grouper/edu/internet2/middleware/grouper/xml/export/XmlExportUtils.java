/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 * utils about xml export
 */
public class XmlExportUtils {

  /**
   * 
   * @param element
   * @return the string
   */
  public static String toString(Element element) {
    OutputFormat format = OutputFormat.createPrettyPrint();
    StringWriter stringWriter = new StringWriter();
    XMLWriter writer = new XMLWriter(stringWriter, format );
    try {
      writer.write(element);                
    } catch (IOException ioe) {
      return ioe.toString();
    }
    return stringWriter.toString();
  }
  
  /**
   * take a record from xml and sync with db
   * @param xmlImportable
   * @param xmlImportMain
   */
  public static void syncImportable(XmlImportable xmlImportable, XmlImportMain xmlImportMain) {
    
    GrouperUtil.substituteStrings(xmlImportMain.getUuidTranslation(), xmlImportable);
    
    XmlImportable dbObject = xmlImportable.xmlRetrieveByIdOrKey();
    boolean insert = false;
    boolean update = false;
    
    //if not in db
    if (dbObject == null) {
      dbObject = (XmlImportable)xmlImportable.xmlSaveBusinessProperties(dbObject);
      insert = true;
    } else {
      //db is there, see if different
      if (!StringUtils.equals(xmlImportable.xmlGetId(), dbObject.xmlGetId())) {
        //this is a translation
        xmlImportMain.getUuidTranslation().put(xmlImportable.xmlGetId(), dbObject.xmlGetId());
        xmlImportable.xmlSetId(dbObject.xmlGetId());
      }
      if (xmlImportable.xmlDifferentBusinessProperties(dbObject)) {
        update = true;
        xmlImportable.xmlCopyBusinessPropertiesToExisting(dbObject);
        dbObject = (XmlImportable)xmlImportable.xmlSaveBusinessProperties(dbObject);
      }
    }
    //see if update properties need work
    if (xmlImportable.xmlDifferentUpdateProperties(dbObject)) {
      update = true;
      xmlImportable.xmlSaveUpdateProperties();
    }

    //dont increment insert and update, if insert, do that one, else update
    if (insert) {
      xmlImportMain.incrementInsertCount();
    } else if (update) {
      xmlImportMain.incrementUpdateCount();
    } else {
      xmlImportMain.incrementSkipCount();
    }
    
  }
  
  /**
   * @return xstream
   */
  public static XStream xstream() {

    final XStream xStream = new XStream(new XppDriver());
    
    //do javabean properties, not fields
    xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {

      /**
       * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
       */
      @SuppressWarnings("unchecked")
      @Override
      public boolean canConvert(Class type) {
        //see if one of our beans
        return type.getName().startsWith("edu.internet2");
      }
      
    }); 

    registerClass(xStream, XmlExportAttribute.class);
    registerClass(xStream, XmlExportAttributeAssign.class);
    registerClass(xStream, XmlExportAttributeAssignAction.class);
    registerClass(xStream, XmlExportAttributeAssignActionSet.class);
    registerClass(xStream, XmlExportAttributeAssignValue.class);
    registerClass(xStream, XmlExportAttributeDef.class);
    registerClass(xStream, XmlExportAttributeDefName.class);
    registerClass(xStream, XmlExportAttributeDefNameSet.class);
    registerClass(xStream, XmlExportAttributeDefScope.class);
    registerClass(xStream, XmlExportAuditType.class);
    registerClass(xStream, XmlExportAuditEntry.class);
    registerClass(xStream, XmlExportComposite.class);
    registerClass(xStream, XmlExportField.class);
    registerClass(xStream, XmlExportGroup.class);
    registerClass(xStream, XmlExportGroupType.class);
    registerClass(xStream, XmlExportGroupTypeTuple.class);
    registerClass(xStream, XmlExportMember.class);
    registerClass(xStream, XmlExportMembership.class);
    registerClass(xStream, XmlExportStem.class);
    registerClass(xStream, XmlExportRoleSet.class);
    return xStream;
  }

  /**
   * 
   * @param writer 
   * @param typeId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringType(Writer writer, String typeId, boolean includeComma) throws IOException {
    writer.write("type: ");
    GroupType groupType = GroupTypeFinder.findByUuid(typeId, true);
    writer.write(groupType.getName());
    if (includeComma) {
      writer.write(", ");
    }
  }

  /**
   * @param prefix
   * @param writer 
   * @param memberId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringMember(String prefix, Writer writer, String memberId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("Member: ");
    } else {
      writer.write("member: ");
    }
    Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
    writer.write(member.getSubjectSourceId());
    writer.write(" - ");
    if ("g:gsa".equals(member.getSubjectSourceId())) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), member.getSubjectIdDb(), true);
      String groupName = group == null ? member.getSubjectIdDb() : group.getName();
      writer.write(groupName);
    } else {
      writer.write(member.getSubjectId());
    }
    if (includeComma) {
      writer.write(", ");
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param attributeDefId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringAttributeDef(String prefix, Writer writer, String attributeDefId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("AttributeDef: ");
    } else {
      writer.write("attributeDef: ");
    }
    AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, true);
    writer.write(attributeDef.getName());
    if (includeComma) {
      writer.write(", ");
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param attributeDefNameId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringAttributeDefName(String prefix, Writer writer, String attributeDefNameId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("AttributeDefName: ");
    } else {
      writer.write("attributeDefName: ");
    }
    AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
    writer.write(attributeDefName.getName());
    if (includeComma) {
      writer.write(", ");
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param auditTypeId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringAuditType(String prefix, Writer writer, String auditTypeId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("AuditType: ");
    } else {
      writer.write("auditType: ");
    }
    AuditType auditType = AuditTypeFinder.find(auditTypeId, true);
    writer.write(auditType.getAuditCategory());
    writer.write(" - ");
    writer.write(auditType.getActionName());
    if (includeComma) {
      writer.write(", ");
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param stemId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringStem(String prefix, Writer writer, String stemId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("Stem: ");
    } else {
      writer.write("stem: ");
    }
    Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, true);
    writer.write(stem.getName());
    if (includeComma) {
      writer.write(", ");
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param groupId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringGroup(String prefix, Writer writer, String groupId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("Group: ");
    } else {
      writer.write("group: ");
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    writer.write(group.getName());
    if (includeComma) {
      writer.write(", ");
    }
  }

  /**
   * @param prefix
   * @param writer 
   * @param attributeAssignId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringAttributeAssign(String prefix, Writer writer, String attributeAssignId, boolean includeComma) throws IOException {
    AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true);
    toStringAttributeAssign(prefix, writer, attributeAssign, includeComma);
  }

  /**
   * @param prefix
   * @param writer 
   * @param attributeAssign
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringAttributeAssign(String prefix, Writer writer, AttributeAssign attributeAssign, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("AttributeAssign: ");
    } else {
      writer.write("attributeAssign: ");
    }
    XmlExportUtils.toStringAttributeDefName(null, writer, attributeAssign.getAttributeDefNameId(), true);
    
    if (!StringUtils.isBlank(attributeAssign.getOwnerStemId())) {
      XmlExportUtils.toStringStem(null, writer, attributeAssign.getOwnerStemId(), false);
    }
    if (!StringUtils.isBlank(attributeAssign.getOwnerGroupId())) {
      XmlExportUtils.toStringGroup(null, writer, attributeAssign.getOwnerGroupId(), false);
    }
    if (!StringUtils.isBlank(attributeAssign.getOwnerMemberId())) {
      XmlExportUtils.toStringMember(null, writer, attributeAssign.getOwnerMemberId(), false);
    }
    if (!StringUtils.isBlank(attributeAssign.getOwnerMembershipId())) {
      XmlExportUtils.toStringMembership(null, writer, attributeAssign.getOwnerMembershipId(), false);
    }
    if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeDefId())) {
      XmlExportUtils.toStringAttributeDef(null, writer, attributeAssign.getOwnerAttributeDefId(), false);
    }
    if (!StringUtils.isBlank(attributeAssign.getOwnerAttributeAssignId())) {
      XmlExportUtils.toStringAttributeAssign("attrOn", writer, attributeAssign.getOwnerAttributeAssignId(), false);
    }

    if (includeComma) {
      writer.write(", ");
    }
  }

  
  /**
   * @param prefix
   * @param writer 
   * @param membershipId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringMembership(String prefix, Writer writer, String membershipId, boolean includeComma) throws IOException {
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
    toStringMembership(prefix, writer, membership, includeComma);
  }

  /**
   * @param prefix
   * @param writer 
   * @param membership
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringMembership(String prefix, Writer writer, Membership membership, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("Membership: ");
    } else {
      writer.write("membership: ");
    }
    if (!StringUtils.isBlank(membership.getOwnerGroupId())) {
      XmlExportUtils.toStringGroup(null, writer, membership.getOwnerGroupId(), true);
    }
    if (!StringUtils.isBlank(membership.getOwnerStemId())) {
      XmlExportUtils.toStringStem(null, writer, membership.getOwnerStemId(), true);
    }
    if (!StringUtils.isBlank(membership.getOwnerAttrDefId())) {
      XmlExportUtils.toStringAttributeDef(null, writer, membership.getOwnerAttrDefId(), true);
    }
    
    writer.write("field: ");
    writer.write(membership.getListName());
    writer.write(", ");
    XmlExportUtils.toStringMember(null, writer, membership.getMemberUuid(), false);

    if (includeComma) {
      writer.write(", ");
    }
  }
  
  
  /**
   * @param prefix
   * @param writer 
   * @param attributeAssignActionId
   * @param includeComma 
   * @throws IOException 
   * @return the action
   */
  public static AttributeAssignAction toStringAttributeAssignAction(String prefix, Writer writer, String attributeAssignActionId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("Action: ");
    } else {
      writer.write("action: ");
    }
    AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(attributeAssignActionId, true);
    writer.write(attributeAssignAction.getName());
    if (includeComma) {
      writer.write(", ");
    }
    return attributeAssignAction;
  }

  /**
   * @param prefix
   * @param writer 
   * @param roleId
   * @param includeComma 
   * @throws IOException 
   */
  public static void toStringRole(String prefix, Writer writer, String roleId, boolean includeComma) throws IOException {
    if (!StringUtils.isBlank(prefix)) {
      writer.write(prefix);
      writer.write("Role: ");
    } else {
      writer.write("role: ");
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), roleId, true);
    writer.write(group.getName());
    if (includeComma) {
      writer.write(", ");
    }
  }  
  
  /**
   * 
   * @param xStream
   * @param theClass
   */
  private static void registerClass(XStream xStream, Class<?> theClass) {
    xStream.alias(theClass.getSimpleName(), theClass);
  }

  /**
   * take a multiple assign record from xml and sync with db
   * @param xmlImportableMultiple
   * @param xmlImportMain
   */
  public static void syncImportableMultiple(XmlImportableMultiple xmlImportableMultiple, XmlImportMain xmlImportMain) {

    GrouperUtil.substituteStrings(xmlImportMain.getUuidTranslation(), xmlImportableMultiple);

    XmlImportableMultiple dbObject = (XmlImportableMultiple)xmlImportableMultiple.xmlRetrieveByIdOrKey(xmlImportMain.getIdsToIgnore());
    boolean insert = false;
    boolean update = false;

    //if not in db
    if (dbObject == null) {
      dbObject = (XmlImportableMultiple)xmlImportableMultiple.xmlSaveBusinessProperties(dbObject);
      insert = true;
    } else {
      //db is there, see if different
      if (!StringUtils.equals(xmlImportableMultiple.xmlGetId(), dbObject.xmlGetId())) {
        //this is a translation
        xmlImportMain.getUuidTranslation().put(xmlImportableMultiple.xmlGetId(), dbObject.xmlGetId());
        xmlImportableMultiple.xmlSetId(dbObject.xmlGetId());
      }
      if (xmlImportableMultiple.xmlDifferentBusinessProperties(dbObject)) {
        update = true;
        xmlImportableMultiple.xmlCopyBusinessPropertiesToExisting(dbObject);
        dbObject = (XmlImportableMultiple)xmlImportableMultiple.xmlSaveBusinessProperties(dbObject);
      }
    }
    //see if update properties need work
    if (xmlImportableMultiple.xmlDifferentUpdateProperties(dbObject)) {
      update = true;
      xmlImportableMultiple.xmlSaveUpdateProperties();
    }

    //dont increment insert and update, if insert, do that one, else update
    if (insert) {
      xmlImportMain.incrementInsertCount();
    } else if (update) {
      xmlImportMain.incrementUpdateCount();
    } else {
      xmlImportMain.incrementSkipCount();
    }
  }
}
