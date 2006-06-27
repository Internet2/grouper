/*
 Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
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
package edu.internet2.middleware.grouper;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.grouper.AccessPrivilege;
import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.NamingPrivilege;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Utility class for exporting data from the Grouper repository in XML format.
 * All or part of the repository can be exported as a stem/group hierarchy.
 * Alternatively, Collections of Stems, Groups, Subjects or Memberships may be
 * exported.
 * <p />
 * Exported stem and group data may be imported, as is, or with modifications,
 * into the same repository or into another repository. See XmlImporer.
 * 
 * 
 * @author Gary Brown.
 * @version $Id: XmlExporter.java,v 1.1 2006-06-27 17:04:45 blair Exp $
 */

public class XmlExporter {
  
  private static final Log log = LogFactory.getLog(XmlExporter.class);

  private Properties options;

  private GroupType baseType = null;

  private boolean isRelative;

  private boolean includeParent;

  private String fromStem = null;

  private int writeStemsCounter = 0;

  private Subject sysUser;

  public static void main(String args[]) throws Exception {

    if (args.length == 0
        || "--h --? /h /? --help /help ${cmd}".indexOf(args[0]) > -1) {
      XmlExporter.commandLineUsage();
      System.exit(0);
    }
    Log log = LogFactory.getLog(XmlExporter.class);
    String subjectIdentifier = null;
    String exportFile = null;
    String exportProperties = "export.properties";
    String userExportProperties = null;
    String id = null;
    String name = null;
    boolean relative = false;
    boolean includeParent = false;
    int pos = 0;
    int inputPos = 0;
    String arg;
    try {
      while (pos < args.length) {
        arg = args[pos];
        if (arg.startsWith("-")) {
          if (arg.equals("-id")) {
            if (name != null)
              throw new IllegalArgumentException(
                  "Cannot specify id and name");
            id = args[pos + 1];
            if (id.startsWith("-"))
              throw new IllegalArgumentException(
                  "id cannot start with -");
            pos += 2;
            continue;
          } else if (arg.equals("-name")) {
            if (id != null)
              throw new IllegalArgumentException(
                  "Cannot specify id and name");
            name = args[pos + 1];
            if (name.startsWith("-"))
              throw new IllegalArgumentException(
                  "name cannot start with -");
            pos += 2;
            continue;
          } else if (arg.equals("-relative")) {
            relative = true;
            pos++;
            continue;
          } else if (arg.equalsIgnoreCase("-includeparent")) {
            includeParent = true;
            pos++;
            continue;
          } else {
            throw new IllegalArgumentException(
                "Unrecognised option " + arg);
          }
        }
        switch (inputPos) {
        case 0:
          subjectIdentifier = arg;
          break;
        case 1:
          exportFile = arg;
          break;
        case 2:
          userExportProperties = arg;
          break;
        case 3:
          throw new IllegalArgumentException("Too many arguments - "
              + arg);
        }
        pos++;
        inputPos++;
      }
      if (inputPos < 1)
        throw new IllegalStateException("Too few arguments");
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println();
      XmlExporter.commandLineUsage();
      System.exit(1);
    }
    Properties props = new Properties();
    if (userExportProperties != null) {
      log.info("Loading user-specified properties [" + userExportProperties
          + "]");
      props.load(new FileInputStream(userExportProperties));
    } else {
      log.info("Loading default properties [" + exportProperties + "]");
      try {
        props.load(new FileInputStream(exportProperties));
      } catch (Exception e) {
        log.info("Failed to find [" + exportProperties
            + "] in working directory, trying classpath");
        InputStream is = XmlExporter.class.getResourceAsStream(exportProperties);
        props.load(is);
      }
    }

    XmlExporter exporter = new XmlExporter(props);
    Subject user = SubjectFinder.findByIdentifier(subjectIdentifier);
    GrouperSession s = GrouperSession.start(user);
    FileWriter fw = new FileWriter(exportFile);
    PrintWriter writer = new PrintWriter(fw);
    if (id == null && name == null) {
      exporter.export(s, writer);
    } else {
      Group group = null;
      Stem stem = null;
      if (id != null) {
        try {
          group = GroupFinder.findByUuid(s, id);
          log.debug("Found group with id [" + id + "]");
        } catch (GroupNotFoundException e) {
        }
        if (group == null) {
          try {
            stem = StemFinder.findByUuid(s, id);
            log.debug("Found stem with id [" + id + "]");
          } catch (StemNotFoundException e) {
          }
        }
        if (group == null && stem == null) {
          throw new IllegalArgumentException(
              "Could not find group or stem with id [" + id + "]");
        }
      } else {
        try {
          group = GroupFinder.findByName(s, name);
          log.debug("Found group with name [" + name + "]");
        } catch (GroupNotFoundException e) {
        }
        if (group == null) {
          try {
            stem = StemFinder.findByName(s, name);
            log.debug("Found stem with name [" + name + "]");
          } catch (StemNotFoundException e) {
          }
        }
      }
      if (group == null && stem == null) {
        if (name != null)
          throw new IllegalArgumentException(
              "Could not find group or stem with name [" + name
                  + "]");
        throw new IllegalArgumentException(
            "Could not find group or stem with id [" + id + "]");
      }
      if (group != null) {
        exporter.export(s, group, relative, writer);
      } else {
        exporter.export(s, stem, relative, includeParent, writer);
      }
    }
    log.info("Finished export to [" + exportFile + "]");
    s.stop();
  }

