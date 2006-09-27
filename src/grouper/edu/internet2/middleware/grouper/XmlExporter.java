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
 * @author  blair christensen.
 * @version $Id: XmlExporter.java,v 1.38 2006-09-27 17:51:09 blair Exp $
 * @since   1.0
 */
public class XmlExporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final String CF          = "export.properties"; 
  private static final Log    LOG         = LogFactory.getLog(XmlExporter.class);
  private static final String RC_EFILE    = "export.file";
  private static final String RC_NAME     = "owner.name";
  private static final String RC_PARENT   = "mystery.parent";
  private static final String RC_RELATIVE = "mystery.relative";
  private static final String RC_SUBJ     = "subject.identifier";
  private static final String RC_UPROPS   = "properties.user";
  private static final String RC_UUID     = "owner.uuid";


  // PRIVATE INSTANCE VARIABLES //
  private GrouperSession  s;
  private GroupType       baseType          = null;
  private String          fromStem          = null;
  private boolean         includeParent;
  private boolean         isRelative;
  private Properties      options;
  private Subject         sysUser;
  private int             writeStemsCounter = 0;
  private XmlWriter       xml               = null;


  // CONSTRUCTORS //

  /**
   * Export the Groups Registry to XML.
   * <p>
   * The export process is configured using the follow properties.
   * </p>
   * <table width="90%" border="1">
   * <tr>
   * <td>Key</td>
   * <td>Values</td>
   * <td>Default Value</td>
   * <td>Description</td>
   * </tr>
   * <tr>
   * <td>export.metadata</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true Group type and field information as well as Subject sources will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.data</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true data will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.privs.naming</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true naming privileges will be exported along with Stems.</td>
   * </tr>
   * <tr>
   * <td>export.privs.access</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true access privileges will be exported along with Groups.</td>
   * </tr>
   * <tr>
   * <td>export.privs.immediate-only</td>
   * <td>true/false</td>
   * <td>false</td>
   * <td>If true only directly granted privileges will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.group.members</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true group memberships are exported.</td>
   * </tr>
   * <tr>
   * <td>export.group.members.immediate-only</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true only immediate group memberships will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.group.lists</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true custom list attributes will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.group.lists.immediate-only</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true only immediate list members will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.group.internal-attributes</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true system-maintained Group attributes (eg. <tt>modifyDate</tt>) will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.group.custom-attributes</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true custom attributes will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.stem.internal-attributes</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true system-maintained Stem attributes (eg. <tt>modifyDate</tt>) will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.privs.for-parents</td>
   * <td>true/false</td>
   * <td>false</td>
   * <td>If true and only exporting a partial hierarchy then privileges for parent stems will be exported.</td>
   * </tr>
   * <tr>
   * <td>export.subject-attributes.source.&lt;source name&gt;.&lt;subject type&gt;</td>
   * <td>Space separated list of attribute names</td>
   * <td>?</td>
   * <td>Specifices any attributes that should be exported with a Subject given the specified Source and Subject Type.</td>
   * </tr>
   * <tr>
   * <td>export.subject-attributes.source.&lt;source name&gt;</td>
   * <td>Space separated list of attribute names</td>
   * <td>?</td>
   * <td>Specifies any attributes that should be exported with a Subject given the specified Source.</td>
   * </tr>
   * <tr>
   * <td>export.subject-attributes.type.&lt;subject type&gt;</td>
   * <td>Space separated list of attribute names</td>
   * <td>?</td>
   * <td>Specifies any attributes that should be exported with a Subject given the Subject Type.</td>
   * </tr>
   * </table>
   * @param   s           Perform export within this session.
   * @param   userOptions User-specified configuration parameters.
   * @since   1.1.0
   */
  public XmlExporter(GrouperSession s, Properties userOptions) {
    try {
      this.baseType = GroupTypeFinder.find("base"); // TODO ?
    }
    catch (SchemaException eS) {
      throw new GrouperRuntimeException(eS.getMessage(), eS);
    }
    try {
      this.options  = XmlUtils.getSystemProperties(LOG, CF);
    }
    catch (IOException eIO) {
      throw new GrouperRuntimeException(eIO.getMessage(), eIO);
    }
    this.options.putAll(userOptions); 
    this.s        = s;
    this.sysUser  = SubjectFinder.findRootSubject(); // TODO ?
  } // public XmlExporter(s, userOptions)

  
  // MAIN //
  /**
   * Export Groups Registry to XML output.
   * <p/>
   * @since   1.1.0
   */
  public static void main(String args[]) {
    if (XmlUtils.wantsHelp(args)) {
      System.out.println( _getUsage() );
      System.exit(0);
    }
    Properties rc = new Properties();
    try {
      rc = _getArgs(args);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println();
      System.err.println( _getUsage() );
      System.exit(1);
    }
    XmlExporter exporter = null;
    try {
      exporter = new XmlExporter(
        GrouperSession.start(
          SubjectFinder.findByIdentifier( rc.getProperty(RC_SUBJ) )
        ),
        XmlUtils.getUserProperties(LOG, rc.getProperty(RC_UPROPS) )
      );
      _handleArgs(exporter, rc);
      LOG.debug("Finished export to [" + rc.getProperty(RC_EFILE) + "]");
    }
    catch (Exception e) {
      LOG.fatal("unable to export to xml: " + e.getMessage());
      System.exit(1);
    }
    finally {
      if (exporter != null) {
        try {
          exporter.s.stop();
        }
        catch (SessionException eS) {
          LOG.error(eS.getMessage());
        }
      }
    }
    System.exit(0);
  } // public static void main(args)


  // PUBLIC INSTANCE METHODS //

  /**
   * Exports data for the entire repository
   * <p/>
   * @param   writer    Write XML here.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public void export(Writer writer)
    throws  GrouperException
  {
    LOG.info("starting export from root stem");
    this.xml = new XmlWriter(writer);
    try {
      this._export(StemFinder.findRootStem(s), true, false);
    }
    catch (CompositeNotFoundException eCNF) {
      throw new GrouperException(eCNF.getMessage(), eCNF);
    }
    catch (GroupNotFoundException eGNF)     {
      throw new GrouperException(eGNF.getMessage(), eGNF);
    }
    catch (IOException eIO)                 {
      throw new GrouperException(eIO.getMessage(), eIO);
    }
    catch (MemberNotFoundException eMNF)    {
      throw new GrouperException(eMNF.getMessage(), eMNF);
    }
    catch (SchemaException eS)              {
      throw new GrouperException(eS.getMessage(), eS);
    }
    catch (StemNotFoundException eNSNF)     {
      throw new GrouperException(eNSNF.getMessage(), eNSNF);
    }
    catch (SubjectNotFoundException eSNF)   {
      throw new GrouperException(eSNF.getMessage(), eSNF);
    }
    LOG.debug("export complete");
  } // public void export()

  /**
   * Export a Collection of stems, groups, members, subjects or memberships
   * <p/> 
   * @param   items
   * @param   info    allows you to indicate how the Collection was generated
   * @throws  Exception
   * @since   1.1.0
   */
  public synchronized void export(Collection items, String info) 
    throws  Exception 
  {
    LOG.debug("Start export of Collection:" + info);

    this.fromStem         = "_Z";
    Date    before        = _writeHeader();
    int     counter       = 0;
    String  origPadding   = "  ";
    String  padding       = "    ";

    if (_optionTrue("export.data")) {
      Iterator itemsIterator = items.iterator();
      this.xml.put(origPadding);
      this.xml.puts("<dataList>");
      Object obj;
      while (itemsIterator.hasNext())       {
        obj = itemsIterator.next();
        counter++;
        if      (obj instanceof Group)      {
          this._writeFullGroup( (Group) obj, padding );
        } 
        else if (obj instanceof Stem)       {
          Stem stem = (Stem) obj;
          this._writeBasicStemHeader(stem, padding);
          this._writeInternalAttributes(stem, padding);
          this._writeStemPrivs(stem, padding);
          this._writeBasicStemFooter(stem, padding);
        } 
        else if (obj instanceof Subject)    {
          if (counter == 1) {
            this.xml.puts("<exportOnly/>");
          }
          this._writeSubject( (Subject) obj, padding );
        } 
        else if (obj instanceof Member)     {
          if (counter == 1) {
            this.xml.puts("<exportOnly/>");
          }
          this._writeSubject( ((Member) obj).getSubject(), padding );
        } 
        else if (obj instanceof Membership) {
          if (counter == 1) {
            this.xml.puts("<exportOnly/>");
          }
          this._writeMembership( (Membership) obj, padding );
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
    this._writeFooter(before);
    LOG.debug("Finished export of Collection:" + info);
  } // public synchronized void export(items, info)

  /**
   * Export a single group
   * <p/>
   * @param   group
   * @param   relative  determines whether to export parent stems
   * @throws  Exception
   * @since   1.1.0
   */
  public void export(Group group, boolean relative) 
    throws  Exception 
  {
    LOG.debug("Start export of Group " + group.getName());
    this._export(group, relative, false);
    LOG.debug("Finished export of Group " + group.getName());
  } // public void export( group, relative)

  /**
   * Exports part of the repository
   * <p/> 
   * @param   stem          where to export from
   * @param   relative      determines whether to export parent stems
   * @param   includeParent should 'stem' be included or just the children
   * @throws  Exception
   * @since   1.1.0
   */
  public void export(Stem stem, boolean relative, boolean includeParent) 
    throws  Exception 
  {
    LOG.debug("Start export of Stem " + stem.getName());
    _export(stem, relative, includeParent);
    LOG.debug("Finished export of Stem " + stem.getName());
  } // public void export(stem, relative, includeParent)


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
    Subject subject, Owner gos, String privilege
  ) 
  {
    if (gos instanceof Group) {
      return hasImmediatePrivilege(subject, (Group) gos, privilege);
    }
    return hasImmediatePrivilege(subject, (Stem) gos, privilege);
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


  // PROTECTED INSTANCE METHODS //

  // @since   1.1.0
  protected Properties getOptions() {
    return (Properties) this.options.clone();
  } // protected Properties getOptions()


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static Properties _getArgs(String args[])
    throws  IllegalArgumentException,
            IllegalStateException
  {
    Properties rc = new Properties();

    String  arg;
    int     inputPos  = 0;
    int     pos       = 0;

    while (pos < args.length) {
      arg = args[pos];
      if (arg.startsWith("-")) {
        if (arg.equals("-id")) {
          if (rc.getProperty(RC_NAME) != null) {
            throw new IllegalArgumentException(XmlUtils.E_NAME_AND_UUID);
          }
          rc.setProperty(RC_UUID, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (arg.equals("-name")) {
          if (rc.getProperty(RC_UUID) != null) {
            throw new IllegalArgumentException(XmlUtils.E_NAME_AND_UUID);
          }
          rc.setProperty(RC_NAME, args[pos + 1]);
          pos += 2;
          continue;
        } 
        else if (arg.equals("-relative")) {
          rc.setProperty(RC_RELATIVE, "true");
          pos++;
          continue;
        } 
        else if (arg.equalsIgnoreCase("-includeparent")) {
          rc.setProperty(RC_PARENT, "true");
          pos++;
          continue;
        } else {
          throw new IllegalArgumentException(XmlUtils.E_UNKNOWN_OPTION + arg);
        }
      }
      switch (inputPos) {
      case 0:
        rc.setProperty(RC_SUBJ, arg);
        break;
      case 1:
        rc.setProperty(RC_EFILE, arg);
        break;
      case 2:
        rc.setProperty(RC_UPROPS, arg);
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
    return rc;
  } // private static Properties _getArgs(args)
    
  // @since   1.1.0
  private static Set _getListFieldsForGroup(Group g)
    throws  SchemaException 
  {
    Set       lists   = new LinkedHashSet();
    Field     defList = Group.getDefaultList();
    Field     f;
    GroupType type;
    Iterator  iterF;
    Iterator  iter    = g.getTypes().iterator();
    while (iter.hasNext()) {
      type  = (GroupType) iter.next();
      iterF = type.getFields().iterator();
      while (iterF.hasNext()) { 
        f = (Field) iterF.next();
        if (
              f.getType().equals(FieldType.LIST)
          &&  !f.equals(defList)
          &&  g.canReadField(f)
        )
        {
          lists.add(f.getName());
        }
      }
    }
    return lists;
  } // private static Set _getListFieldsForGroup(group)
  
  // @since   1.1.0
  private static String _getUsage() {
    return  "Usage:"
            + "args: -h,            Prints this message"
            + "args: subjectIdentifier [(-id <id>] | [-name <name>)] [-relative]"
            + "      [-includeParent] fileName [properties]"
            + ""
            + "  subjectIdentifier, Identifies a Subject 'who' will create a"
            + "                     GrouperSession"
            + "  -id,               The UUID of a Group or Stem to export"
            + "  -name,             The name of a Group or Stem to export"
            + "  -relative,         If id or name specified do not export parent"
            + "                     Stems"
            + "  -includeParent,    If id or name identifies a Stem export this"
            + "                     stem and child Stems or Groups"
            + "  filename,          The file where exported data will be written."
            + "                     Will overwrite existing files"
            + "  properties,        The name of a standard Java properties file"
            + "                     which configures the export. Check Javadoc for"
            + "                     a list of properties. If 'properties' is not "
            + "                     specified, XmlExporter will look for "
            + "                     'export.properties' in the working directory. "
            + "                     If this file does not exist XmlExporter will "
            + "                     look on the classpath. If 'properties' is not "
            + "                     specified and 'export.properties' cannot be "
            + "                     found, the export will fail."
            ;
  } // private static String _getUsage()

  // @since   1.1.0
  private static void _handleArgs(XmlExporter exporter, Properties rc) 
    throws  Exception
  {
    if (rc.getProperty(RC_UUID) == null && rc.getProperty(RC_NAME) == null) {
      exporter.export( new PrintWriter( new FileWriter( rc.getProperty(RC_EFILE) ) ) );
    } 
    else {
      Group group = null;
      Stem  stem  = null;
      if (rc.getProperty(RC_UUID) != null) {
        String uuid = rc.getProperty(RC_UUID);
        try {
          group = GroupFinder.findByUuid(exporter.s, uuid);
          LOG.debug("Found group with uuid [" + uuid + "]");
        } 
        catch (GroupNotFoundException eGNF) {
          // Look for stem instead
          try {
            stem = StemFinder.findByUuid(exporter.s, uuid);
            LOG.debug("Found stem with uuid [" + uuid + "]");
          } 
          catch (StemNotFoundException eNSNF) {
            throw new IllegalArgumentException(
              "Could not find group or stem with uuid [" + uuid + "]"
            );
          }
        }
      } 
      else {
        String name = rc.getProperty(RC_NAME);
        try {
          group = GroupFinder.findByName(exporter.s, name);
          LOG.debug("Found group with name [" + name + "]");
        } 
        catch (GroupNotFoundException eGNF) {
          // Look for stem instead
          try {
            stem = StemFinder.findByName(exporter.s, name);
            LOG.debug("Found stem with name [" + name + "]");
          } catch (StemNotFoundException eNSNF) {
            // No group or stem
            throw new IllegalArgumentException(
              "Could not find group or stem with name [" + name + "]"
            );
          }
        }
      }
      if (group != null) {
        exporter.export(
          group,
          Boolean.getBoolean(rc.getProperty(RC_RELATIVE))
        );
      } 
      else {
        exporter.export(
          stem,   
          Boolean.getBoolean(rc.getProperty(RC_RELATIVE)),
          Boolean.getBoolean(rc.getProperty(RC_PARENT))
        );
      }
    }
  } // private static void _handleArgs(exporter, rc)


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private synchronized void _export(Owner gos, boolean relative, boolean includeParent)
    throws  CompositeNotFoundException,
            GrouperException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    LOG.debug("Relative export="     + relative);
    LOG.debug("Include parent stem=" + includeParent);
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
      if      (includeParent || gos instanceof Group) {
        if (gos instanceof Group) {
          dummyStem = ( (Group) gos ).getParentStem();
        } 
        else {
          dummyStem = ( (Stem) gos ).getParentStem();
        }
      } 
      else if (!includeParent) {
        dummyStem = (Stem) gos;
      }
      fromStem = dummyStem.getName() + ":";
    }

    if (_optionTrue("export.data")) {
      _exportData(gos, padding);
    } 
    else {
      LOG.debug("export.data=false, so no data exported");
    }
    _writeExportParams(gos, padding);
    _writeFooter(before);
  } // private synchronized void _export(gos, relative, includeParent)

  // @since   1.1.0
  private void _exportData(Owner gos, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    LOG.debug("Writing repository data as XML");
    this.xml.puts(padding + "<data>");
    Stack stems = null;
    if (!isRelative) {
      stems = _getParentStems(gos);
    } 
    else {
      stems = new Stack();
      if (gos instanceof Group) {
        stems.push( (Group) gos);
        if (includeParent) {
          stems.push( ( (Group) gos ).getParentStem());
        }
      } 
      else {
        stems.push( (Stem) gos);
      }
    }
    _writeStems(stems, padding + "  ");
    this.xml.puts(padding + "</data>");
    LOG.debug("Finished repository data as XML");
  } // private void _exportData(gos, padding)

  // @since   1.0
  private String _fixGroupName(String name) {
    if (fromStem != null && name.startsWith(fromStem)) {
      name = name.replaceAll("^" + fromStem, XmlUtils.SPECIAL_STAR);
    }
    return name;
  } // private String _fixGroupName(name)

  // @since   1.0
  private Iterator _getExportAttributes(Subject subj) {
    String source = subj.getSource().getId();
    String type   = subj.getType().getName();
    String key    = "export.subject-attributes.source." + source + "." + type;
    String value  = options.getProperty(key);
    if (XmlUtils.isEmpty(value)) {
      key   = "export.subject-attributes.source." + source;
      value = options.getProperty(key);
    }
    if (XmlUtils.isEmpty(value)) {
      key   = "export.subject-attributes.type." + type;
      value = options.getProperty(key);
    }
    if (XmlUtils.isEmpty(value)) {
      return null;
    }
    if (XmlUtils.SPECIAL_STAR.equals(value)) {
      return subj.getAttributes().keySet().iterator();
    }
    StringTokenizer st  = new StringTokenizer(value);
    Set             res = new LinkedHashSet();
    while (st.hasMoreTokens()) {
      res.add(st.nextToken());
    }
    return res.iterator();
  } // private Iterator _getExportAttributes(subj)

  // @since   1.1.0
  private String _fixXmlAttribute(String value) {
    value = value.replaceAll("'", "&apos;");
    value = value.replaceAll("<", "&lt;");
    value = value.replaceAll(">", "&gt;");
    return value;
  } // private String _fixXmlAttribute(value)

  // @since   1.1.0
  private Stack _getParentStems(Owner gos) 
  {
    Stem  startStem = null;
    Stack stems     = new Stack();
    if (gos instanceof Group) {
      Group group = (Group) gos;
      stems.push(group);
      startStem = group.getParentStem();
    } 
    else {
      startStem = (Stem) gos;
    }
    stems.push(startStem);
    Stem parent = startStem;
    do {
      try {
        parent = parent.getParentStem();
        if (XmlUtils.isEmpty(parent.getExtension())) {
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

  // @since   1.1.0
  private boolean _optionTrue(String key) {
    if (XmlUtils.isEmpty(key)) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals(options.getProperty(key));
  } // private boolean _optionTrue(key)

  // @since   1.1.0
  private void _writeBasicStemFooter(Stem stem, String padding) 
    throws  IOException
  {
    this.xml.puts(padding + "</stem>");
    this.xml.puts(padding + "<!--/" + stem.getName() + "-->");
    this.xml.puts();
  } // private void _writeBasicStemFooter(stem, padding)

  // @since   1.1.0
  private void _writeBasicStemHeader(Stem stem, String padding) 
    throws  IOException
  {
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts("<!--" + stem.getName() + "-->");
    this.xml.put(padding);
    this.xml.puts(
      "<stem extension='" + this._fixXmlAttribute(stem.getExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts(
      "displayExtension='" + this._fixXmlAttribute(stem.getDisplayExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("name='" + this._fixXmlAttribute(stem.getName()) + "'");
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts(
      "displayName='" + this._fixXmlAttribute(stem.getDisplayName()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("id='" + this._fixXmlAttribute(stem.getUuid()) + "'>");

    this.xml.put(padding + "  ");
    this.xml.puts(
      "<description>" + this._fixXmlAttribute(stem.getDescription()) + "</description>"
    );

  } // private void _writeBasicStemHeader(stem, padding)

  // @since   1.1.0
  private void _writeComposite(Composite comp, String padding) 
    throws  GroupNotFoundException,
            IOException
  {
    String nPadding = padding + "  ";
    this.xml.put(padding);
    this.xml.puts("<composite>");
    _writeGroupRef(comp.getLeftGroup(), nPadding);
    this.xml.puts();
    this.xml.put(nPadding);
    this.xml.puts(
      "<compositeType>" + comp.getType().toString() + "</compositeType>"
    );
    this.xml.puts();
    _writeGroupRef(comp.getRightGroup(), nPadding);
    this.xml.put(padding);
    this.xml.puts("</composite>");
  } // private void _writeComposite(comp, padding)

  // @since   1.1.0
  private void _writeExportParams(
    Owner groupOrStem, String padding
  ) 
    throws  IOException
  {
    LOG.debug("Writing export params to XML");
    this.xml.put(padding);
    this.xml.puts("<exportParams>");
    this.xml.put(padding + "  ");
    if (groupOrStem instanceof Group) {
      this.xml.put("<node type='group'>");
    }
    else {
      this.xml.put("<node type='stem'>");
    }
    this.xml.put(groupOrStem.getName());
    this.xml.puts("</node>");
    this.xml.put(padding + "  ");
    this.xml.puts("<relative>" + isRelative + "</relative>");
    if (groupOrStem instanceof Stem) {
      this.xml.put(padding + "  ");
      this.xml.puts("<includeParent>" + includeParent + "</includeParent>");
    }
    this.xml.put(padding);
    this.xml.puts("</exportParams>");
  } // private void _writeExportParams(groupOrStem, padding)

  // @since   1.1.0
  private void _writeFieldMetaData(Field field, String padding) 
    throws  IOException
  {
    this.xml.puts();
    this.xml.put(padding);
    this.xml.puts(
      "<field name='" + this._fixXmlAttribute(field.getName()) + "'"
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
    throws  IOException
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

  // @since 1.1.0
  private void _writeFullGroup(Group group, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    LOG.debug("Writing group " + group.getName() + " to XML");
    this.xml.puts();
    this.xml.put(padding);
    // This was in a try/catch that ignored `Exception`.
    this.xml.puts("<!--" + group.getName() + "-->");
    this.xml.put(padding);
    this.xml.puts(
      "<group extension='" + this._fixXmlAttribute(group.getExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("       ");
    this.xml.puts(
      "displayExtension='" + this._fixXmlAttribute(group.getDisplayExtension()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("name='" + this._fixXmlAttribute(group.getName()) + "'");
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts(
      "displayName='" + this._fixXmlAttribute(group.getDisplayName()) + "'"
    );
    this.xml.put(padding);
    this.xml.put("      ");
    this.xml.puts("id='" + this._fixXmlAttribute(group.getUuid()) + "'>");

    this.xml.put(padding + "  ");
    this.xml.puts(
      "<description>" + this._fixXmlAttribute(group.getDescription()) + "</description>"
    );

    if (_optionTrue("export.group.internal-attributes")) {
      _writeInternalAttributes(group, padding + "  ");
    }
    if (_optionTrue("export.group.custom-attributes")) {
      Set       types     = group.getTypes();
      types.remove(this.baseType);
      if (!types.isEmpty()) {
        this.xml.puts(padding + "  <groupTypes>");
        Iterator  typesIterator = types.iterator();
        GroupType groupType;
        while (typesIterator.hasNext()) {
          groupType = (GroupType) typesIterator.next();
          this._writeGroupType(group, groupType, padding + "    ");
        }
        this.xml.puts(padding + "  </groupTypes>");
      }
    }

    List listFields = new ArrayList();
    if (_optionTrue("export.group.lists")) {
      listFields.addAll( _getListFieldsForGroup(group) );
    }
    if (_optionTrue("export.group.members")) {
      listFields.add(0, "members");
    }

    for (int i = 0; i < listFields.size(); i++) {
      LOG.debug("Writing list members for " + group.getName() + ": field=" + listFields.get(i));
      _writeListField(
        group, FieldFinder.find((String) listFields .get(i)), padding + "  "
      );
    }
    if (_optionTrue("export.privs.access")) {
      _writePrivileges("admin" , group.getAdmins()   , group, padding);
      _writePrivileges("update", group.getUpdaters() , group, padding);
      _writePrivileges("read"  , group.getReaders()  , group, padding);
      _writePrivileges("view"  , group.getViewers()  , group, padding);
      _writePrivileges("optin" , group.getOptins()   , group, padding);
      _writePrivileges("optout", group.getOptouts()  , group, padding);
    }
    this.xml.puts();
    this.xml.puts(padding + "</group>");
    // This was in a try/catch that ignored `Exception`.
    this.xml.puts(padding + "<!--/" + group.getName() + "-->");
    this.xml.puts();
    LOG.debug("Finished writing group " + group.getName() + " to XML");
  } // private void _writeFullGroup(group, padding)

  // @since   1.1.0
  private void _writeFullStem(Stem stem, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    LOG.debug("Writing Stem " + stem.getName() + " to XML");
    _writeBasicStemHeader(stem, padding);
    _writeInternalAttributes(stem, padding);
    _writeStemPrivs(stem, padding);
    _writeStemBody(stem, padding + "  ");
    _writeBasicStemFooter(stem, padding);
    LOG.debug("Finished writing Stem " + stem.getName() + " to XML");
  } // private void _writeFullStem(stem, padding)

  // @since   1.1.0
  private void _writeGroupRef(Group group, String padding) 
    throws  IOException
  {
    _writeGroupRef(group, padding, false);
  } // private void _writeGroupRef(group, padding)

  // @since   1.1.0
  private void _writeGroupRef(Group group, String padding, boolean writeAbsoluteName) 
    throws  IOException
  {
    this.xml.put(padding);
    this.xml.puts("<groupRef id='" + group.getUuid() + "'");
    this.xml.put(padding);
    String name = group.getName();
    if (!writeAbsoluteName) {
      name = this._fixGroupName(name);
    }
    this.xml.puts("        name='" + name + "'");
    this.xml.put(padding);
    this.xml.puts(" displayName='" + group.getDisplayName() + "'/>");
  } // private void _writeGroupRef(group, padding, writeAbsoluteName)

  // @since   1.1.0
  private void _writeGroupType(Group group, GroupType groupType, String padding)
    throws  IOException,
            SchemaException
  {
    this.xml.puts(
      padding + "<groupType name='" + this._fixXmlAttribute(groupType.getName()) + "'>"
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
        value = this._fixXmlAttribute(group.getAttribute(field.getName()));
        if (
            !XmlUtils.isEmpty(value)
            && ":description:extension:displayExtension:"
                .indexOf(":" + field.getName() + ":") == -1
        ) 
        {
          this.xml.put(padding);
          this.xml.puts(
            "<attribute name='"
            + this._fixXmlAttribute(field.getName()) + "'>" + value
            + "</attribute>"
          );
        }
      }
      catch (AttributeNotFoundException eANF) {
        LOG.error(eANF.getMessage());
      }
    }
    this.xml.puts(padding + "</groupType>");
  } // private void _writeGroupType(group, groupType, padding)

  // @since   1.1.0
  private void _writeInternalAttributes(Group group, String padding) 
    throws  IOException,
            SubjectNotFoundException
  {
    this.xml.puts();
    this.xml.puts(padding + "<internalAttributes>");
    this.xml.puts(
      padding + "  <internalAttribute name='parentStem'>"
      + this._fixXmlAttribute(group.getParentStem().getName())
      + "</internalAttribute>"
    );
    this.xml.puts(
      padding + "  <internalAttribute name='createSource'>"
      + this._fixXmlAttribute(group.getCreateSource())
      + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='createSubject'>");
    this._writeSubject(group.getCreateSubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
        padding 
      + "  <internalAttribute name='createTime'>"
      + this._fixXmlAttribute( Long.toString( group.getCreateTime().getTime() ) )
      + "</internalAttribute> "
      + "<!-- " + group.getCreateTime().toString() + " -->"
    );

    this.xml.puts(
      padding + "  <internalAttribute name='modifySource'>"
      + this._fixXmlAttribute(group.getModifySource())
      + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='modifySubject'>");
    this._writeSubject(group.getModifySubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
        padding 
      + "  <internalAttribute name='modifyTime'>"
      + this._fixXmlAttribute( Long.toString( group.getModifyTime().getTime() ) )
      + "</internalAttribute> "
      + "<!-- " + group.getModifyTime().toString() + " -->"
    );

    this.xml.puts(padding + "</internalAttributes>");
    this.xml.puts();
  } // private void _writeInternalAttributes(group, padding)

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
        "<groupTypeDef name='" + this._fixXmlAttribute(groupType.getName()) + "'>"
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
    if (_optionTrue("export.metadata")) {
      _writeMetaData(padding);
    }
    return before;
  } // private synchronized Date _writeHeader()

  // @since   1.1.0
  private void _writeInternalAttributes(Stem stem, String padding) 
    throws  IOException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if (!_optionTrue("export.stem.internal-attributes")) {
      return;
    }
    this.xml.puts();
    this.xml.puts(padding + "<internalAttributes>");
    this.xml.puts(
      padding + "  <internalAttribute name='parentStem'>"
      + this._fixXmlAttribute(stem.getParentStem().getName())
      + "</internalAttribute>"
    );
    this.xml.puts(
      padding + "  <internalAttribute name='createSource'>"
     + this._fixXmlAttribute(stem.getCreateSource())
     + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='createSubject'>");
    this._writeSubject(stem.getCreateSubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
        padding 
      + "  <internalAttribute name='createTime'>"
      + this._fixXmlAttribute( Long.toString( stem.getCreateTime().getTime() ) )
      + "</internalAttribute> "
      + "<!-- " + stem.getCreateTime().toString() + " -->"
    );

    this.xml.puts(
      padding + "  <internalAttribute name='modifySource'>"
      + this._fixXmlAttribute(stem.getModifySource())
      + "</internalAttribute>"
    );
    this.xml.puts(padding + "  <internalAttribute name='modifySubject'>");
    this._writeSubject(stem.getModifySubject(), padding + "    ");
    this.xml.puts(padding + "  </internalAttribute>");
    this.xml.puts(
        padding 
      + "  <internalAttribute name='modifyTime'>"
      + this._fixXmlAttribute( Long.toString( stem.getModifyTime().getTime() ) )
      + "</internalAttribute> "
      + "<!-- " + stem.getModifyTime().toString() + " -->"
    );

    this.xml.puts(padding + "</internalAttributes>");
    this.xml.puts();
  } // private void _writeInternalAttributes(stem, padding)

  // @since   1.1.0
  private void _writeListField(Group group, Field field, String padding) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    if (!group.canReadField(field)) {
      LOG.debug(
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
        "members".equals(field.getName()) && !_optionTrue("export.group.members.immediate-only")
      )
      || 
      (
        !"members".equals(field.getName()) && !_optionTrue("export.group.lists.immediate-only")
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
      "<list field='" + this._fixXmlAttribute(field.getName())
      + "'  groupType='"
      + this._fixXmlAttribute(field.getGroupType().getName()) + "'>"
    );
    if (isComposite) {
      Composite composite = CompositeFinder.findAsOwner(group);
      _writeComposite(composite, padding + "  ");
    }
    _writeMembers(members, group, field, padding + "  ");
    this.xml.put(padding);
    this.xml.puts(
      "</list> <!--/field=" + this._fixXmlAttribute(field.getName()) + "-->"
    );
  } // private void _writeListField(group, field, padding)

  // @since   1.1.0
  private void _writeMembers(Collection members, Group group, Field field, String padding)
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
      this._writeSubject(subj, " immediate='" + isImmediate + "' ", padding);
    }
  } // private void _writeMembers(members, group, field, padding)

  // @since   1.1.0
  private void _writeMembership(Membership membership, String padding) 
    throws  GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SubjectNotFoundException
  {
    boolean isImmediate = true;
    // How do composites fit in here?
    if (membership.getMship_type().equals(MembershipType.E)) {
      isImmediate = false;
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
    _writeGroupRef(membership.getGroup(), exPadding, true);
    this._writeSubject(membership.getMember().getSubject(), exPadding);
    this.xml.put(padding);
    this.xml.puts("</membership>");
  } // private void _writeMembership(membership, padding)

  // @since   1.1.0
  private void _writeMetaData(String padding)
    throws  GrouperException,
            IOException 
  {
    LOG.debug("Writing repository metadata as XML");
    this.xml.put(padding);
    this.xml.puts("<metadata>");
    _writeGroupTypesMetaData(padding + "  ");
    this._writeSubjectSourceMetaData(padding + "  ");
    this.xml.put(padding);
    this.xml.puts("</metadata>");
  } // private void _writeMetaData(padding)

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

  // @since   1.1.0
  private void _writePrivileges(String privilege, Set subjects, Owner gos, String padding)
    throws  IOException,
            MemberNotFoundException
  {
    if (subjects.size() == 1) {
      subjects.remove(sysUser);
    }
    if (subjects.isEmpty()) {
      LOG.debug("No privilegees with [" + privilege + "] for " + gos.getName());
      return;
    }

    LOG.debug("Writing privilegees with [" + privilege + "] for " + gos.getName());
    this.xml.puts();
    this.xml.put(padding);

    this.xml.puts("<privileges type='" + privilege + "'>");
    Iterator  subjIterator  = subjects.iterator();
    Subject   subject;
    boolean   isImmediate   = false;

    while (subjIterator.hasNext()) {
      subject     = (Subject) subjIterator.next();
      isImmediate = hasImmediatePrivilege(subject, gos, privilege);
      if (
        (!"GrouperSystem".equals(subject.getId()))
        && 
        (isImmediate || !_optionTrue("export.privs.immediate-only"))) 
      {
        this._writeSubject(subject, " immediate='" + isImmediate + "' ", padding + "  ");
      }
    }
    this.xml.put(padding);
    this.xml.puts("</privileges> <!--/privilege=" + privilege + "-->");

  } // private void _writePrivileges(privilege, subjects, gos, padding)

  // @since   1.1.0
  private void _writeStemBody(Stem stem, String padding) 
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
      _writeFullStem(childStem, padding);
    }

    Set       groups          = stem.getChildGroups();
    Iterator  groupsIterator  = groups.iterator();
    Group childGroup;
    while (groupsIterator.hasNext()) {
      childGroup = (Group) groupsIterator.next();
      _writeFullGroup(childGroup, padding);
    }
  } // private void _writeStemBody(stem, padding)

  // @since   1.1.0
  private void _writeStemPrivs(Stem stem, String padding) 
    throws  IOException,
            MemberNotFoundException
  {
    if (
        _optionTrue("export.privs.naming") 
       ) 
    {
      LOG.debug("Writing STEM privilegees for " + stem.getName());
      _writePrivileges("stem", stem.getStemmers(), stem, padding);
      _writePrivileges("create", stem.getStemmers(), stem, padding);
      LOG.debug("Writing CREATE privilegees for " + stem.getName());
    } 
    else {
      LOG.debug("Skipping naming privs for " + stem.getName());
    }
  } // private void _writeStemPrivs(stem, padding)

  // @since   1.1.0
  private void _writeStems(Stack stems, String padding) 
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
      _writeFullGroup( (Group) obj, padding );
      return;
    }

    Stem stem = (Stem) obj;

    if (stems.isEmpty()) {
      if (includeParent || writeStemsCounter > 1) {
        _writeFullStem(stem, padding + "  ");
      } 
      else {
        _writeStemBody(stem, padding + "  ");
      }
      return;
    } 
    else {
      _writeBasicStemHeader(stem, padding);
      if(_optionTrue("export.privs.for-parents")) {
      	_writeStemPrivs(stem, padding);
      }
      _writeStems(stems, padding + "  ");
      _writeBasicStemFooter(stem, padding);
    }
  } // private void _writeStems(stems, padding)

  // @since   1.1.0
  private void _writeSubject(Subject subj, String padding) 
    throws  IOException 
  {
    this._writeSubject(subj, "", padding);
  } // private void _writeSubject(subj, padding)

  // @since   1.1.0
  private void _writeSubject(Subject subj, String immediate, String padding) 
    throws  IOException 
  {
    this.xml.put(padding);
    String attrName = "id";
    String id       = null;
    if ("group".equals(subj.getType().getName())) {
      attrName  = "identifier";
      id        = this._fixGroupName(subj.getName());
    } else {
      id = subj.getId();
    }
    this.xml.put(
      "<subject " + attrName + "='" + this._fixXmlAttribute(id)
      + "' type='" + this._fixXmlAttribute(subj.getType().getName())
      + "' source='" + this._fixXmlAttribute(subj.getSource().getId())
      + "'" + immediate
    );
    if ("group".equals(subj.getType().getName())) {
      this.xml.put(" id='" + subj.getId() + "'");
    }
    Iterator exportAttrs = _getExportAttributes(subj);
    if (XmlUtils.isEmpty(exportAttrs)) {
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
    }
    this.xml.put(padding);
    this.xml.puts("</subject>");

  } // private void _writeSubject(subj, immediate, padding)

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
        "<source id='" + this._fixXmlAttribute(source.getId()) + "'"
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

} // public class XmlExporter

