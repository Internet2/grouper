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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 * utils about xml export
 */
public class XmlExportUtils {

  /**
   * 
   */
  public static final String FILE_NAME_ARG = "fileName";

  /**
   * 
   * @param args
   * @return true if wants help
   */
  public static boolean internal_wantsHelp(String[] args) {
    if (
      args.length == 0
      || 
      "--h --? /h /? --help /help ${cmd}".indexOf(args[0]) > -1
    ) {
      return true;
    }
    return false;
  }

  /**
   * 
   * @param args
   * @return the map
   */
  public static Map<String, Object> internal_getXmlImportArgs(String args[]) {
    return internal_getXmlExportArgs(args);
  }
  
  /**
   * 
   * @param args
   * @return the map
   */
  public static Map<String, Object> internal_getXmlExportArgs(String[] args) {
    
    Map<String, Object> argsMap = new HashMap<String, Object>();
    String      arg;
    int         pos       = 0;
    while (pos < args.length) {
      arg = args[pos];
      if (arg.startsWith("-")) {
        String argName = arg.substring(1);
        if (StringUtils.equals(XmlExportGsh.STEMS_ARG, argName)) {
          argsMap.put(argName, args[pos+1]);
          pos++;
        } else if (StringUtils.equals(XmlExportGsh.OBJECT_NAMES_ARG, argName)) {
          argsMap.put(argName, args[pos+1]);
          pos++;
        } else {
          argsMap.put(argName, Boolean.TRUE);
        }
        pos++;
        continue;
      }
      //must be file name
      if (argsMap.containsKey(FILE_NAME_ARG)) {
        throw new RuntimeException("Enter only one filename: " + arg + ", " + argsMap.get(FILE_NAME_ARG));
      }
      argsMap.put(FILE_NAME_ARG, arg);
      pos++;
    }
    return argsMap;
  }
  
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
    if (dbObject != null && xmlImportable.xmlDifferentUpdateProperties(dbObject)) {
      update = true;
      xmlImportable.xmlSaveUpdateProperties();
    }