  /**
   * The export process is configured using the following properties: <table
   * width="100%" border="1">
   * <tr bgcolor="#CCCCCC">
   * <td width="28%"><font face="Arial, Helvetica, sans-serif">Key </font>
   * </td>
   * <td width="8%"><font face="Arial, Helvetica, sans-serif">values </font>
   * </td>
   * <td width="64%"><font face="Arial, Helvetica, sans-serif">Description
   * </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.metadata </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether
   * information about the group types and fields available in this Grouper
   * instance should be exported, as well as the Subject sources. </font>
   * </td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.data </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines if actual data
   * is exported </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.privs.naming
   * </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines if naming
   * privilege information is exported with stems </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.privs.access
   * </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines if access
   * privilege information is exported with groups </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.privs.immediate-only </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether all
   * privilegees are exported or only those to which privileges have been
   * granted directly </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.group.members
   * </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether or not
   * group membership information is exported </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.group.members.immediate-only </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether all
   * members are exported, or only direct members </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.group.lists </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether custom
   * list attributes are exported </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.group.lists.immediate-only </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether all
   * list members are exported, or only direct members </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.group.internal-attributes </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether Grouper
   * maintained attributes e.g. modifyDate are exported </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.group.custom-attributes </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether custom
   * attributes are exported </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">
   * export.stem.internal-attributes <br>
   * </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether Grouper
   * maintained attributes e.g. modifyDate are exported </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">export.privs.for-parents
   * </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">If exporting part of the
   * hierarchy it is possible to export parent stems. This property determines
   * if privileges are exported for parent stems </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.subject-attributes.source.&lt;source
   * name&gt;.&lt;subject type&gt; </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Space separated list of
   * attribute names </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Specifies any attributes
   * that should be exported with a Subject given the source and subject type
   * </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.subject-attributes.source.&lt;source name&gt; </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">Space separated list of
   * attribute names </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Specifies any attributes
   * that should be exported with a Subject given the source regardless of th
   * esubject type </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">export.subject-attributes.type.&lt;subject type&gt; </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">Space separated list of
   * attribute names </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Specifies any attributes
   * that should be exported with a Subject given the subject type regardless
   * of the source </font></td>
   * </tr>
   * </table>
   * 
   * @param options
   * @throws Exception
   */
  public XmlExporter(Properties options) throws Exception {
    this.options = options;
    this.baseType = GroupTypeFinder.find("base");
    sysUser = SubjectFinder.findById("GrouperSystem");
  }

  /**
   * Exports data for the entire repository
   * 
   * @param s
   * @param writer
   * @throws Exception
   */
  public void export(GrouperSession s, PrintWriter writer) throws Exception {
    Stem stem = StemFinder.findRootStem(s);
    GroupOrStem gos = findByStem(s, stem);
    log.info("Start export of entire repository");
    export(s, gos, true, false, writer);
    log.info("Finished export of entire repository");
  }

  /**
   * Exports part of the repository
   * 
   * @param s
   * @param stem -
   *            where to export from
   * @param relative -
   *            determines whether to export parent stems
   * @param includeParent -
   *            should 'stem' be included or just the children
   * @param writer
   * @throws Exception
   */
  public void export(GrouperSession s, Stem stem, boolean relative,
      boolean includeParent, PrintWriter writer) throws Exception {
    GroupOrStem gos = findByStem(s, stem);
    log.info("Start export of Stem " + stem.getName());
    export(s, gos, relative, includeParent, writer);
    log.info("Finished export of Stem " + stem.getName());
  }

  /**
   * Export a single group
   * 
   * @param s
   * @param group
   * @param relative -
   *            determines whether to export parent stems
   * @param writer
   * @throws Exception
   */
  public void export(GrouperSession s, Group group, boolean relative,
      PrintWriter writer) throws Exception {
    GroupOrStem gos = findByGroup(s, group);
    log.info("Start export of Group " + group.getName());
    export(s, gos, relative, false, writer);
    log.info("Finished export of Group " + group.getName());
  }

  /**
   * Export a Collection of stems, groups, members, subjects or memberships
   * 
   * @param s
   * @param items
   * @param info -
   *            allows you to indicate how the Collection was generated
   * @param writer
   * @throws Exception
   */
  public synchronized void export(GrouperSession s, Collection items,
      String info, PrintWriter writer) throws Exception {
    log.info("Start export of Collection:" + info);

    String padding = "    ";
    String origPadding = "  ";
    fromStem = "_Z";
    Date before = writeHeader(writer);

    int counter = 0;

    if (optionTrue("export.data")) {
      Iterator itemsIterator = items.iterator();
      writer.print(origPadding);
      writer.println("<dataList>");
      Object obj;
      while (itemsIterator.hasNext()) {
        obj = itemsIterator.next();
        counter++;
        if (obj instanceof Group) {
          writeFullGroup(s, (Group) obj, writer, padding);
        } else if (obj instanceof Stem) {
          Stem stem = (Stem) obj;
          writeBasicStemHeader(s, stem, writer, padding);
          writeInternalAttributes(s, stem, writer, padding);
          writeStemPrivs(s, stem, writer, padding);
          writeBasicStemFooter(s, stem, writer, padding);
        } else if (obj instanceof Subject) {
          if (counter == 1)
            writer.println("<exportOnly/>");
          writeSubject(s, (Subject) obj, writer, padding);
        } else if (obj instanceof Member) {
          if (counter == 1)
            writer.println("<exportOnly/>");
          writeSubject(s, ((Member) obj).getSubject(), writer,
              padding);
        } else if (obj instanceof Membership) {
          if (counter == 1)
            writer.println("<exportOnly/>");
          writeMembership(s, (Membership) obj, writer, padding);
        } else {
          log.error("Don't know about exporting " + obj);
        }
        writer.println();
      }
      writer.print(origPadding);
      writer.println("</dataList>");
    }
    writer.print(origPadding);
    writer.println("<exportComments><![CDATA[");
    writer.print(origPadding);
    writer.println(info);
    writer.print(origPadding);
    writer.println("]]></exportComments>");
    writeFooter(writer, before);
    log.info("Finished export of Collection:" + info);
  }

  /**
   * @return Returns the options.
   */
  public Properties getOptions() {
    return options;
  }

  /**
   * @param options
   *            The options to set.
   */
  public void setOptions(Properties options) {
    this.options = options;
  }

