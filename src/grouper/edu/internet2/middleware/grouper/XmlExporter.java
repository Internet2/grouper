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
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  org.apache.commons.logging.*;

/**
 * Utility class for exporting data from the Groups Registry in XML format.
 * <p>
 * This class can export all-or-port of a Groups Registry as a stem/group
 * hierarchy.  Alternatively, collections of {@link Stem}s, {@link Group}s,
 * {@link Subject}s or {@link Membership}s may be exported.
 * </p>
 * <p>
 * Exported stem and group data may be imported, as is, or with modifications,
 * into the same repository or into another repository. See {@link XmlImporter}.
 * </p>
 * <p><b>The API for this class will change in future Grouper releases.</b></p>
 * @author  Gary Brown.
 * @version $Id: XmlExporter.java,v 1.10 2006-09-12 17:19:58 blair Exp $
 * @since   1.0
 */
public class XmlExporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final Log LOG = LogFactory.getLog(XmlExporter.class);


  // PRIVATE INSTANCE VARIABLES //
  private GroupType   baseType          = null;
  private String      fromStem          = null;
  private boolean     includeParent;
  private boolean     isRelative;
  private Properties  options;
  private Subject     sysUser;
  private int         writeStemsCounter = 0;
  private XmlWriter   xml;


  // CONSTRUCTORS //

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
   * @param   options
   * @throws  Exception
   * @since   1.0
   */
  public XmlExporter(Properties options) 
    throws  Exception 
  {
    this.baseType = GroupTypeFinder.find("base");
    this.options  = options;
    this.sysUser  = SubjectFinder.findRootSubject();
  } // public XmlExporter(options)

  /**
   * TODO
   * </p>
   * @param   options   Configuration parameters.
   * @param   writer    Write XML here.
   * @throws  GrouperRuntimeException
   * @since   1.1
   */
  public XmlExporter(Properties options, Writer writer) {
    try {
      this.baseType = GroupTypeFinder.find("base");     // TODO ?
    }
    catch (SchemaException eS) {
      throw new GrouperRuntimeException(eS.getMessage(), eS);
    }
    this.options  = options;
    this.sysUser  = SubjectFinder.findRootSubject();  // TODO ?
    this.xml      = new XmlWriter(writer);
  } // public XmlExporter(options, writer)
  
  // MAIN //
  /**
   * Export Groups Registry to XML output.
   * <p/>
   * @throws 
  // @throws  Exception
   * @since   1.0
   */
  public static void main(String args[]) 
    throws  Exception 
  {
    if (
      args.length == 0
      || 
      "--h --? /h /? --help /help ${cmd}".indexOf(args[0]) > -1
    ) 
    {
      XmlExporter.commandLineUsage();
      System.exit(0);
    }

    String  arg;
    String  exportFile            = null;
    String  exportProperties      = "export.properties"; // TODO constant
    String  id                    = null;
    boolean includeParent         = false;
    int     inputPos              = 0;
    String  name                  = null;
    int     pos                   = 0;
    boolean relative              = false;
    String  subjectIdentifier     = null;
    String  userExportProperties  = null;

    try {
      while (pos < args.length) {
        arg = args[pos];
        if (arg.startsWith("-")) {
          if (arg.equals("-id")) {
            if (name != null) {
              throw new IllegalArgumentException("Cannot specify id and name");
            }
            id = args[pos + 1];
            if (id.startsWith("-")) {
              throw new IllegalArgumentException("id cannot start with -");
            }
            pos += 2;
            continue;
          } 
          else if (arg.equals("-name")) {
            if (id != null) {
              throw new IllegalArgumentException("Cannot specify id and name");
            }
            name = args[pos + 1];
            if (name.startsWith("-")) {
              throw new IllegalArgumentException("name cannot start with -");
            }
            pos += 2;
            continue;
          } 
          else if (arg.equals("-relative")) {
            relative = true;
            pos++;
            continue;
          } 
          else if (arg.equalsIgnoreCase("-includeparent")) {
            includeParent = true;
            pos++;
            continue;
          } else {
            throw new IllegalArgumentException("Unrecognised option " + arg);
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
          throw new IllegalArgumentException("Too many arguments - " + arg);
        }
        pos++;
        inputPos++;
      }
      if (inputPos < 1) {
        throw new IllegalStateException("Too few arguments");
      }
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.out.println();
      XmlExporter.commandLineUsage();
      System.exit(1);
    }
    Properties props = new Properties();
    if (userExportProperties != null) {
      LOG.info("Loading user-specified properties [" + userExportProperties + "]");
      props.load(new FileInputStream(userExportProperties));
    } 
    else {
      LOG.info("Loading default properties [" + exportProperties + "]");
      try {
        props.load(new FileInputStream(exportProperties));
      } catch (Exception e) {
        LOG.info(
          "Failed to find [" + exportProperties 
          + "] in working directory, trying classpath"
        );
        InputStream is = XmlExporter.class.getResourceAsStream(exportProperties);
        props.load(is);
      }
    }

    XmlExporter     exporter  = new XmlExporter( props, new PrintWriter(new FileWriter(exportFile) ) );
    Subject         user      = SubjectFinder.findByIdentifier(subjectIdentifier);
    GrouperSession  s         = GrouperSession.start(user);

    if (id == null && name == null) {
      exporter.export(s);
    } 
    else {
      Group group = null;
      Stem  stem  = null;
      if (id != null) {
        try {
          group = GroupFinder.findByUuid(s, id);
          LOG.debug("Found group with id [" + id + "]");
        } 
        catch (GroupNotFoundException e) {
          // Look for stem instead
        }
        if (group == null) {
          try {
            stem = StemFinder.findByUuid(s, id);
            LOG.debug("Found stem with id [" + id + "]");
          } 
          catch (StemNotFoundException e) {
            // No group or stem
          }
        }
        if (group == null && stem == null) {
          
        throw new IllegalArgumentException(
          "Could not find group or stem with id [" + id + "]"
        );
        }
      } 
      else {
        try {
          group = GroupFinder.findByName(s, name);
          LOG.debug("Found group with name [" + name + "]");
        } 
        catch (GroupNotFoundException e) {
          // Look for stem instead
        }
        if (group == null) {
          try {
            stem = StemFinder.findByName(s, name);
            LOG.debug("Found stem with name [" + name + "]");
          } catch (StemNotFoundException e) {
            // No group or stem
          }
        }
      }
      if (group == null && stem == null) {
        if (name != null) {
          throw new IllegalArgumentException(
            "Could not find group or stem with name [" + name + "]"
          );
        }
        throw new IllegalArgumentException(
          "Could not find group or stem with id [" + id + "]"
        );
      }
      if (group != null) {
        exporter.export(s, group, relative);
      } 
      else {
        exporter.export(s, stem, relative, includeParent);
      }
    }
    LOG.info("Finished export to [" + exportFile + "]");
    s.stop();
  } // public static void main(args)


  // PUBLIC CLASS METHODS //

  /**
   * @since   1.0
   */
  public static void commandLineUsage() {
    System.out.println("Usage:");
    System.out.println("args: -h,            Prints this message");
    System.out.println("args: subjectIdentifier [(-id <id>] | [-name <name>)] [-relative]");
    System.out.println("      [-includeParent] fileName [properties]");
    System.out.println();
    System.out.println("  subjectIdentifier, Identifies a Subject 'who' will create a");
    System.out.println("                     GrouperSession");
    System.out.println("  -id,               The Uuid of a Group or Stem to export");
    System.out.println("  -name,             The name of a Group or Stem to export");
    System.out.println("  -relative,         If id or name specified do not export parent");
    System.out.println("                     Stems");
    System.out.println("  -includeParent,    If id or name identifies a Stem export this");
    System.out.println("                     stem and child Stems or Groups");
    System.out.println("  filename,          The file where exported data will be written.");
    System.out.println("                     Will overwrite existing files");
    System.out.println("  properties,        The name of a standard Java properties file");
    System.out.println("                     which configures the export. Check Javadoc for");
    System.out.println("                     a list of properties. If 'properties' is not ");
    System.out.println("                     specified, XmlExporter will look for ");
    System.out.println("                     'export.properties' in the working directory. ");
    System.out.println("                     If this file does not exist XmlExporter will ");
    System.out.println("                     look on the classpath. If 'properties' is not ");
    System.out.println("                     specified and 'export.properties' cannot be ");
    System.out.println("                     found, the export will fail.");
    System.out.println();
  } // public static void commandLineUsage()

  /**
   * For a group, for all its types, return fields of type LIST
   * <p/>
   * @param   s
   * @param   g
   * @since   1.0
   */
  public static List getListFieldsForGroup(GrouperSession s, Group g)
    throws  SchemaException 
  {
    Field     field;
    Set       fields;
    Iterator  fieldsIt;
    List      lists     = new ArrayList();
    Set       types     = g.getTypes();

    GroupType type;
    Iterator  it        = types.iterator();
    while (it.hasNext()) {
      type      = (GroupType) it.next();
      fields    = type.getFields();
      fieldsIt  = fields.iterator();
      while (fieldsIt.hasNext()) {
        field = (Field) fieldsIt.next();
        if (
          field.getType().equals(FieldType.LIST)
          && !"members".equals(field.getName())
        ) 
        {
          if (g.canReadField(s.getSubject(), field)) {
            lists.add(field.getName());
          }
        }
      }
    }

    return lists;
  } // public static getListFieldsForGroup(s, group)

  
  // PUBLIC INSTANCE METHODS //

  /**
   * Exports data for the entire repository
   * <p/>
   * @param   s
   * @throws  Exception
   * @since   1.1.0
   */
  public void export(GrouperSession s)
    throws  Exception 
  {
    Stem        stem  = StemFinder.findRootStem(s);
    GroupOrStem gos   = findByStem(s, stem);
    LOG.info("Start export of entire repository");
    this._export(s, gos, true, false);
    LOG.info("Finished export of entire repository");
  } // public void export(s)

  /**
   * Export a Collection of stems, groups, members, subjects or memberships
   * <p/> 
   * @param   s
   * @param   items
   * @param   info    allows you to indicate how the Collection was generated
   * @throws  Exception
   * @since   1.0
   */
  public synchronized void export(GrouperSession s, Collection items, String info) 
    throws  Exception 
  {
    LOG.info("Start export of Collection:" + info);

    this.fromStem         = "_Z";
    Date    before        = _writeHeader();
    int     counter       = 0;
    String  origPadding   = "  ";
    String  padding       = "    ";

    if (optionTrue("export.data")) {
      Iterator itemsIterator = items.iterator();
      this.xml.put(origPadding);
      this.xml.puts("<dataList>");
      Object obj;
      while (itemsIterator.hasNext())       {
        obj = itemsIterator.next();
        counter++;
        if      (obj instanceof Group)      {
          _writeFullGroup(s, (Group) obj, padding);
        } 
        else if (obj instanceof Stem)       {
          Stem stem = (Stem) obj;
          _writeBasicStemHeader(s, stem, padding);
          _writeInternalAttributes(s, stem, padding);
          _writeStemPrivs(s, stem, padding);
          _writeBasicStemFooter(s, stem, padding);
        } 
        else if (obj instanceof Subject)    {
          if (counter == 1) {
            this.xml.puts("<exportOnly/>");
          }
          _writeSubject(s, (Subject) obj, padding);
        } 
        else if (obj instanceof Member)     {
          if (counter == 1) {
            this.xml.puts("<exportOnly/>");
          }
          _writeSubject(s, ((Member) obj).getSubject(), padding);
        } 
        else if (obj instanceof Membership) {
          if (counter == 1) {
            this.xml.puts("<exportOnly/>");
          }
          _writeMembership(s, (Membership) obj, padding);
        } 
        else {
          LOG.error("Don't know about exporting " + obj);
        }
        this.xml.puts();
      }
      this.xml.put(origPadding);
      this.xml.puts("</dataList>");
    }
    this.xml.put(origPadding);
    this.xml.puts("<exportComments><![CDATA[");
    this.xml.put(origPadding);
    this.xml.puts(info);
    this.xml.put(origPadding);
    this.xml.puts("]]></exportComments>");
    _writeFooter(before);
    LOG.info("Finished export of Collection:" + info);
  } // public synchronized void export(s, items, info)

  /**
   * Export a single group
   * <p/>
   * @param   s
   * @param   group
   * @param   relative  determines whether to export parent stems
   * @throws  Exception
   * @since   1.1.0
   */
  public void export(GrouperSession s, Group group, boolean relative) 
    throws  Exception 
  {
    GroupOrStem gos = findByGroup(s, group);
    LOG.info("Start export of Group " + group.getName());
    _export(s, gos, relative, false);
    LOG.info("Finished export of Group " + group.getName());
  } // public void export(s, group, relative)

  /**
   * Exports part of the repository
   * <p/> 
   * @param   s
   * @param   stem          where to export from
   * @param   relative      determines whether to export parent stems
   * @param   includeParent should 'stem' be included or just the children
   * @throws  Exception
   * @since   1.1.0
   */
  public void export(GrouperSession s, Stem stem, boolean relative, boolean includeParent) 
    throws  Exception 
  {
    GroupOrStem gos = findByStem(s, stem);
    LOG.info("Start export of Stem " + stem.getName());
    _export(s, gos, relative, includeParent);
    LOG.info("Finished export of Stem " + stem.getName());
  } // public void export(s, stem, relative, includeParent)

  /**
   * Already have a group but a method needs GroupOrStem
   * <p/>
   * @param   s
   * @param   group
   * @since   1.0
   */
  public GroupOrStem findByGroup(GrouperSession s,Group group) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s           = s;
    groupOrStem.group       = group;
    return groupOrStem;
  } // public GroupOrStem findByGroup(s, group)
  
  /**
   * Only have and id ...
   * <p/>
   * @param   s
   * @param   id
   * @since   1.0
   */
  public GroupOrStem findByID(GrouperSession s,String id) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s           = s;
    if("Grouper.NS_ROOT".equals(id)) {
      groupOrStem.stem=StemFinder.findRootStem(s);
      return groupOrStem;
    }
    try {
      Group group       = GroupFinder.findByUuid(s,id);
      groupOrStem.group = group;
    }
    catch (Exception e) {
      try {
        Stem stem         = StemFinder.findByUuid(s,id);
        groupOrStem.stem  = stem;
      }
      catch (Exception se) {
        throw new GrouperRuntimeException("Unable to instatiate a group or stem with ID=" + id);
      }
    }
    return groupOrStem;
  } // public GroupOrStem findByID(s, id)
  
  /**
   * Already have a stem but a method needs GroupOrStem
   * <p/>
   * @param   s
   * @param   stem
   * @since   1.0
   */
  public  GroupOrStem findByStem(GrouperSession s, Stem stem) {
    GroupOrStem groupOrStem = new GroupOrStem();
    groupOrStem.s           = s;
    groupOrStem.stem        = stem;
    return groupOrStem;
  } // public GroupOrStem findByStem(s, stem)
  
  /**
   * Only have a name...
   * @param s
   * @param name
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
        throw new GrouperRuntimeException("Unable to instatiate a group or stem with name=" + name);
      }
    }
    return groupOrStem;
  }
  
  /**
   * @return  Returns the options.
   * @since   1.0
   */
  public Properties getOptions() {
    return this.options;
  } // public Properties getOptions()

  /**
   * @param options   The options to set.
   * @since 1.0
   */
  public void setOptions(Properties options) {
    this.options = options;
  } // public void setOptions(options)


  // PROTECTED CLASS METHODS //

  // @since   1.0
  protected static boolean hasImmediatePrivilege(
    Subject subject, Group group, String privilege
  ) 
  {
    Iterator  privIterator  = null;
    Set       privs         = null;

    privs = group.getPrivs(subject);
    AccessPrivilege aPriv;
    privIterator = privs.iterator();
    while (privIterator.hasNext()) {
      aPriv = (AccessPrivilege) privIterator.next();
      if (
          aPriv.getName().equals(privilege)
          && aPriv.getOwner().equals(subject)
      ) 
      {
        return true;
      }
    }
    return false;

  } // protected static boolean hasImmediatePrivilege(subject, group, privilege)

  // @since   1.0
  protected static boolean hasImmediatePrivilege(
    Subject subject, GroupOrStem gos, String privilege
  ) 
  {
    if (gos.isGroup()) {
      return hasImmediatePrivilege(subject, gos.getGroup(), privilege);
    }
    return hasImmediatePrivilege(subject, gos.getStem(), privilege);
  } // protected static boolean hasImmediatePrivilege(subject, gos, privilege)

  // @since   1.0
  protected static boolean hasImmediatePrivilege(
    Subject subject, Stem stem, String privilege
  ) 
  {
    Iterator  privIterator  = null;
    Set       privs         = null;

    privs = stem.getPrivs(subject);
    NamingPrivilege nPriv;
    privIterator = privs.iterator();
    while (privIterator.hasNext()) {
      nPriv = (NamingPrivilege) privIterator.next();
      if (
          nPriv.getName().equals(privilege)
          && nPriv.getOwner().equals(subject)
      ) 
      {
        return true;
      }
    }
    return false;
  } // protected static boolean hasImmediatePrivilege(subject, stem, privilege)


  // PRIVATE INSTANCE METHODS //

  // @since   1.0
  private synchronized void _export(
    GrouperSession s, GroupOrStem groupOrStem, boolean relative, boolean includeParent
  )
    throws  Exception 
  {
    LOG.info("Relative export="     + relative);
    LOG.info("Include parent stem=" + includeParent);
    this.isRelative         = relative;
    this.includeParent      = includeParent;
    this.writeStemsCounter  = 0;
    Date    before          = _writeHeader();
    String  padding         = "  ";
    if (!relative) {
      fromStem = null;
    }
    if (relative) {
      Stem dummyStem = null;
      if      (includeParent || groupOrStem.isGroup()) {
        if (groupOrStem.isGroup()) {
          dummyStem = groupOrStem.getGroup().getParentStem();
        } 
        else {
          dummyStem = groupOrStem.getStem().getParentStem();
        }
      } 
      else if (!includeParent) {
        dummyStem = groupOrStem.getStem();
      }
      fromStem = dummyStem.getName() + ":";
    }

    if (optionTrue("export.data")) {
      _exportData(s, groupOrStem, padding);
    } 
    else {
      LOG.info("export.data=false, so no data exported");
    }
    _writeExportParams(groupOrStem, padding);
    _writeFooter(before);
  } // private synchronized void _export(s, groupOrStem, relative, includeParent)

  // @since   1.0
  private void _exportData(GrouperSession s, GroupOrStem groupOrStem, String padding) 
    throws  Exception 
  {
    LOG.debug("Writing repository data as XML");
    this.xml.puts(padding + "<data>");
    Stack stems = null;
    if (!isRelative) {
      stems = _getParentStems(groupOrStem);
    } 
    else {
      stems = new Stack();
      if (groupOrStem.isGroup()) {
        stems.push(groupOrStem.getGroup());
        if (includeParent) {
          stems.push(groupOrStem.getGroup().getParentStem());
        }
      } 
      else {
        stems.push(groupOrStem.getStem());
      }
    }
    _writeStems(s, stems, padding + "  ");
    this.xml.puts(padding + "</data>");
    LOG.debug("Finished repository data as XML");
  } // private void _exportData(s, groupOrStem, padding)

  // @since   1.0
  private String _fixGroupName(String name) {
    if (fromStem != null && name.startsWith(fromStem)) {
      name = name.replaceAll("^" + fromStem, "*");
    }
    return name;
  } // private String _fixGroupName(name)

  // @since   1.0
  private Iterator _getExportAttributes(Subject subj) {
    String source = subj.getSource().getId();
    String type   = subj.getType().getName();
    String key    = "export.subject-attributes.source." + source + "." + type;
    String value  = options.getProperty(key);
    if (_isEmpty(value)) {
      key   = "export.subject-attributes.source." + source;
      value = options.getProperty(key);
    }
    if (_isEmpty(value)) {
      key   = "export.subject-attributes.type." + type;
      value = options.getProperty(key);
    }
    if (_isEmpty(value)) {
      return null;
    }
    if ("*".equals(value)) {
      return subj.getAttributes().keySet().iterator();
    }
    StringTokenizer st  = new StringTokenizer(value);
    Set             res = new LinkedHashSet();
    while (st.hasMoreTokens()) {
      res.add(st.nextToken());
    }
    return res.iterator();
  } // private Iterator _getExportAttributes(subj)

  // @since   1.0
  private boolean _isEmpty(Object obj) {
    if (obj == null || "".equals(obj)) {
      return true;
    }
    return false;
  } // private boolean _isEmpty(obj)

  private String fixXmlAttribute(String value) {
    value = value.replaceAll("'", "&apos;");
    value = value.replaceAll("<", "&lt;");
    value = value.replaceAll(">", "&gt;");
    return value;
  }

  private boolean optionTrue(String key) {
    if (_isEmpty(key)) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals(options.getProperty(key));
  }

  // @since   1.0
  private Stack _getParentStems(GroupOrStem gos) 
    throws  Exception 
  {
    Stem  startStem = null;
    Stack stems     = new Stack();
    if (gos.isGroup()) {
      Group group = gos.getGroup();
      stems.push(group);
      startStem = group.getParentStem();
    } 
    else {
      startStem = gos.getStem();
    }
    stems.push(startStem);
    Stem parent = startStem;
    do {
      try {
        parent = parent.getParentStem();
        if (_isEmpty(parent.getExtension())) {
          parent = null;
        } 
        else {
          stems.push(parent);
        }
      } 
      catch (StemNotFoundException e) {
        parent = null;
      }
    } 
    while (parent != null);
    return stems;
  } // private Stack _getParentStems(gos)

  // @throws  IOException
  // @since   1.1.0
  private void _writeBasicStemFooter(GrouperSession s, Stem stem, String padding) 
    throws  IOException
  {
    this.xml.puts(padding + "</stem>");
    this.xml.puts(padding + "<!--/" + stem.getName() + "-->");
    this.xml.puts();
  } // private void _writeBasicStemFooter(s, stem, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeBasicStemHeader(GrouperSession s, Stem stem, String padding) 
    throws  IOException
  {
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts("<!--" + stem.getName() + "-->");
    this.xml.put(padding);
    this.xml.puts(
      "<stem extension='" + fixXmlAttribute(stem.getExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts(
      "displayExtension='" + fixXmlAttribute(stem.getDisplayExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("name='" + fixXmlAttribute(stem.getName()) + "'");
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts(
      "displayName='" + fixXmlAttribute(stem.getDisplayName()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("id='" + fixXmlAttribute(stem.getUuid()) + "'>");

    this.xml.put(padding + "  ");
    this.xml.puts(
      "<description>" + fixXmlAttribute(stem.getDescription()) + "</description>"
    );

  } // private void _writeBasicStemHeader(s, stem, padding)

  // @throws  GroupNotFoundException
  // @throws  IOException
  // @since   1.1.0
  private void _writeComposite(GrouperSession s, Composite comp, String padding) 
    throws  GroupNotFoundException,
            IOException
  {
    String nPadding = padding + "  ";
    this.xml.put(padding);
    this.xml.puts("<composite>");
    _writeGroupRef(s, comp.getLeftGroup(), nPadding);
    this.xml.puts();
    this.xml.put(nPadding);
    this.xml.puts(
      "<compositeType>" + comp.getType().toString() + "</compositeType>"
    );
    this.xml.puts();
    _writeGroupRef(s, comp.getRightGroup(), nPadding);
    this.xml.put(padding);
    this.xml.puts("</composite>");
  } // private void _writeComposite(s, comp, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeExportParams(
    GroupOrStem groupOrStem, String padding
  ) 
    throws  IOException
  {
    LOG.debug("Writing export params to XML");
    this.xml.put(padding);
    this.xml.puts("<exportParams>");
    this.xml.put(padding + "  ");
    this.xml.put("<node type='" + groupOrStem.getType() + "'>");
    this.xml.put(groupOrStem.getName());
    this.xml.puts("</node>");
    this.xml.put(padding + "  ");
    this.xml.puts("<relative>" + isRelative + "</relative>");
    if (groupOrStem.isStem()) {
      this.xml.put(padding + "  ");
      this.xml.puts("<includeParent>" + includeParent
          + "</includeParent>");
    }
    this.xml.put(padding);
    this.xml.puts("</exportParams>");
  } // private void _writeExportParams(groupOrStem, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeFieldMetaData(Field field, String padding) 
    throws  IOException
  {
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts(
      "<field name='" + fixXmlAttribute(field.getName()) + "'"
    );
    this.xml.puts(
      padding + "        required='" + field.getRequired() + "'"
    );
    this.xml.puts(padding + "        type='" + field.getType() + "'");
    this.xml.puts(
      padding + "        readPriv='" + field.getReadPriv() + "'"
    );
    this.xml.puts(
      padding + "        writePriv='" + field.getWritePriv() + "'/>"
    );
  } // private void _writeFieldMetaData(field, padding)

  // @since   1.1.0
  private synchronized void _writeFooter(Date before)
    throws  Exception 
  {
    LOG.debug("Writing XML Footer");
    Date    now       = new Date();
    long    duration  = (now.getTime() - before.getTime()) / 1000;
    String  padding   = "  ";
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts("<exportInfo>");
    this.xml.put(padding + "  ");
    this.xml.puts("<start>" + before + "</start>");
    this.xml.put(padding + "  ");
    this.xml.puts("<end>" + now + "</end>");
    this.xml.put(padding + "  ");
    this.xml.puts("<duration>" + duration + "</duration>");
    _writeOptions(padding + "  ");
    this.xml.put(padding);
    this.xml.puts("</exportInfo>");
    this.xml.puts("</registry>");
    this.xml.close();
  } // private synchronized _writeFooter(before)

  // @throws  CompositeNotFoundException
  // @throws  GroupNotFoundException
  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SubjectNotFoundException
  // @throws  SchemaException
  // @since 1.1.0
  private void _writeFullGroup(GrouperSession s, Group group, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Writing group " + group.getName() + " to XML");
    }
    this.xml.puts();
    this.xml.put(padding);
    try {
      this.xml.puts("<!--" + group.getName() + "-->");
    } 
    catch (Exception e) {
    }
    this.xml.put(padding);
    this.xml.puts(
      "<group extension='" + fixXmlAttribute(group.getExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("       ");
    this.xml.puts(
      "displayExtension='" + fixXmlAttribute(group.getDisplayExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("name='" + fixXmlAttribute(group.getName()) + "'");
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts(
      "displayName='" + fixXmlAttribute(group.getDisplayName()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("id='" + fixXmlAttribute(group.getUuid()) + "'>");

    this.xml.put(padding + "  ");
    this.xml.puts(
      "<description>" + fixXmlAttribute(group.getDescription()) + "</description>"
    );

    if (optionTrue("export.group.internal-attributes")) {
      _writeInternalAttributes(s, group, padding + "  ");
    }
    if (optionTrue("export.group.custom-attributes")) {
      Set       types     = group.getTypes();
      GroupType baseType  = GroupTypeFinder.find("base");
      types.remove(baseType);
      if (!types.isEmpty()) {
        this.xml.puts(padding + "  <groupTypes>");
        Iterator  typesIterator = types.iterator();
        GroupType groupType;
        while (typesIterator.hasNext()) {
          groupType = (GroupType) typesIterator.next();
          _writeGroupType(s, group, groupType, padding + "    ");
        }

        this.xml.puts(padding + "  </groupTypes>");
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
      if (LOG.isDebugEnabled()) { 
        LOG.debug(
          "Writing list members for " + group.getName() + ": field=" + listFields.get(i)
        );
      }
      _writeListField(
        s, group, FieldFinder.find((String) listFields .get(i)), padding + "  "
      );
    }
    if (optionTrue("export.privs.access")) {
      _writePrivileges(s, "admin" , group.getAdmins()   , group, padding);
      _writePrivileges(s, "update", group.getUpdaters() , group, padding);
      _writePrivileges(s, "read"  , group.getReaders()  , group, padding);
      _writePrivileges(s, "view"  , group.getViewers()  , group, padding);
      _writePrivileges(s, "optin" , group.getOptins()   , group, padding);
      _writePrivileges(s, "optout", group.getOptouts()  , group, padding);
    }
    this.xml.puts();
    this.xml.puts(padding + "</group>");
    try {
      this.xml.puts(padding + "<!--/" + group.getName() + "-->");
    } catch (Exception e) {
    }
    this.xml.puts();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished writing group " + group.getName() + " to XML");
    }
  } // private void _writeFullGroup(s, group, padding)

  // @throws  CompositeNotFoundException
  // @throws  GroupNotFoundException
  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SchemaException
  // @throws  StemNotFoundException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeFullStem(GrouperSession s, Stem stem, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Writing Stem " + stem.getName() + " to XML");
    }
    _writeBasicStemHeader(s, stem, padding);
    _writeInternalAttributes(s, stem, padding);
    _writeStemPrivs(s, stem, padding);
    _writeStemBody(s, stem, padding + "  ");
    _writeBasicStemFooter(s, stem, padding);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Finished writing Stem " + stem.getName() + " to XML");
    }
  } // private void _writeFullStem(s, stem, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeGroupRef(GrouperSession s, Group group, String padding) 
    throws  IOException
  {
    _writeGroupRef(s, group, padding, false);
  } // private void _writeGroupRef(s, group, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeGroupRef(GrouperSession s, Group group, String padding, boolean writeAbsoluteName) 
    throws  IOException
  {
    this.xml.put(padding);
    this.xml.puts("<groupRef id='" + group.getUuid() + "'");
    this.xml.put(padding);
    String name = group.getName();
    if (!writeAbsoluteName) {
      name = _fixGroupName(name);
    }
    this.xml.puts("        name='" + name + "'");
    this.xml.put(padding);
    this.xml.puts(" displayName='" + group.getDisplayName() + "'/>");
  } // private void _writeGroupRef(s, group, padding, writeAbsoluteName)

  // @throws  IOException
  // @throws  SchemaException
  // @since   1.1.0
  private void _writeGroupType(GrouperSession s, Group group, GroupType groupType, String padding)
    throws  IOException,
            SchemaException
  {
    this.xml.puts(
      padding + "<groupType name='" + fixXmlAttribute(groupType.getName()) + "'>"
    );
    Field     field;
    Set       fields          = groupType.getFields();
    Iterator  fieldsIterator  = fields.iterator();
    String    value;
    padding                   = padding + "  ";
    while (fieldsIterator.hasNext()) {
      field = (Field) fieldsIterator.next();
      if (field.getType().equals(FieldType.LIST)) {
        continue;
      }
      if (!group.canReadField(field)) {
        continue;
      }
      try {
        value = fixXmlAttribute(group.getAttribute(field.getName()));
        if (
            !_isEmpty(value)
            && ":description:extension:displayExtension:"
                .indexOf(":" + field.getName() + ":") == -1
        ) 
        {
          this.xml.put(padding);
          this.xml.puts(
            "<attribute name='"
            + fixXmlAttribute(field.getName()) + "'>" + value
            + "</attribute>"
          );
        }
      } catch (Exception e) {
      }

    }

    this.xml.puts(padding + "</groupType>");
  } // private void _writeGroupType(s, group, groupType, padding)

  // @throws  IOException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeInternalAttributes(GrouperSession s, Group group, String padding) 
    throws  IOException,
            SubjectNotFoundException
  {
    this.xml.puts();
    this.xml.puts(padding + "<internalAttributes>");
    this.xml.puts(
      padding + "  <internalAttribute name='parentStem'>"
      + fixXmlAttribute(group.getParentStem().getName())
      + "</internalAttribute>"
    );
    this.xml.puts(
      padding + "  <internalAttribute name='createSource'>"
      + fixXmlAttribute(group.getCreateSource())
      + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='createSubject'>");
    _writeSubject(s, group.getCreateSubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
      padding + "  <internalAttribute name='createTime'>"
      + fixXmlAttribute(group.getCreateTime().toString())
      + "</internalAttribute>"
    );

    this.xml.puts(
      padding + "  <internalAttribute name='modifySource'>"
      + fixXmlAttribute(group.getModifySource())
      + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='modifySubject'>");
    _writeSubject(s, group.getModifySubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
      padding + "  <internalAttribute name='modifyTime'>"
      + fixXmlAttribute(group.getModifyTime().toString())
      + "</internalAttribute>"
    );

    this.xml.puts(padding + "</internalAttributes>");
    this.xml.puts();
  } // private void _writeInternalAttributes(s, group, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeGroupTypesMetaData(String padding)
    throws  IOException 
  {
    Set groupTypes = GroupTypeFinder.findAll();
    if (groupTypes.isEmpty()) {
      return;
    }
    this.xml.put(padding);
    this.xml.puts("<groupTypesMetaData>");
    Field     field;
    Set       fields;
    Iterator  fieldsIterator;
    GroupType groupType;
    Iterator  groupTypesIterator = groupTypes.iterator();
    padding               = padding + "  ";
    while (groupTypesIterator.hasNext()) {
      groupType = (GroupType) groupTypesIterator.next();
      this.xml.puts();
      this.xml.put(padding);
      this.xml.puts(
        "<groupTypeDef name='" + fixXmlAttribute(groupType.getName()) + "'>"
      );
      fields = groupType.getFields();
      fieldsIterator = fields.iterator();
      while (fieldsIterator.hasNext()) {
        field = (Field) fieldsIterator.next();
        _writeFieldMetaData(field, padding + " ");
      }
      this.xml.puts();
      this.xml.put(padding);
      this.xml.puts("</groupTypeDef>");
    }

    this.xml.put(padding);
    this.xml.puts("</groupTypesMetaData>");
    this.xml.puts();
  } // private void _writeGroupTypesMetaData(padding)

  // @throws  GrouperException
  // @throws  IOException
  // @since   1.1.0
  private synchronized Date _writeHeader()
    throws  GrouperException,
            IOException 
  {
    LOG.debug("Writing XML header");
    Date    before  = new Date();
    String  padding = "  ";
    this.xml.puts("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    this.xml.puts("<registry>");
    if (optionTrue("export.metadata")) {
      _writeMetaData(padding);
    }
    return before;
  } // private synchronized Date _writeHeader()

  // @throws  IOException
  // @throws  StemNotFoundException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeInternalAttributes(GrouperSession s, Stem stem, String padding) 
    throws  IOException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if (!optionTrue("export.stem.internal-attributes")) {
      return;
    }
    this.xml.puts();
    this.xml.puts(padding + "<internalAttributes>");
    this.xml.puts(
      padding + "  <internalAttribute name='parentStem'>"
      + fixXmlAttribute(stem.getParentStem().getName())
      + "</internalAttribute>"
    );
    this.xml.puts(
      padding + "  <internalAttribute name='createSource'>"
     + fixXmlAttribute(stem.getCreateSource())
     + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='createSubject'>");
    _writeSubject(s, stem.getCreateSubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
      padding + "  <internalAttribute name='createTime'>"
      + fixXmlAttribute(stem.getCreateTime().toString())
     + "</internalAttribute>"
    );

    this.xml.puts(
      padding + "  <internalAttribute name='modifySource'>"
      + fixXmlAttribute(stem.getModifySource())
      + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='modifySubject'>");
    _writeSubject(s, stem.getModifySubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
      padding + "  <internalAttribute name='modifyTime'>"
      + fixXmlAttribute(stem.getModifyTime().toString())
      + "</internalAttribute>"
    );

    this.xml.puts(padding + "</internalAttributes>");
    this.xml.puts();
  } // private void _writeInternalAttributes(s, stem, padding)

  // @throws  CompositeNotFoundException
  // @throws  GroupNotFoundException
  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SchemaException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeListField(GrouperSession s, Group group, Field field, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    if (!group.canReadField(field)) {
      LOG.info(
        "No read privilege. List [" + field.getName() + "] for ["
        + group.getName() + "] ignored"
      );
      return;
    }
    boolean isComposite = false;
    Set     membersSet = null;
    if ("members".equals(field.getName()) && group.hasComposite()) {
      isComposite = true;
      membersSet  = new HashSet();
    } 
    else {
      membersSet = group.getImmediateMemberships(field);
    }
    Collection members = new ArrayList();
    members.addAll(membersSet);
    if (
      (
        "members".equals(field.getName()) && !optionTrue("export.group.members.immediate-only")
      )
      || 
      (
        !"members".equals(field.getName()) && !optionTrue("export.group.lists.immediate-only")
      )
    ) 
    {
      members.addAll(group.getEffectiveMemberships(field));
      if ("members".equals(field.getName()) && group.hasComposite()) {
        members.addAll(group.getCompositeMemberships());
      }
    }

    if (members.isEmpty() && !isComposite) {
      return;
    }
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts(
      "<list field='" + fixXmlAttribute(field.getName())
      + "'  groupType='"
      + fixXmlAttribute(field.getGroupType().getName()) + "'>"
    );
    if (isComposite) {
      Composite composite = CompositeFinder.findAsOwner(group);
      _writeComposite(s, composite, padding + "  ");
    }
    _writeMembers(s, members, group, field, padding + "  ");
    this.xml.put(padding);
    this.xml.puts(
      "</list> <!--/field=" + fixXmlAttribute(field.getName()) + "-->"
    );
  } // private void _writeListField(s, group, field, padding)

  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeMembers(GrouperSession s, Collection members, Group group, Field field, String padding)
    throws  IOException,
            MemberNotFoundException,
            SubjectNotFoundException
  {
    boolean     isImmediate;
    Iterator    it = members.iterator();
    Membership  member;
    Subject     subj;
    while (it.hasNext()) {
      isImmediate = false;
      member      = (Membership) it.next();
      try {
        isImmediate = member.getViaGroup() == null;
      }   
      catch (GroupNotFoundException e) {
        if (!group.hasComposite()) {
          isImmediate = true;
        }
      }
      subj = member.getMember().getSubject();
      _writeSubject(s, subj, " immediate='" + isImmediate + "' ", padding);
    }
  } // private void _writeMembers(s, members, group, field, padding)

  // @throws  GroupNotFoundException
  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeMembership(GrouperSession s, Membership membership, String padding) 
    throws  GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SubjectNotFoundException
  {
    boolean isImmediate = true;
    try {
      membership.getViaGroup();
      isImmediate = false;
    } catch (Exception e) {

    }

    String exPadding = padding + "  ";
    this.xml.put(padding);
    this.xml.puts("<membership>");
    this.xml.put(exPadding);
    this.xml.puts("<depth>" + membership.getDepth() + "</depth>");
    this.xml.put(exPadding);
    this.xml.puts("<listName>" + membership.getList().getName() + "</listName>");
    this.xml.put(exPadding);
    this.xml.puts("<immediate>" + isImmediate + "</immediate>");
    _writeGroupRef(s, membership.getGroup(), exPadding, true);
    _writeSubject(s, membership.getMember().getSubject(), exPadding);
    this.xml.put(padding);
    this.xml.puts("</membership>");
  } // private void _writeMembership(s, membership, padding)

  // @throws  GrouperException
  // @throws  IOException
  // @since   1.1.0
  private void _writeMetaData(String padding)
    throws  GrouperException,
            IOException 
  {
    LOG.debug("Writing repository metadata as XML");
    this.xml.put(padding);
    this.xml.puts("<metadata>");
    _writeGroupTypesMetaData(padding + "  ");
    _writeSubjectSourceMetaData(padding + "  ");
    this.xml.put(padding);
    this.xml.puts("</metadata>");
  } // private void _writeMetaData(padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeOptions(String padding) 
    throws  IOException
  {
    LOG.debug("Writing export options as XML");
    this.xml.put(padding);
    this.xml.puts("<options>");
    List      orderedList     = new ArrayList(options.keySet());
    Collections.sort(orderedList);
    Iterator  optionsIterator = orderedList.iterator();

    String key;
    while (optionsIterator.hasNext()) {
      key = (String) optionsIterator.next();
      this.xml.put(padding + "  ");
      this.xml.puts(
        "<option key='" + key + "'>"
        + options.getProperty(key) + "</option>"
      );
    }
    this.xml.put(padding);
    this.xml.puts("</options>");
  } // private void _writeOptions(padding)

  // @throws  IOException
  // @throws  MemberNotFoundException
  // @since   1.1.0
  private void _writePrivileges(GrouperSession s, String privilege, Set subjects, Group group, String padding)
    throws  IOException,
            MemberNotFoundException
  {
    GroupOrStem gos = findByGroup(s, group);
    _writePrivileges(s, privilege, subjects, gos, padding);
  } // private void _writePrivileges(s, privilege, subjects, group, padding)

  // @throws  IOException
  // @throws  MemberNotFoundException
  // @since   1.1.0
  private void _writePrivileges(GrouperSession s, String privilege, Set subjects, Stem stem, String padding)
    throws  IOException,
            MemberNotFoundException
  {
    GroupOrStem gos = findByStem(s, stem);
    _writePrivileges(s, privilege, subjects, gos, padding);
  } // private void _writePrivileges(s, privilege, subjects, stem, padding)

  // @throws  IOException
  // @throws  MemberNotFoundException
  // @since   1.1.0
  private void _writePrivileges(GrouperSession s, String privilege, Set subjects, GroupOrStem gos, String padding)
    throws  IOException,
            MemberNotFoundException
  {
    if (subjects.size() == 1) {
      subjects.remove(sysUser);
    }
    if (subjects.isEmpty()) {
      if (LOG.isDebugEnabled()) { 
        LOG.debug("No privilegees with [" + privilege + "] for " + gos.getName());
      }
      return;
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Writing privilegees with [" + privilege + "] for " + gos.getName());
    }
    this.xml.puts();
    this.xml.put(padding);

    this.xml.puts("<privileges type='" + privilege + "'>");
    Iterator  subjIterator = subjects.iterator();
    Subject   subject;
    Member    member;
    boolean   isImmediate = false;

    while (subjIterator.hasNext()) {
      subject = (Subject) subjIterator.next();
      member  = MemberFinder.findBySubject(s, subject);

      isImmediate = hasImmediatePrivilege(subject, gos, privilege);
      if (
        (!"GrouperSystem".equals(subject.getId()))
        && 
        (isImmediate || !optionTrue("export.privs.immediate-only"))) 
      {
        _writeSubject(
          s, subject, " immediate='" + isImmediate + "' ", padding + "  " 
        );
      }
    }
    this.xml.put(padding);
    this.xml.puts("</privileges> <!--/privilege=" + privilege + "-->");

  } // private void _writePrivileges(s, privilege, subjects, gos, padding)

  // @throws  CompositeNotFoundException
  // @throws  GroupNotFoundException
  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SchemaException,
  // @throws  StemNotFoundException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeStemBody(GrouperSession s, Stem stem, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    Stem      childStem;
    Set       stems         = stem.getChildStems();
    Iterator  stemsIterator = stems.iterator();
    while (stemsIterator.hasNext()) {
      childStem = (Stem) stemsIterator.next();
      _writeFullStem(s, childStem, padding);
    }

    Set       groups          = stem.getChildGroups();
    Iterator  groupsIterator  = groups.iterator();
    Group childGroup;
    while (groupsIterator.hasNext()) {
      childGroup = (Group) groupsIterator.next();
      _writeFullGroup(s, childGroup, padding);
    }
  } // private void _writeStemBody(s, stem, padding)

  // @throws  IOException
  // @throws  MemberNotFoundException
  // @since   1.1.0
  private void _writeStemPrivs(GrouperSession s, Stem stem, String padding) 
    throws  IOException,
            MemberNotFoundException
  {
    if (
        optionTrue("export.privs.naming") 
       ) 
    {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Writing STEM privilegees for " + stem.getName());
      }
      _writePrivileges(s, "stem", stem.getStemmers(), stem, padding);
      _writePrivileges(s, "create", stem.getStemmers(), stem, padding);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Writing CREATE privilegees for " + stem.getName());
      }
    } 
    else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Skipping naming privs for " + stem.getName());
      }
    }
  } // private void _writeStemPrivs(s, stem, padding)

  // @throws  CompositeNotFoundException
  // @throws  GroupNotFoundException
  // @throws  IOException
  // @throws  MemberNotFoundException
  // @throws  SchemaException
  // @throws  StemNotFoundException
  // @throws  SubjectNotFoundException
  // @since   1.1.0
  private void _writeStems(GrouperSession s, Stack stems, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    writeStemsCounter++;
    Object obj = stems.pop();
    if (obj instanceof Group) {
      _writeFullGroup(s, (Group) obj, padding);
      return;
    }

    Stem stem = (Stem) obj;

    if (stems.isEmpty()) {
      if (includeParent || writeStemsCounter > 1) {
        _writeFullStem(s, stem, padding + "  ");
      } 
      else {
        _writeStemBody(s, stem, padding + "  ");
      }
      return;
    } 
    else {
      _writeBasicStemHeader(s, stem, padding);
      if(optionTrue("export.privs.for-parents")) {
      	_writeStemPrivs(s, stem, padding);
      }
      _writeStems(s, stems, padding + "  ");
      _writeBasicStemFooter(s, stem, padding);
    }
  } // private void _writeStems(s, stems, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeSubject(GrouperSession s, Subject subj, String padding
  ) 
    throws  IOException 
  {
    _writeSubject(s, subj, "", padding);
  } // private void _writeSubject(s, subj, padding)

  // @throws  IOException
  // @since   1.1.0
  private void _writeSubject(GrouperSession s, Subject subj, String immediate, String padding) 
    throws  IOException 
  {
    this.xml.put(padding);
    String attrName = "id";
    String id       = null;
    if ("group".equals(subj.getType().getName())) {
      attrName  = "identifier";
      id        = _fixGroupName(subj.getName());
    } else {
      id = subj.getId();
    }
    this.xml.put(
      "<subject " + attrName + "='" + fixXmlAttribute(id)
      + "' type='" + fixXmlAttribute(subj.getType().getName())
      + "' source='" + fixXmlAttribute(subj.getSource().getId())
      + "'" + immediate
    );
    if ("group".equals(subj.getType().getName())) {
      this.xml.put(" id='" + subj.getId() + "'");
    }
    Iterator exportAttrs = _getExportAttributes(subj);
    if (_isEmpty(exportAttrs)) {
      this.xml.puts("/>");
      return;
    }
    this.xml.puts(">");
    String    attr;
    Iterator  attrIt;
    String    attrPadding = padding + "  ";
    String    attrValue;
    Set       values;
    while (exportAttrs.hasNext()) {
      attr = (String) exportAttrs.next();
      try {
        values = subj.getAttributeValues(attr);
        this.xml.put(attrPadding);
        this.xml.puts("<subjectAttribute name='" + attr + "'>");
        attrIt = values.iterator();
        while (attrIt.hasNext()) {
          attrValue = (String) attrIt.next();
          this.xml.put(attrPadding + "  ");
          this.xml.puts("<value>" + attrValue + "</value>");
        }
        this.xml.put(attrPadding);
        this.xml.puts("</subjectAttribute>");
      } catch (Exception e) {
        this.xml.put(attrPadding);
        this.xml.puts("<!-- Problem retrieving attribute '" + attr + "' -->");
      }
    }
    this.xml.put(padding);
    this.xml.puts("</subject>");

  } // private void _writeSubject(s, subj, immediate, padding)

  // @throws  GrouperException
  // @throws  IOException
  // @since   1.1.0
  private void _writeSubjectSourceMetaData(String padding)
    throws  GrouperException,
            IOException 
  {
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts("<subjectSourceMetaData>");
    String      origPadding     = padding;
    padding                     = padding + "  ";
    Source      source;
    Collection  sources;
    try {
      sources = SourceManager.getInstance().getSources();
    }
    catch (Exception e) {
      throw new GrouperException(e.getMessage(), e);
    }
    Iterator    sourcesIterator = sources.iterator();
    SubjectType subjectType;
    Set         subjectTypes;

    Iterator    subjectTypesIterator;
    while (sourcesIterator.hasNext()) {
      source = (Source) sourcesIterator.next();
      this.xml.puts();
      this.xml.put(padding);
      this.xml.puts(
        "<source id='" + fixXmlAttribute(source.getId()) + "'"
      );
      this.xml.puts(padding + "        name='" + source.getName() + "'");
      this.xml.puts(
        padding + "        class='" + source.getClass().getName() + "'>"
      );
      subjectTypes          = source.getSubjectTypes();
      subjectTypesIterator  = subjectTypes.iterator();
      while (subjectTypesIterator.hasNext()) {
        subjectType = (SubjectType) subjectTypesIterator.next();
        this.xml.put(padding + "  ");
        this.xml.puts(
          "<subjectType name='" + subjectType.getName() + "'/>"
        );
      }

      this.xml.put(padding);
      this.xml.puts("</source>");

    }
    this.xml.puts();
    this.xml.put(origPadding);
    this.xml.puts("</subjectSourceMetaData>");
    this.xml.puts();
  } // private void _writeSubjectSourceMetaData(padding)


  // CLASS: GroupOrStem //

  /**
   * @since   1.0
   */
  public class GroupOrStem {

    // PRIVATE INSTANCE VARIABLES //
    private Group           group = null;
    private Stem            stem  = null;
    private GrouperSession  s     = null;
    
   
    // CONSTRUCTORS // 
    
    // Don't just let any one make a GroupOrStem
    // @since   1.0
    private GroupOrStem() {
      
    } // private GroupOrStem()
   

    // PUBLIC INSTANCE METHODS //

    /**
     * @since   1.0
     */ 
    public String getDisplayExtension() {
      if (group != null) {
        return group.getDisplayExtension();
      }
      if(stem != null) {
        return stem.getDisplayExtension();
      }
      throw new IllegalStateException("GroupOrStem is not initialised");
    } // public String getDisplayExtension()
   
    /** 
     * @since   1.0
     */ 
    public String getDisplayName() {
      if (group != null) {
        return group.getDisplayName();
      }
      if (stem != null) {
        return stem.getDisplayName();
      }
      throw new IllegalStateException("GroupOrStem is not initialised");
    } // public String getDisplayName()
    
    /**
     * @return  masked Group (or null if is a Stem)
     * @since   1.0
     */
    public Group getGroup() {
      return this.group;
    } // public Group getGroup()
    
    /**
     * @return  id of 'masked' group or stem
     * @since   1.0
     */
    public String getId() {
      if (group!=null) {
        return group.getUuid();
      }
      return stem.getUuid();
    } // public String getId()
   
    /**
     * @since   1.0
     */ 
    public String getName() {
      if (group != null) {
        return group.getName();
      }
      if (stem != null) {
        return stem.getName();
      }
      throw new IllegalStateException("GroupOrStem is not initialised");
    } // public String getName()
    
    /**
     * @return  masked Stem (or null if is a Group)
     * @since   1.0
     */
    public Stem getStem() {
      return this.stem;
    } // public Stem getStem()
  
    /**
     * @since   1.0
     */
    public String getType() {
      if (group != null) {
        return "group";
      }
      if (stem != null) {
        return "stem";
      }
      throw new IllegalStateException("GroupOrStem is not initialised");
    } // public String getType()

    /**
     * @return  if 'masked' object is a Group
     * @since   1.0
     */
    public boolean isGroup() {
      return (group != null);
    }  // public boolean isGroup()
    
    /**
     * @return  if 'masked' object is a Stem
     * @since   1.0
     */
    public boolean isStem() {
      return (stem != null);
    } // public boolean isStem()
   
  } 
  // CLASS: GroupOrStem //

} // public class XmlExporter