    //dont increment insert and update, if insert, do that one, else update
    if (insert) {
      xmlImportMain.incrementInsertCount();
      if (xmlImportMain.isRecordReport()) {
        xmlImportMain.readonlyWriteLogEntry("Insert: " + xmlImportable.xmlToString() + "\n");
      }
    } else if (update) {
      xmlImportMain.incrementUpdateCount();
      if (xmlImportMain.isRecordReport()) {
        xmlImportMain.readonlyWriteLogEntry("Update: " + xmlImportable.xmlToString() + "\n");
      }
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
   */
  public static void toStringType(Writer writer, String typeId, boolean includeComma) {
    try {
      writer.write("type: ");
      GroupType groupType = GroupTypeFinder.findByUuid(typeId, true);
      writer.write(groupType.getName());
      if (includeComma) {
        writer.write(", ");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with typeId: " + typeId, ioe);
    }
  }

  /**
   * 
   * @param writer 
   * @param fieldId
   * @param includeComma 
   */
  public static void toStringField(Writer writer, String fieldId, boolean includeComma) {
    try {
      writer.write("field: ");
      Field field = FieldFinder.findById(fieldId, true);
      writer.write(field.getName());
      if (includeComma) {
        writer.write(", ");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with fieldId: " + fieldId, ioe);
    }
  }

  /**
   * @param prefix
   * @param writer 
   * @param memberId
   * @param includeComma 
   */
  public static void toStringMember(String prefix, Writer writer, String memberId, boolean includeComma) {
    Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, true);
    toStringMember(prefix, writer, member, includeComma);
  }

  /**
   * @param prefix
   * @param writer 
   * @param member
   * @param includeComma 
   */
  public static void toStringMember(String prefix, Writer writer, Member member, boolean includeComma) {
    try {
      if (!StringUtils.isBlank(prefix)) {
        writer.write(prefix);
        writer.write("Member: ");
      } else {
        writer.write("member: ");
      }
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
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with member: " + member, ioe);
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param attributeDefId
   * @param includeComma 
   */
  public static void toStringAttributeDef(String prefix, Writer writer, String attributeDefId, boolean includeComma) {
    AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, true);
    toStringAttributeDef(prefix, writer, attributeDef, includeComma);
  }

  /**
   * 
   * @param writer
   * @param composite
   * @param includeComma
   */
  public static void toStringComposite(Writer writer, Composite composite, boolean includeComma) {
    XmlExportUtils.toStringGroup("owner", writer, composite.getFactorOwnerUuid(), true);
    XmlExportUtils.toStringGroup("left", writer, composite.getLeftFactorUuid(), true);
    XmlExportUtils.toStringGroup("right", writer, composite.getRightFactorUuid(), includeComma);
    
  }

  /**
   * 
   * @param writer
   * @param groupTypeTuple
   * @param includeComma
   */
  public static void toStringGroupTypeTuple(Writer writer, GroupTypeTuple groupTypeTuple, boolean includeComma) {
    XmlExportUtils.toStringGroup(null, writer, groupTypeTuple.getGroupUuid(), true);
    XmlExportUtils.toStringType(writer, groupTypeTuple.getTypeUuid(), includeComma);
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param attributeDef
   * @param includeComma 
   */
  public static void toStringAttributeDef(String prefix, Writer writer, AttributeDef attributeDef, boolean includeComma) {
    try {
      if (!StringUtils.isBlank(prefix)) {
        writer.write(prefix);
        writer.write("AttributeDef: ");
      } else {
        writer.write("attributeDef: ");
      }
      writer.write(attributeDef.getName());
      if (includeComma) {
        writer.write(", ");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with attributeDef: " + attributeDef, ioe);
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param attributeDefNameId
   * @param includeComma 
   */
  public static void toStringAttributeDefName(String prefix, Writer writer, String attributeDefNameId, boolean includeComma) {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
    toStringAttributeDefName(prefix, writer, attributeDefName, includeComma);
  }
  

  /**
   * 
   * @param writer
   * @param roleSet
   * @param includeComma
   */
  public static void toStringRoleSet(Writer writer, RoleSet roleSet, boolean includeComma) {
//    try {

    XmlExportUtils.toStringRole("ifHas", writer, roleSet.getIfHasRoleId(), true);
    XmlExportUtils.toStringRole("thenHas", writer, roleSet.getThenHasRoleId(), includeComma);

//    } catch (IOException ioe) {
//      throw new RuntimeException("Problem with attributeDefNameSet: " + attributeDefNameSet, ioe);
//    }
  }
  
  /**
   * 
   * @param writer
   * @param attributeDefNameSet
   * @param includeComma
   */
  public static void toStringAttributeDefNameSet(Writer writer, AttributeDefNameSet attributeDefNameSet, boolean includeComma) {
//    try {

    XmlExportUtils.toStringAttributeDefName("ifHas", writer, attributeDefNameSet.getIfHasAttributeDefNameId(), true);
    XmlExportUtils.toStringAttributeDefName("thenHas", writer, attributeDefNameSet.getThenHasAttributeDefNameId(), includeComma);

//    } catch (IOException ioe) {
//      throw new RuntimeException("Problem with attributeDefNameSet: " + attributeDefNameSet, ioe);
//    }
  }

  /**
   * @param prefix
   * @param writer 
   * @param attributeDefName
   * @param includeComma 
   */
  public static void toStringAttributeDefName(String prefix, Writer writer, AttributeDefName attributeDefName, boolean includeComma) {
    try {
      if (!StringUtils.isBlank(prefix)) {
        writer.write(prefix);
        writer.write("AttributeDefName: ");
      } else {
        writer.write("attributeDefName: ");
      }
      writer.write(attributeDefName.getName());
      if (includeComma) {
        writer.write(", ");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with attributeDefName: " + attributeDefName, ioe);
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param auditTypeId
   * @param includeComma 
   */
  public static void toStringAuditType(String prefix, Writer writer, String auditTypeId, boolean includeComma) {
    try {
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
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with auditTypeId: " + auditTypeId, ioe);
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param stemId
   * @param includeComma 
   */
  public static void toStringStem(String prefix, Writer writer, String stemId, boolean includeComma) {
    try {
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
    } catch(IOException ioe) {
      throw new RuntimeException("Problem with stemId: " + stemId, ioe);
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param groupId
   * @param includeComma 
   */
  public static void toStringGroup(String prefix, Writer writer, String groupId, boolean includeComma) {
    try {
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
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with groupId: " + groupId, ioe);
    }
  }

  /**
   * @param prefix
   * @param writer 
   * @param attributeAssignId
   * @param includeComma 
   */
  public static void toStringAttributeAssign(String prefix, Writer writer, String attributeAssignId, boolean includeComma) {
    AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true);
    toStringAttributeAssign(prefix, writer, attributeAssign, includeComma);
  }

  /**
   * @param prefix
   * @param writer 
   * @param attributeAssign
   * @param includeComma 
   */
  public static void toStringAttributeAssign(String prefix, Writer writer, AttributeAssign attributeAssign, boolean includeComma) {
    try {
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
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with: " + attributeAssign, ioe);
    }
  }

  
  /**
   * @param prefix
   * @param writer 
   * @param membershipId
   * @param includeComma 
   */
  public static void toStringMembership(String prefix, Writer writer, String membershipId, boolean includeComma) {
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
    toStringMembership(prefix, writer, membership, includeComma);
  }

  /**
   * convert attribute assign value to string
   * @param writer
   * @param attributeAssignValue
   * @param includeComma
   */
  public static void toStringAttributeAssignValue(Writer writer, AttributeAssignValue attributeAssignValue, boolean includeComma) {
    try {
      writer.write("value: ");
      
      if (attributeAssignValue.getValueInteger() != null) {
        writer.write(attributeAssignValue.getValueInteger().toString());
      } else if (!StringUtils.isBlank(attributeAssignValue.getValueMemberId())) {
        XmlExportUtils.toStringMember(null, writer, attributeAssignValue.getValueMemberId(), false);
      } else if (attributeAssignValue.getValueString() != null) {
        writer.write(attributeAssignValue.getValueString());
      } else {
        writer.write("null");
      }
      writer.write(", ");
      
      XmlExportUtils.toStringAttributeAssign(null, writer, attributeAssignValue.getAttributeAssignId(), false);

      if (includeComma) {
        writer.write(", ");
      }
      
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with assign value: " + attributeAssignValue, ioe);
    }
  }
  
  /**
   * 
   * @param writer
   * @param attributeAssignActionSet
   * @param includeComma
   */
  public static void toStringAttributeAssignActionSet(Writer writer, AttributeAssignActionSet attributeAssignActionSet, boolean includeComma) {
    try {
      AttributeAssignAction attributeAssignAction = XmlExportUtils
        .toStringAttributeAssignAction("ifHas", writer, 
            attributeAssignActionSet.getIfHasAttrAssignActionId(), true);
      XmlExportUtils
        .toStringAttributeAssignAction("thenHas", writer, 
          attributeAssignActionSet.getThenHasAttrAssignActionId(), true);
      XmlExportUtils
        .toStringAttributeDef(null, writer, 
          attributeAssignAction.getAttributeDefId(), false);

      if (includeComma) {
        writer.write(", ");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with attributeAssignActionSet: " + attributeAssignActionSet, ioe);
    }
  }
  
  /**
   * @param prefix
   * @param writer 
   * @param membership
   * @param includeComma 
   */
  public static void toStringMembership(String prefix, Writer writer, Membership membership, boolean includeComma) {
    try {
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
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with membership: " + membership, ioe);
    }
  }
  

  /**
   * @param prefix
   * @param writer 
   * @param attributeAssignActionId
   * @param includeComma 
   * @return the action
   */
  public static AttributeAssignAction toStringAttributeAssignAction(String prefix, Writer writer, 
      String attributeAssignActionId, boolean includeComma) {
    AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(attributeAssignActionId, true);
    toStringAttributeAssignAction(prefix, writer, attributeAssignAction, includeComma);
    return attributeAssignAction;
  }

  /**
   * @param prefix
   * @param writer 
   * @param attributeAssignAction
   * @param includeComma 
   */
  public static void toStringAttributeAssignAction(String prefix, Writer writer, 
      AttributeAssignAction attributeAssignAction, boolean includeComma) {
    try {
      if (!StringUtils.isBlank(prefix)) {
        writer.write(prefix);
        writer.write("Action: ");
      } else {
        writer.write("action: ");
      }
      writer.write(attributeAssignAction.getName());
      if (includeComma) {
        writer.write(", ");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with attributeAssignAction: " + attributeAssignAction, ioe);
    }
  }

  /**
   * @param prefix
   * @param writer 
   * @param roleId
   * @param includeComma 
   */
  public static void toStringRole(String prefix, Writer writer, String roleId, boolean includeComma) {
    try {
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
    } catch (IOException ioe) {
      throw new RuntimeException("Problem with roleId: " + roleId, ioe);
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
    if (dbObject != null && xmlImportableMultiple.xmlDifferentUpdateProperties(dbObject)) {
      update = true;
      xmlImportableMultiple.xmlSaveUpdateProperties();
    }

    //dont increment insert and update, if insert, do that one, else update
    if (insert) {
      xmlImportMain.incrementInsertCount();
      if (xmlImportMain.isRecordReport()) {
        xmlImportMain.readonlyWriteLogEntry("Insert: " + xmlImportableMultiple.xmlToString() + "\n");
      }
    } else if (update) {
      xmlImportMain.incrementUpdateCount();
      if (xmlImportMain.isRecordReport()) {
        xmlImportMain.readonlyWriteLogEntry("Update: " + xmlImportableMultiple.xmlToString() + "\n");
      }
    } else {
      xmlImportMain.incrementSkipCount();
    }
  }
}