  private synchronized void export(GrouperSession s, GroupOrStem groupOrStem,
      boolean relative, boolean includeParent, PrintWriter writer)
      throws Exception {
    log.info("Relative export=" + relative);
    log.info("Include parent stem=" + includeParent);
    String padding = "  ";
    Date before = writeHeader(writer);
    this.isRelative = relative;
    this.includeParent = includeParent;
    writeStemsCounter = 0;
    if (!relative)
      fromStem = null;
    if (relative) {
      Stem dummyStem = null;
      if (includeParent || groupOrStem.isGroup()) {
        if (groupOrStem.isGroup()) {
          dummyStem = groupOrStem.getGroup().getParentStem();
        } else {
          dummyStem = groupOrStem.getStem().getParentStem();
        }
      } else if (!includeParent) {
        dummyStem = groupOrStem.getStem();
      }
      fromStem = dummyStem.getName() + ":";
    }

    if (optionTrue("export.data")) {
      exportData(s, groupOrStem, padding, writer);
    } else {
      log.info("export.data=false, so no data exported");
    }
    writeExportParams(groupOrStem, writer, padding);
    writeFooter(writer, before);
  }

  private synchronized Date writeHeader(PrintWriter writer) throws Exception {
    log.debug("Writing XML header");
    Date before = new Date();
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.println("<registry>");
    String padding = "  ";
    if (optionTrue("export.metadata")) {
      writeMetaData(writer, padding);
    }
    return before;
  }

  private synchronized void writeFooter(PrintWriter writer, Date before)
      throws Exception {
    log.debug("Writing XML Footer");
    String padding = "  ";
    Date now = new Date();
    long duration = (now.getTime() - before.getTime()) / 1000;
    writer.println();
    writer.print(padding);
    writer.println("<exportInfo>");
    writer.print(padding + "  ");
    writer.println("<start>" + before + "</start>");
    writer.print(padding + "  ");
    writer.println("<end>" + now + "</end>");
    writer.print(padding + "  ");
    writer.println("<duration>" + duration + "</duration>");
    writeOptions(writer, padding + "  ");
    writer.print(padding);
    writer.println("</exportInfo>");
    writer.println("</registry>");
    writer.close();
  }

  private void exportData(GrouperSession s, GroupOrStem groupOrStem,
      String padding, PrintWriter writer) throws Exception {
    log.debug("Writing repository data as XML");
    writer.println(padding + "<data>");
    Stack stems = null;
    if (!isRelative) {
      stems = getParentStems(groupOrStem);
    } else {
      stems = new Stack();
      if (groupOrStem.isGroup()) {
        stems.push(groupOrStem.getGroup());
        if (includeParent) {
          stems.push(groupOrStem.getGroup().getParentStem());
        }
      } else {
        stems.push(groupOrStem.getStem());
      }
    }
    writeStems(s, stems, writer, padding + "  ");
    writer.println(padding + "</data>");
    log.debug("Finished repository data as XML");
  }

  private void writeExportParams(GroupOrStem groupOrStem, PrintWriter writer,
      String padding) {
    log.debug("Writing export params to XML");
    writer.print(padding);
    writer.println("<exportParams>");
    writer.print(padding + "  ");
    writer.print("<node type='" + groupOrStem.getType() + "'>");
    writer.print(groupOrStem.getName());
    writer.println("</node>");
    writer.print(padding + "  ");
    writer.println("<relative>" + isRelative + "</relative>");
    if (groupOrStem.isStem()) {
      writer.print(padding + "  ");
      writer.println("<includeParent>" + includeParent
          + "</includeParent>");
    }
    writer.print(padding);
    writer.println("</exportParams>");
  }

  private void writeOptions(PrintWriter writer, String padding) {
    log.debug("Writing export options as XML");
    writer.print(padding);
    writer.println("<options>");
    List orderedList = new ArrayList(options.keySet());
    Collections.sort(orderedList);
    Iterator optionsIterator = orderedList.iterator();

    String key;
    while (optionsIterator.hasNext()) {
      key = (String) optionsIterator.next();
      writer.print(padding + "  ");
      writer.println("<option key='" + key + "'>"
          + options.getProperty(key) + "</option>");
    }
    writer.print(padding);
    writer.println("</options>");
  }

  private Stack getParentStems(GroupOrStem gos) throws Exception {
    Stack stems = new Stack();
    Stem startStem = null;
    if (gos.isGroup()) {
      Group group = gos.getGroup();
      stems.push(group);
      startStem = group.getParentStem();
    } else {
      startStem = gos.getStem();
    }
    stems.push(startStem);
    Stem parent = startStem;
    do {
      try {
        parent = parent.getParentStem();
        if (isEmpty(parent.getExtension())) {
          parent = null;
        } else {
          stems.push(parent);
        }
      } catch (StemNotFoundException e) {
        parent = null;
      }
    } while (parent != null);
    return stems;
  }

  private void writeMetaData(PrintWriter writer, String padding)
      throws Exception {
    log.debug("Writing repository metadata as XML");
    writer.print(padding);
    writer.println("<metadata>");
    writeGroupTypesMetaData(writer, padding + "  ");
    writeSubjectSourceMetaData(writer, padding + "  ");
    writer.print(padding);
    writer.println("</metadata>");
  }

  private void writeGroupTypesMetaData(PrintWriter writer, String padding)
      throws Exception {
    Set groupTypes = GroupTypeFinder.findAll();
    if (groupTypes.isEmpty())
      return;
    writer.print(padding);
    writer.println("<groupTypesMetaData>");
    String origPadding = padding;
    padding = padding + "  ";
    Iterator groupTypesIterator = groupTypes.iterator();
    GroupType groupType;
    Set fields;
    Iterator fieldsIterator;
    Field field;
    while (groupTypesIterator.hasNext()) {
      groupType = (GroupType) groupTypesIterator.next();
      writer.println();
      writer.print(padding);
      writer.println("<groupTypeDef name='"
          + fixXmlAttribute(groupType.getName()) + "'>");
      fields = groupType.getFields();
      fieldsIterator = fields.iterator();
      while (fieldsIterator.hasNext()) {
        field = (Field) fieldsIterator.next();
        writeFieldMetaData(field, writer, padding + " ");
      }
      writer.println();
      writer.print(padding);
      writer.println("</groupTypeDef>");
    }

    writer.print(padding);
    writer.println("</groupTypesMetaData>");
    writer.println();
  }

  private void writeSubjectSourceMetaData(PrintWriter writer, String padding)
      throws Exception {
    writer.println();
    writer.print(padding);
    writer.println("<subjectSourceMetaData>");
    String origPadding = padding;
    padding = padding + "  ";

    Collection sources = SourceManager.getInstance().getSources();
    Iterator sourcesIterator = sources.iterator();
    Source source;
    Set subjectTypes;
    SubjectType subjectType;
    Iterator subjectTypesIterator;
    while (sourcesIterator.hasNext()) {
      source = (Source) sourcesIterator.next();
      writer.println();
      writer.print(padding);
      writer.println("<source id='" + fixXmlAttribute(source.getId())
          + "'");
      writer.println(padding + "        name='" + source.getName() + "'");
      writer.println(padding + "        class='"
          + source.getClass().getName() + "'>");
      subjectTypes = source.getSubjectTypes();
      subjectTypesIterator = subjectTypes.iterator();
      while (subjectTypesIterator.hasNext()) {
        subjectType = (SubjectType) subjectTypesIterator.next();
        writer.print(padding + "  ");
        writer.println("<subjectType name='" + subjectType.getName()
            + "'/>");
      }

      writer.print(padding);
      writer.println("</source>");

    }
    writer.println();
    writer.print(origPadding);
    writer.println("</subjectSourceMetaData>");
    writer.println();
  }

  private void writeFieldMetaData(Field field, PrintWriter writer,
      String padding) throws Exception {
    writer.println();
    writer.print(padding);
    writer
        .println("<field name='" + fixXmlAttribute(field.getName())
            + "'");
    writer.println(padding + "        required='" + field.getRequired()
        + "'");
    writer.println(padding + "        type='" + field.getType() + "'");
    writer.println(padding + "        readPriv='" + field.getReadPriv()
        + "'");
    writer.println(padding + "        writePriv='" + field.getWritePriv()
        + "'/>");
  }

  private void writeStems(GrouperSession s, Stack stems, PrintWriter writer,
      String padding) throws Exception {
    writeStemsCounter++;
    Object obj = stems.pop();
    if (obj instanceof Group) {
      writeFullGroup(s, (Group) obj, writer, padding);
      return;
    }

    Stem stem = (Stem) obj;

    if (stems.isEmpty()) {
      if (includeParent || writeStemsCounter > 1) {
        writeFullStem(s, stem, writer, padding + "  ");
      } else {
        writeStemBody(s, stem, writer, padding + "  ");
      }
      return;
    } else {
      writeBasicStemHeader(s, stem, writer, padding);
      writeStemPrivs(s, stem, writer, padding);
      writeStems(s, stems, writer, padding + "  ");
      writeBasicStemFooter(s, stem, writer, padding);
    }
  }

  private void writeFullStem(GrouperSession s, Stem stem, PrintWriter writer,
      String padding) throws Exception {
    if (log.isDebugEnabled())
      log.debug("Writing Stem " + stem.getName() + " to XML");
    writeBasicStemHeader(s, stem, writer, padding);
    writeInternalAttributes(s, stem, writer, padding);
    writeStemPrivs(s, stem, writer, padding);
    writeStemBody(s, stem, writer, padding + "  ");
    writeBasicStemFooter(s, stem, writer, padding);
    if (log.isDebugEnabled())
      log.debug("Finished writing Stem " + stem.getName() + " to XML");
  }

  private void writeBasicStemHeader(GrouperSession s, Stem stem,
      PrintWriter writer, String padding) throws Exception {
    writer.println();
    writer.print(padding);
    writer.println("<!--" + stem.getName() + "-->");
    writer.print(padding);
    writer.println("<stem extension='"
        + fixXmlAttribute(stem.getExtension()) + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("displayExtension='"
        + fixXmlAttribute(stem.getDisplayExtension()) + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("name='" + fixXmlAttribute(stem.getName()) + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("displayName='" + fixXmlAttribute(stem.getDisplayName())
        + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("id='" + fixXmlAttribute(stem.getUuid()) + "'>");

    writer.print(padding + "  ");
    writer.println("<description>" + fixXmlAttribute(stem.getDescription())
        + "</description>");

  }

  private void writeStemPrivs(GrouperSession s, Stem stem,
      PrintWriter writer, String padding) throws Exception {
    if (optionTrue("export.privs.naming")
        && optionTrue("export.privs.for-parents")) {
      if (log.isDebugEnabled())
        log.debug("Writing STEM privilegees for " + stem.getName());
      writePrivileges(s, "stem", stem.getStemmers(), stem, writer,
          padding);
      writePrivileges(s, "create", stem.getStemmers(), stem, writer,
          padding);
      if (log.isDebugEnabled())
        log.debug("Writing CREATE privilegees for " + stem.getName());
    } else {
      if (log.isDebugEnabled())
        log.debug("Skipping naming privs for " + stem.getName());
    }
  }

  private void writeBasicStemFooter(GrouperSession s, Stem stem,
      PrintWriter writer, String padding) throws Exception {
    writer.println(padding + "</stem>");
    writer.println(padding + "<!--/" + stem.getName() + "-->");
    writer.println();
  }

  private void writeStemBody(GrouperSession s, Stem stem, PrintWriter writer,
      String padding) throws Exception {
    Set stems = stem.getChildStems();
    Iterator stemsIterator = stems.iterator();
    Stem childStem;
    while (stemsIterator.hasNext()) {
      childStem = (Stem) stemsIterator.next();
      writeFullStem(s, childStem, writer, padding);
    }

    Set groups = stem.getChildGroups();
    Iterator groupsIterator = groups.iterator();
    Group childGroup;
    while (groupsIterator.hasNext()) {
      childGroup = (Group) groupsIterator.next();
      writeFullGroup(s, childGroup, writer, padding);
    }
  }

  private void writeFullGroup(GrouperSession s, Group group,
      PrintWriter writer, String padding) throws Exception {
    if (log.isDebugEnabled())
      log.debug("Writing group " + group.getName() + " to XML");
    writer.println();
    writer.print(padding);
    try {
      writer.println("<!--" + group.getName() + "-->");
    } catch (Exception e) {
    }
    writer.print(padding);
    writer.println("<group extension='"
        + fixXmlAttribute(group.getExtension()) + "'");
    writer.print(padding);
    writer.print("       ");
    writer.println("displayExtension='"
        + fixXmlAttribute(group.getDisplayExtension()) + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("name='" + fixXmlAttribute(group.getName()) + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("displayName='"
        + fixXmlAttribute(group.getDisplayName()) + "'");
    writer.print(padding);
    writer.print("      ");
    writer.println("id='" + fixXmlAttribute(group.getUuid()) + "'>");

    writer.print(padding + "  ");
    writer.println("<description>"
        + fixXmlAttribute(group.getDescription()) + "</description>");

    if (optionTrue("export.group.internal-attributes")) {
      writeInternalAttributes(s, group, writer, padding + "  ");
    }
    if (optionTrue("export.group.custom-attributes")) {
      Set types = group.getTypes();
      GroupType baseType = GroupTypeFinder.find("base");
      types.remove(baseType);
      if (!types.isEmpty()) {
        writer.println(padding + "  <groupTypes>");
        Iterator typesIterator = types.iterator();
        GroupType groupType;
        while (typesIterator.hasNext()) {
          groupType = (GroupType) typesIterator.next();
          writeGroupType(s, group, groupType, writer, padding
              + "    ");

        }

        writer.println(padding + "  </groupTypes>");
      }
    }

    List listFields = new ArrayList();
    if (optionTrue("export.group.lists")) {
      listFields.addAll(getListFieldsForGroup(s, group));
    }
    if (optionTrue("export.group.members")) {
      listFields.add(0, "members");
    }

    for (int i = 0; i < listFields.size(); i++) {
      if (log.isDebugEnabled()) {
        log.debug("Writing list members for " + group.getName()
            + ": field=" + listFields.get(i));
      }
      writeListField(s, group, FieldFinder.find((String) listFields
          .get(i)), writer, padding + "  ");
    }
    if (optionTrue("export.privs.access")) {

      writePrivileges(s, "admin", group.getAdmins(), group, writer,
          padding);
      writePrivileges(s, "update", group.getUpdaters(), group, writer,
          padding);
      writePrivileges(s, "read", group.getReaders(), group, writer,
          padding);
      writePrivileges(s, "view", group.getViewers(), group, writer,
          padding);
      writePrivileges(s, "optin", group.getOptins(), group, writer,
          padding);
      writePrivileges(s, "optout", group.getOptouts(), group, writer,
          padding);
    }
    writer.println();
    writer.println(padding + "</group>");
    try {
      writer.println(padding + "<!--/" + group.getName() + "-->");
    } catch (Exception e) {
    }
    writer.println();
    if (log.isDebugEnabled())
      log.debug("Finished writing group " + group.getName() + " to XML");
  }

  private void writeGroupType(GrouperSession s, Group group,
      GroupType groupType, PrintWriter writer, String padding)
      throws Exception {
    writer.println(padding + "<groupType name='"
        + fixXmlAttribute(groupType.getName()) + "'>");
    Set fields = groupType.getFields();
    Iterator fieldsIterator = fields.iterator();
    Field field;
    String value;
    padding = padding + "  ";
    while (fieldsIterator.hasNext()) {
      field = (Field) fieldsIterator.next();
      if (field.getType().equals(FieldType.LIST))
        continue;
      if (!group.canReadField(field))
        continue;
      try {
        value = fixXmlAttribute(group.getAttribute(field.getName()));
        if (!isEmpty(value)
            && ":description:extension:displayExtension:"
                .indexOf(":" + field.getName() + ":") == -1) {
          writer.print(padding);
          writer.println("<attribute name='"
              + fixXmlAttribute(field.getName()) + "'>" + value
              + "</attribute>");
        }
      } catch (Exception e) {
      }

    }

    writer.println(padding + "</groupType>");
  }

  private void writeInternalAttributes(GrouperSession s, Group group,
      PrintWriter writer, String padding) throws Exception {
    writer.println();
    writer.println(padding + "<internalAttributes>");
    writer.println(padding + "  <internalAttribute name='parentStem'>"
        + fixXmlAttribute(group.getParentStem().getName())
        + "</internalAttribute>");
    writer.println(padding + "  <internalAttribute name='createSource'>"
        + fixXmlAttribute(group.getCreateSource())
        + "</internalAttribute>");
    writer.println(padding + "  <internalAttribute name='createSubject'>");
    writeSubject(s, group.getCreateSubject(), writer, padding + "    ");
    writer.println(padding + "  </internalAttribute>");
    writer.println(padding + "  <internalAttribute name='createTime'>"
        + fixXmlAttribute(group.getCreateTime().toString())
        + "</internalAttribute>");

    writer.println(padding + "  <internalAttribute name='modifySource'>"
        + fixXmlAttribute(group.getModifySource())
        + "</internalAttribute>");
    writer.println(padding + "  <internalAttribute name='modifySubject'>");
    writeSubject(s, group.getModifySubject(), writer, padding + "    ");
    writer.println(padding + "  </internalAttribute>");
    writer.println(padding + "  <internalAttribute name='modifyTime'>"
        + fixXmlAttribute(group.getModifyTime().toString())
        + "</internalAttribute>");

    writer.println(padding + "</internalAttributes>");
    writer.println();
  }

  private void writeInternalAttributes(GrouperSession s, Stem stem,
      PrintWriter writer, String padding) throws Exception {
    if (!optionTrue("export.stem.internal-attributes"))
      return;
    writer.println();
    writer.println(padding + "<internalAttributes>");
    writer.println(padding + "  <internalAttribute name='parentStem'>"
        + fixXmlAttribute(stem.getParentStem().getName())
        + "</internalAttribute>");
    writer.println(padding + "  <internalAttribute name='createSource'>"
        + fixXmlAttribute(stem.getCreateSource())
        + "</internalAttribute>");
    writer.println(padding + "  <internalAttribute name='createSubject'>");
    writeSubject(s, stem.getCreateSubject(), writer, padding + "    ");
    writer.println(padding + "  </internalAttribute>");
    writer.println(padding + "  <internalAttribute name='createTime'>"
        + fixXmlAttribute(stem.getCreateTime().toString())
        + "</internalAttribute>");

    writer.println(padding + "  <internalAttribute name='modifySource'>"
        + fixXmlAttribute(stem.getModifySource())
        + "</internalAttribute>");
    writer.println(padding + "  <internalAttribute name='modifySubject'>");
    writeSubject(s, stem.getModifySubject(), writer, padding + "    ");
    writer.println(padding + "  </internalAttribute>");
    writer.println(padding + "  <internalAttribute name='modifyTime'>"
        + fixXmlAttribute(stem.getModifyTime().toString())
        + "</internalAttribute>");

    writer.println(padding + "</internalAttributes>");
    writer.println();
  }

  private void writeListField(GrouperSession s, Group group, Field field,
      PrintWriter writer, String padding) throws Exception {
    if (!group.canReadField(field)) {
      log.info("No read privilege. List [" + field.getName() + "] for ["
          + group.getName() + "] ignored");
      return;
    }
    Set membersSet = null;
    boolean isComposite = false;
    if ("members".equals(field.getName()) && group.hasComposite()) {
      isComposite = true;
      membersSet = new HashSet();
    } else {
      membersSet = group.getImmediateMemberships(field);
    }
    Collection members = new ArrayList();
    members.addAll(membersSet);
    if (("members".equals(field.getName()) && !optionTrue("export.group.members.immediate-only"))
        || (!"members".equals(field.getName()) && !optionTrue("export.group.lists.immediate-only"))) {
      members.addAll(group.getEffectiveMemberships(field));
      if ("members".equals(field.getName()) && group.hasComposite()) {
        members.addAll(group.getCompositeMemberships());
      }
    }

    if (members.isEmpty() && !isComposite)
      return;
    writer.println();
    writer.print(padding);
    writer.println("<list field='" + fixXmlAttribute(field.getName())
        + "'  groupType='"
        + fixXmlAttribute(field.getGroupType().getName()) + "'>");
    if (isComposite) {
      Composite composite = CompositeFinder.findAsOwner(group);
      writeComposite(s, composite, writer, padding + "  ");
    }
    writeMembers(s, members, group, field, writer, padding + "  ");
    writer.print(padding);
    writer.println("</list> <!--/field=" + fixXmlAttribute(field.getName())
        + "-->");
  }

  private void writeComposite(GrouperSession s, Composite comp,
      PrintWriter writer, String padding) throws Exception {
    String nPadding = padding + "  ";
    writer.print(padding);
    writer.println("<composite>");
    writeGroupRef(s, comp.getLeftGroup(), writer, nPadding);
    writer.println();
    writer.print(nPadding);
    writer.println("<compositeType>" + comp.getType().toString()
        + "</compositeType>");
    writer.println();
    writeGroupRef(s, comp.getRightGroup(), writer, nPadding);
    writer.print(padding);
    writer.println("</composite>");
  }

  private void writeMembers(GrouperSession s, Collection members,
      Group group, Field field, PrintWriter writer, String padding)
      throws Exception {
    Iterator it = members.iterator();
    Membership member;
    Subject subj;
    boolean isImmediate;
    while (it.hasNext()) {
      isImmediate = false;
      member = (Membership) it.next();
      try {
        isImmediate = member.getViaGroup() == null;
      } catch (GroupNotFoundException e) {
        if (!group.hasComposite())
          isImmediate = true;
      }
      subj = member.getMember().getSubject();
      writeSubject(s, subj, " immediate='" + isImmediate + "' ", writer,
          padding);
    }
  }

  private void writeGroupRef(GrouperSession s, Group group,
      PrintWriter writer, String padding) {
    writeGroupRef(s, group, writer, padding, false);

  }

  private void writeGroupRef(GrouperSession s, Group group,
      PrintWriter writer, String padding, boolean writeAbsoluteName) {
    writer.print(padding);
    writer.println("<groupRef id='" + group.getUuid() + "'");
    writer.print(padding);
    String name = group.getName();
    if (!writeAbsoluteName) {
      name = fixGroupName(name);
    }
    writer.println("        name='" + name + "'");
    writer.print(padding);
    writer.println(" displayName='" + group.getDisplayName() + "'/>");
  }

  private void writeMembership(GrouperSession s, Membership membership,
      PrintWriter writer, String padding) throws Exception {
    boolean isImmediate = true;
    try {
      membership.getViaGroup();
      isImmediate = false;
    } catch (Exception e) {

    }

    String exPadding = padding + "  ";
    writer.print(padding);
    writer.println("<membership>");
    writer.print(exPadding);
    writer.println("<depth>" + membership.getDepth() + "</depth>");
    writer.print(exPadding);
    writer.println("<listName>" + membership.getList().getName()
        + "</listName>");
    writer.print(exPadding);
    writer.println("<immediate>" + isImmediate + "</immediate>");
    writeGroupRef(s, membership.getGroup(), writer, exPadding, true);
    writeSubject(s, membership.getMember().getSubject(), writer, exPadding);
    writer.print(padding);
    writer.println("</membership>");
  }

  private void writeSubject(GrouperSession s, Subject subj,
      PrintWriter writer, String padding) throws Exception {
    writeSubject(s, subj, "", writer, padding);
  }

  private String fixGroupName(String name) {
    if (fromStem != null && name.startsWith(fromStem)) {
      name = name.replaceAll("^" + fromStem, "*");
    }
    return name;
  }

  private void writeSubject(GrouperSession s, Subject subj, String immediate,
      PrintWriter writer, String padding) throws Exception {
    writer.print(padding);
    String id = null;
    String attrName = "id";
    if ("group".equals(subj.getType().getName())) {
      id = fixGroupName(subj.getName());
      attrName = "identifier";
    } else {
      id = subj.getId();
    }
    writer.print("<subject " + attrName + "='" + fixXmlAttribute(id)
        + "' type='" + fixXmlAttribute(subj.getType().getName())
        + "' source='" + fixXmlAttribute(subj.getSource().getId())
        + "'" + immediate);
    if ("group".equals(subj.getType().getName())) {
      writer.print(" id='" + subj.getId() + "'");
    }
    Iterator exportAttrs = getExportAttributes(subj);
    if (isEmpty(exportAttrs)) {
      writer.println("/>");
      return;
    }
    writer.println(">");
    String attr;
    Set values;
    Iterator attrIt;
    String attrValue;
    String attrPadding = padding + "  ";
    while (exportAttrs.hasNext()) {
      attr = (String) exportAttrs.next();
      try {
        values = subj.getAttributeValues(attr);
        writer.print(attrPadding);
        writer.println("<subjectAttribute name='" + attr + "'>");
        attrIt = values.iterator();
        while (attrIt.hasNext()) {
          attrValue = (String) attrIt.next();
          writer.print(attrPadding + "  ");
          writer.println("<value>" + attrValue + "</value>");
        }
        writer.print(attrPadding);
        writer.println("</subjectAttribute>");
      } catch (Exception e) {
        writer.print(attrPadding);
        writer.println("<!-- Problem retrieving attribute '" + attr
            + "' -->");
      }
    }
    writer.print(padding);
    writer.println("</subject>");

  }

  private Iterator getExportAttributes(Subject subj) {

    String source = subj.getSource().getId();
    String type = subj.getType().getName();
    String key = "export.subject-attributes.source." + source + "." + type;
    String value = options.getProperty(key);
    if (isEmpty(value)) {
      key = "export.subject-attributes.source." + source;
      value = options.getProperty(key);
    }
    if (isEmpty(value)) {
      key = "export.subject-attributes.type." + type;
      value = options.getProperty(key);
    }
    if (isEmpty(value)) {
      return null;
    }
    if ("*".equals(value)) {
      return subj.getAttributes().keySet().iterator();
    }
    StringTokenizer st = new StringTokenizer(value);
    Set res = new LinkedHashSet();
    while (st.hasMoreTokens()) {
      res.add(st.nextToken());
    }
    return res.iterator();
  }

  private boolean isEmpty(Object obj) {
    if (obj == null || "".equals(obj))
      return true;
    return false;
  }

  private void writePrivileges(GrouperSession s, String privilege,
      Set subjects, Stem stem, PrintWriter writer, String padding)
      throws Exception {
    GroupOrStem gos = findByStem(s, stem);
    writePrivileges(s, privilege, subjects, gos, writer, padding);
  }

  private void writePrivileges(GrouperSession s, String privilege,
      Set subjects, Group group, PrintWriter writer, String padding)
      throws Exception {
    GroupOrStem gos = findByGroup(s, group);
    writePrivileges(s, privilege, subjects, gos, writer, padding);
  }

  private void writePrivileges(GrouperSession s, String privilege,
      Set subjects, GroupOrStem gos, PrintWriter writer, String padding)
      throws Exception {
    if (subjects.size() == 1)
      subjects.remove(sysUser);
    if (subjects.isEmpty()) {
      if (log.isDebugEnabled()) {
        log.debug("No privilegees with [" + privilege + "] for "
            + gos.getName());
      }
      return;
    }

    if (log.isDebugEnabled()) {
      log.debug("Writing privilegees with [" + privilege + "] for "
          + gos.getName());
    }
    writer.println();
    writer.print(padding);
    Group group = gos.getGroup();
    Stem stem = gos.getStem();

    String id = null;
    writer.println("<privileges type='" + privilege + "'>");
    Iterator subjIterator = subjects.iterator();
    Subject subject;
    Member member;
    boolean isImmediate = false;
    String privUpper = privilege.toUpperCase();

    Map privs;
    Map immediatePrivs;

    while (subjIterator.hasNext()) {
      subject = (Subject) subjIterator.next();
      member = MemberFinder.findBySubject(s, subject);
      //privs = GrouperHelper.getAllHas(s,gos,member);
      //immediatePrivs=(Map)privs.get("subject");

      //isImmediate = immediatePrivs!=null &&
      // immediatePrivs.containsKey(privUpper);
      isImmediate = hasImmediatePrivilege(subject, gos, privilege);
      if ((!"GrouperSystem".equals(subject.getId()))
          && (isImmediate || !optionTrue("export.privs.immediate-only"))) {
        writeSubject(s, subject, " immediate='" + isImmediate + "' ",
            writer, padding + "  ");
      }
    }
    writer.print(padding);
    writer.println("</privileges> <!--/privilege=" + privilege + "-->");

  }

  protected static boolean hasImmediatePrivilege(Subject subject,
      GroupOrStem gos, String privilege) throws Exception {

    if (gos.isGroup()) {
      return hasImmediatePrivilege(subject, gos.getGroup(), privilege);
    }
    return hasImmediatePrivilege(subject, gos.getStem(), privilege);
  }

  protected static boolean hasImmediatePrivilege(Subject subject,
      Group group, String privilege) throws Exception {
    Set privs = null;
    Iterator privIterator = null;

    privs = group.getPrivs(subject);
    AccessPrivilege aPriv;
    privIterator = privs.iterator();
    while (privIterator.hasNext()) {
      aPriv = (AccessPrivilege) privIterator.next();
      if (aPriv.getName().equals(privilege)
          && aPriv.getOwner().equals(subject)) {
        return true;
      }
    }
    return false;

  }

  protected static boolean hasImmediatePrivilege(Subject subject, Stem stem,
      String privilege) throws Exception {
    Set privs = null;
    Iterator privIterator = null;

    privs = stem.getPrivs(subject);
    NamingPrivilege nPriv;
    privIterator = privs.iterator();
    while (privIterator.hasNext()) {
      nPriv = (NamingPrivilege) privIterator.next();
      if (nPriv.getName().equals(privilege)
          && nPriv.getOwner().equals(subject)) {
        return true;
      }
    }
    return false;
  }

  private String fixXmlAttribute(String value) {
    value = value.replaceAll("'", "&apos;");
    value = value.replaceAll("<", "&lt;");
    value = value.replaceAll(">", "&gt;");
    return value;
  }

  private boolean optionTrue(String key) {
    if (isEmpty(key)) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals(options.getProperty(key));
  }

  /**
   * For a group, for all its types, return fields of type LIST
   * 
   * @param s
   * @param g
   * @return
   */
  public static List getListFieldsForGroup(GrouperSession s, Group g)
      throws SchemaException {
    List lists = new ArrayList();
    Set types = g.getTypes();
    Iterator it = types.iterator();
    Set fields;
    Field field;
    Iterator fieldsIt;
    GroupType type;
    while (it.hasNext()) {
      type = (GroupType) it.next();
      fields = type.getFields();
      fieldsIt = fields.iterator();
      while (fieldsIt.hasNext()) {
        field = (Field) fieldsIt.next();
        if (field.getType().equals(FieldType.LIST)
            && !"members".equals(field.getName())) {
          if (g.canReadField(s.getSubject(), field)) {
            lists.add(field.getName());
          }
        }
      }
    }

    return lists;
  }

  public static void commandLineUsage() {
    System.out.println("Usage:");
    System.out.println("args: -h,            Prints this message");
    System.out
        .println("args: subjectIdentifier [(-id <id>] | [-name <name>)] [-relative]");
    System.out.println("      [-includeParent] fileName [properties]");
    System.out.println();
    System.out
        .println("  subjectIdentifier, Identifies a Subject 'who' will create a");
    System.out.println("                     GrouperSession");
    System.out
        .println("  -id,               The Uuid of a Group or Stem to export");
    System.out
        .println("  -name,             The name of a Group or Stem to export");
    System.out
        .println("  -relative,         If id or name specified do not export parent");
    System.out.println("                     Stems");
    System.out
        .println("  -includeParent,    If id or name identifies a Stem export this");
    System.out
        .println("                     stem and child Stems or Groups");
    System.out
        .println("  filename,          The file where exported data will be written.");
    System.out
        .println("                     Will overwrite existing files");
    System.out
        .println("  properties,        The name of a standard Java properties file");
    System.out
        .println("                     which configures the export. Check Javadoc for");
    System.out
        .println("                     a list of properties. If 'properties' is not ");
    System.out
        .println("                     specified, XmlExporter will look for ");
    System.out
        .println("                     'export.properties' in the working directory. ");
    System.out
        .println("                     If this file does not exist XmlExporter will ");
    System.out
        .println("                     look on the classpath. If 'properties' is not ");
    System.out
        .println("                     specified and 'export.properties' cannot be ");
    System.out.println("                     found, the export will fail.");
    System.out.println();
  }
  
  /**
   * Already have a stem but a method needs GroupOrStem
   * @param s
   * @param stem
   * @return
   */
  public  GroupOrStem findByStem(GrouperSession s,Stem stem) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s=s;
    groupOrStem.stem=stem;
    return groupOrStem;
  }
  
  /**
   * Already have a group but a method needs GroupOrStem
   * @param s
   * @param group
   * @return
   */
  public  GroupOrStem findByGroup(GrouperSession s,Group group) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s=s;
    groupOrStem.group=group;
    return groupOrStem;
  }
  
  /**
   * Only have and id ...
   * @param s
   * @param id
   * @return
   */
  public  GroupOrStem findByID(GrouperSession s,String id) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s = s;
    if("Grouper.NS_ROOT".equals(id)) {
      groupOrStem.stem=StemFinder.findRootStem(s);
      return groupOrStem;
    }
    try {
      Group group = GroupFinder.findByUuid(s,id);
      groupOrStem.group = group;
      
    }catch(Exception e) {
      try {
        Stem stem = StemFinder.findByUuid(s,id);
        groupOrStem.stem = stem;
      }catch(Exception se) {
        throw new RuntimeException("Unable to instatiate a group or stem with ID=" + id);
      }
    }
    return groupOrStem;
  }
  
  /**
   * Only have a name...
   * @param s
   * @param name
   * @return
   */
  public  GroupOrStem findByName(GrouperSession s,String name) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s = s;
    try {
      Group group = GroupFinder.findByName(s,name);
      groupOrStem.group = group;
      
    }catch(Exception e) {
      try {
        Stem stem = StemFinder.findByName(s,name);
        groupOrStem.stem = stem;
      }catch(Exception se) {
        throw new RuntimeException("Unable to instatiate a group or stem with name=" + name);
      }
    }
    return groupOrStem;
  }
  
  public class GroupOrStem {
    private Group group = null;
    private Stem stem = null;
    private GrouperSession s = null;
    
    
    
    /**
     * Don't just let any one make a GroupOrStem
     */
    private GroupOrStem() {
      
    }
    
    /**
     * 
     * @return id of 'masked' group or stem
     */
    public String getId() {
      if(group!=null) return group.getUuid();
      return stem.getUuid();
    }
    
    /**
     * @return if 'masked' object is a Group
     */
    public boolean isGroup() {
      return (group != null);
    }
    
    /**
     * @return if 'masked' object is a Stem
     */
    public boolean isStem() {
      return (stem != null);
    }
    
    
    
    /**
     * @return masked Group (or null if is a Stem)
     */
    public Group getGroup() {
      return group;
    }
    
    /**
     * @return masked Stem (or null if is a Group)
     */
    public Stem getStem() {
      return stem;
    }
    
    public String getDisplayExtension() {
      if(group!=null) return group.getDisplayExtension();
      if(stem!=null) return stem.getDisplayExtension();
      throw new IllegalStateException("GroupOrStem is not initialised");
    }
    
    public String getDisplayName() {
      if(group!=null) return group.getDisplayName();
      if(stem!=null) return stem.getDisplayName();
      throw new IllegalStateException("GroupOrStem is not initialised");
    }
    
    public String getName() {
      if(group!=null) return group.getName();
      if(stem!=null) return stem.getName();
      throw new IllegalStateException("GroupOrStem is not initialised");
    }
    
    public String getType() {
      if(group!=null) return "group";
      if(stem!=null) return "stem";
      throw new IllegalStateException("GroupOrStem is not initialised");
    }
  }

}

