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
 * @version $Id: XmlExporter.java,v 1.77 2006-10-18 14:17:27 blair Exp $
 * @since   1.0
 */
public class XmlExporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final String CF          = "export.properties"; 
  private static final Log    LOG         = LogFactory.getLog(XmlExporter.class);


  // PRIVATE INSTANCE VARIABLES //
  private GrouperSession  s;
  private String          fromStem      = null;
  // TODO 20061003 i still do not understand the entirety of how `includeParent` and `isRelative` interact
  private boolean         includeParent = true;
  private boolean         isRelative    = false;
  private Properties      options;
  private Date            startTime;
  private XmlWriter       xml           = null;


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
  public XmlExporter(GrouperSession s, Properties userOptions) 
    throws  GrouperRuntimeException
  {
    try {
      this.options  = XmlUtils.getSystemProperties(LOG, CF);
    }
    catch (IOException eIO) {
      throw new GrouperRuntimeException(eIO.getMessage(), eIO);
    }
    this.options.putAll(userOptions); 
    this.s          = s;
    this.startTime  = new Date();
  } // public XmlExporter(s, userOptions)

  
  // MAIN //
  /**
   * Export Groups Registry to XML output.
   * <p/>
   * @since   1.1.0
   */
  public static void main(String args[]) {
    if (XmlArgs.wantsHelp(args)) {
      System.out.println( _getUsage() );
      System.exit(0);
    }
    Properties rc = new Properties();
    try {
      rc = XmlArgs.getXmlExportArgs(args);
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
          SubjectFinder.findByIdentifier( rc.getProperty(XmlArgs.RC_SUBJ) )
        ),
        XmlUtils.getUserProperties(LOG, rc.getProperty(XmlArgs.RC_UPROPS) )
      );
      _handleArgs(exporter, rc);
      LOG.debug("Finished export to [" + rc.getProperty(XmlArgs.RC_EFILE) + "]");
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
    this.xml            = new XmlWriter(writer);
    this.includeParent  = false;
    this.isRelative     = false;
    this._export(StemFinder.findRootStem(s));
  } // public void export()

  /**
   * Export a single group.
   * <p/>
   * @param   writer        Write XML here.
   * @param   g             Export this group.
   * @param   relative      If true export in a format suitable for relocating within the Groups Registry.
   * @param   includeParent Include parent stem in export.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public void export(Writer writer, Group g, boolean relative, boolean includeParent) 
    throws  GrouperException 
  {
    this.xml            = new XmlWriter(writer);
    this.includeParent  = includeParent;
    this.isRelative     = relative;
    this._export(g);
  } // public void export(writer, g, relative, includeParent)

  /**
   * Export a single stem.
   * <p/>
   * @param   writer        Write XML here.
   * @param   ns            Export this stem.
   * @param   relative      If true export in a format suitable for relocating within the Groups Registry.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public void export(Writer writer, Stem ns, boolean relative)
    throws  GrouperException 
  {
    this.xml            = new XmlWriter(writer);
    this.isRelative     = relative;
    if (ns.isRootStem()) {
      this.includeParent = false; // TODO 20061010 includeParent is still sort of magic to /me
    }
    this._export(ns);
  } // public void export(writer, ns, relative)

  /**
   * Export a Collection of Stems, Groups, Members, Subjects or Memberships.
   * <p>
   * <b>NOTE:</b> <tt>XmlImporter</tt> cannot currently import the XML generated
   * by this method.
   * </p>
   * @param   writer  Write XML here.
   * @param   items   Collection to export.
   * @param   msg     Comment to indicate how collection was generated.
   * @throws  GrouperException
   * @since   1.1.0
   */
  public void export(Writer writer, Collection items, String msg) 
    throws  GrouperException 
  {
    this.xml = new XmlWriter(writer);
    this._exportCollection(items, msg);
  } // public void _export(items, msg)


  // PROTECTED INSTANCE METHODS //

  // @since   1.1.0
  protected Properties getOptions() {
    return (Properties) this.options.clone();
  } // protected Properties getOptions()


  // PRIVATE CLASS METHODS //

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
    return  "Usage:"                                                                + GrouperConfig.NL
            + "args: -h,            Prints this message"                            + GrouperConfig.NL
            + "args: subjectIdentifier [(-id <id>] | [-name <name>)] [-relative]"   + GrouperConfig.NL
            + "      [-includeParent] fileName [properties]"                        + GrouperConfig.NL
            +                                                                         GrouperConfig.NL
            + "  subjectIdentifier, Identifies a Subject 'who' will create a"       + GrouperConfig.NL
            + "                     GrouperSession"                                 + GrouperConfig.NL
            + "  -id,               The UUID of a Group or Stem to export"          + GrouperConfig.NL
            + "  -name,             The name of a Group or Stem to export"          + GrouperConfig.NL
            + "  -relative,         If id or name specified do not export parent"   + GrouperConfig.NL
            + "                     Stems"                                          + GrouperConfig.NL
            + "  -includeParent,    If id or name identifies a Stem export this"    + GrouperConfig.NL
            + "                     stem and child Stems or Groups"                 + GrouperConfig.NL
            + "  filename,          The file where exported data will be written."  + GrouperConfig.NL
            + "                     Will overwrite existing files"                  + GrouperConfig.NL
            + "  properties,        The name of a standard Java properties file "   + GrouperConfig.NL
            + "                     which configures the export. Check Javadoc for" + GrouperConfig.NL
            + "                     a list of properties. If 'properties' is not "  + GrouperConfig.NL
            + "                     specified, XmlExporter will look for "          + GrouperConfig.NL
            + "                     'export.properties' in the working directory. " + GrouperConfig.NL
            + "                     If this file does not exist XmlExporter will "  + GrouperConfig.NL
            + "                     look on the classpath. If 'properties' is not " + GrouperConfig.NL
            + "                     specified and 'export.properties' cannot be "   + GrouperConfig.NL
            + "                     found, the export will fail."                   + GrouperConfig.NL
            ;
  } // private static String _getUsage()

  // @since   1.1.0
  private static void _handleArgs(XmlExporter exporter, Properties rc) 
    throws  Exception
  {
    Writer writer = new PrintWriter( new FileWriter( rc.getProperty(XmlArgs.RC_EFILE) ));
    if (rc.getProperty(XmlArgs.RC_UUID) == null && rc.getProperty(XmlArgs.RC_NAME) == null) {
      exporter.export(writer);
    } 
    else {
      Group group = null;
      Stem  stem  = null;
      if (rc.getProperty(XmlArgs.RC_UUID) != null) {
        String uuid = rc.getProperty(XmlArgs.RC_UUID);
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
        String name = rc.getProperty(XmlArgs.RC_NAME);
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
          writer, group,
          Boolean.valueOf(rc.getProperty(XmlArgs.RC_RELATIVE)),
          Boolean.valueOf(rc.getProperty(XmlArgs.RC_PARENT))
        );
      } 
      else {
        exporter.export(writer, stem,   Boolean.valueOf(rc.getProperty(XmlArgs.RC_RELATIVE)));
      }
    }
  } // private static void _handleArgs(exporter, rc)


  // PRIVATE INSTANCE METHODS //

  // @since   1.1.0
  private void _export(Owner o)
    throws  GrouperException
  {
    try {
      this._setFromStem(o);
      this._writeHeader();
      this._writeMetaData();
      this._writeData(o);
      this._writeExportParams(o);
      this._writeFooter();
    }
    catch (CompositeNotFoundException eCNF) {
      throw new GrouperException(eCNF.getMessage(), eCNF);
    }
    catch (GroupNotFoundException eGNF)     {
      throw new GrouperException(eGNF.getMessage(), eGNF);
    }
    catch (IOException eIO) {
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
  } // private void _export(o)

  // @since   1.1.0
  private void _exportCollection(Collection c, String msg) 
    throws  GrouperException
  {
    try {
      // TODO 20061010 refactor! refactor! refactor!
      this.fromStem         = "_Z";
      this._writeHeader();
      int     counter       = 0;
      if (this._isDataExportEnabled()) {
        this.xml.indent();
        this.xml.puts("<dataList>");
        Iterator it = c.iterator();
        Object obj;
        while (it.hasNext())       {
          obj = it.next();
          counter++;
          if      (obj instanceof Group)      {
            this._writeGroup( (Group) obj);
          } 
          else if (obj instanceof Stem)       {
            Stem stem = (Stem) obj;
            this._writeStemHeader(stem);
            this._writeInternalAttributes(stem);
            this._writeStemPrivs(stem);
            this._writeStemFooter(stem);
          } 
          else if (obj instanceof Subject)    {
            if (counter == 1) {
              this.xml.puts("<exportOnly/>");
            }
            this._writeSubject( (Subject) obj);
          } 
          else if (obj instanceof Member)     {
            if (counter == 1) {
              this.xml.puts("<exportOnly/>");
            }
            this._writeSubject( ((Member) obj).getSubject());
          } 
          else if (obj instanceof Membership) {
            if (counter == 1) {
              this.xml.puts("<exportOnly/>");
            }
            this._writeMembership( (Membership) obj);
          } 
          else {
            LOG.error("Don't know about exporting " + obj);
          }
          this.xml.puts();
        }
        this.xml.puts("</dataList>");
        this.xml.undent();
        this.xml.puts();
      }
      this.xml.indent();
      this.xml.puts("<exportComments><![CDATA[");
      this.xml.indent();
      this.xml.puts(msg);
      this.xml.undent();
      this.xml.puts("]]></exportComments>");
      this.xml.undent();
      this.xml.puts();
      this._writeFooter();
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
  } // private void _exportCollection(c, msg)

  // @since   1.0
  private String _fixGroupName(String name) {
    if (fromStem != null && name.startsWith(fromStem)) {
      name = name.replaceAll("^" + fromStem, XmlUtils.SPECIAL_STAR);
    }
    return name;
  } // private String _fixGroupName(name)

  // @since   1.1.0
  private String _fixXmlAttribute(String value) {
    value = value.replaceAll("'", "&apos;");
    value = value.replaceAll("<", "&lt;");
    value = value.replaceAll(">", "&gt;");
    return value;
  } // private String _fixXmlAttribute(value)

  // @since   1.0
  private Iterator _getExportAttributes(Subject subj) {
    String source = subj.getSource().getId();
    String type   = subj.getType().getName();
    String key    = "export.subject-attributes.source." + source + "." + type;
    String value  = options.getProperty(key);
    // TODO 20061018 i'm not keen on these repetitive "if"s
    if (Validator.isNullOrBlank(value)) {
      key   = "export.subject-attributes.source." + source;
      value = options.getProperty(key);
    }
    if (Validator.isNullOrBlank(value)) {
      key   = "export.subject-attributes.type." + type;
      value = options.getProperty(key);
    }
    if (Validator.isNullOrBlank(value)) {
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
  private Stack _getStackToWrite(Owner o) {
    // If I understand correctly the code behaves in this manner because we may
    // need to export parents in order to export a child but we do NOT want to
    // export ALL children of that parent in that case.  Alas.
    Stack stems = null;
    if (!this.isRelative) {
      stems = this._getParentStems(o);
    } 
    else {
      stems = new Stack();
      if (o instanceof Group) {
        stems.push( (Group) o);
        if (this.includeParent) {
          stems.push( ( (Group) o ).getParentStem());
        }
      } 
      else {
        stems.push( (Stem) o);
      }
    }
    return stems;
  } // private Stack _getStackToWrite(o)

  // @since   1.1.0
  private boolean _isAccessPrivExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.privs.access");
  } // private boolean _isAccessPrivExportEnabled()
  
  // @since   1.1.0
  private boolean _isDataExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.data");
  } // private boolean _isDataExportEnabled()

  // @since   1.1.0
  private boolean _isGroupInternalAttrsExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.group.internal-attributes");
  } // private boolean _isGroupInternalAttrsExportEnabled()

  // @since   1.1.0
  private boolean _isGroupListImmediateOnlyExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.group.lists.immediate-only");
  } // private boolean _isGroupListImmediateOnlyExportEnabled()

  // @since   1.1.0
  private boolean _isGroupMemberImmediateOnlyExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.group.members.immediate-only");
  } // private boolean _isGroupMemberImmediateOnlyExportEnabled()

  // @since   1.1.0
  private boolean _isMetadataExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.metadata");
  } // private boolean _isMetadataExportEnabled()

  // @since   1.1.0
  private boolean _isNamingPrivExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.privs.naming");
  } // private boolean _isNamingPrivExportEnabled()

  // @since   1.1.0
  private boolean _isParentPrivsExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.privs.for-parents");
  } // private boolean _isParentPrivsExportEnabled()

  // @since   1.1.0
  private boolean _isStemInternalAttrsExportEnabled() {
    return XmlUtils.getBooleanOption(this.options, "export.stem.internal-attributes");
  } // private boolean _isStemInternalAttrsExportEnabled()

  // @since   1.1.0
  private String _getParentStemName(Owner o) 
    throws  StemNotFoundException
  {
    String parent = GrouperConfig.EMPTY_STRING;
    if (o instanceof Group) {
      parent = ( (Group) o).getParentStem().getName();
    }
    else {
      parent = ( (Stem) o).getParentStem().getName();
    }
    return parent;
  } // private String _getParentStemName(o)

  // @since   1.1.0
  private Stack _getParentStems(Owner o) 
  {
    Stem  startStem = null;
    Stack stems     = new Stack();
    if (o instanceof Group) {
      Group group = (Group) o;
      stems.push(group);
      startStem = group.getParentStem();
    } 
    else {
      startStem = (Stem) o;
    }
    stems.push(startStem);
    Stem parent = startStem;
    do {
      try {
        parent = parent.getParentStem();
        if (Validator.isNullOrBlank( parent.getExtension() ) ) {
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
  } // private Stack _getParentStems(o)

  // @since   1.1.0
  // TODO 20061003 deprecate
  private boolean _optionTrue(String key) {
    if (Validator.isNullOrBlank(key)) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals(options.getProperty(key));
  } // private boolean _optionTrue(key)

  // @since   1.1.0
  private void _setFromStem(Owner o)
    throws  StemNotFoundException
  {
    if (!this.isRelative) {
      this.fromStem = null;
    }
    else {
      Stem anchor = null;
      if (o instanceof Group)       {
        anchor = ( (Group) o ).getParentStem();

      }
      else  if (this.includeParent) {
        anchor = ( (Stem) o ).getParentStem();
      }
      else                          {
        anchor = (Stem) o;
      }
      this.fromStem = anchor.getName() + ":";
    }
  } // private void _setFromStem(o)

  // @since   1.1.0
  private void _writeComposite(Composite comp) 
    throws  GroupNotFoundException,
            IOException
  {
    this.xml.puts("<composite>");
    this._writeGroupRef(comp.getLeftGroup());
    this.xml.puts();
    this.xml.puts(
      "<compositeType>" + comp.getType().toString() + "</compositeType>"
    );
    this.xml.puts();
    this._writeGroupRef(comp.getRightGroup());
    this.xml.puts("</composite>");
  } // private void _writeComposite(comp)

  // @since   1.1.0
  private void _writeExportParams(Owner o)
    throws  IOException
  {
    this.xml.indent();
    this.xml.puts("<exportParams>");
    this.xml.indent();
    String s = "<node type='";
    if (o instanceof Group) {
      s += "group'>";
    }
    else {
      s += "stem'>";
    }
    this.xml.puts( s + o.getName() + "</node>" );
    this.xml.puts("<relative>" + isRelative + "</relative>");
    if (o instanceof Stem) {
      this.xml.puts("<includeParent>" + includeParent + "</includeParent>");
    }
    this.xml.undent();
    this.xml.puts("</exportParams>");
    this.xml.undent();
    this.xml.puts();
  } // private void _writeExportParams(o)

  // @since   1.1.0
  private void _writeFieldMetaData(Field f) 
    throws  IOException
  {
    this.xml.indent();
    this.xml.puts("<field name="  + U.q( this._fixXmlAttribute(f.getName()))  );
    this.xml.indent();
    this.xml.puts("required="     + U.q( f.getRequired() )                    );
    this.xml.puts("type="         + U.q( f.getType().toString() )             );
    this.xml.puts("readPriv="     + U.q( f.getReadPriv().toString() )         );
    this.xml.puts("writePriv="    + U.q( f.getWritePriv().toString() )        );
    this.xml.undent();
    this.xml.puts("/>");
    this.xml.undent();
  } // private void _writeFieldMetaData(f)

  // @since   1.1.0
  private void _writeFooter()
    throws  IOException
  {
    Date    now       = new Date();
    long    duration  = (now.getTime() - this.startTime.getTime()) / 1000;
    this.xml.indent();
    this.xml.puts("<exportInfo>");
    this.xml.indent();
    this.xml.puts("<start>" + startTime + "</start>");
    this.xml.puts("<end>" + now + "</end>");
    this.xml.puts("<duration>" + duration + "</duration>");
    this._writeOptions();
    this.xml.undent();
    this.xml.puts("</exportInfo>");
    this.xml.undent();
    this.xml.puts("</registry>");
    this.xml.puts();
    this.xml.close();
  } // private _writeFooter()

  // @since 1.1.0
  private void _writeGroup(Group g) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    this._writeGroupHeader(g);
    this._writeInternalAttributes(g);
    this._writeAttributes(g);
    this._writeLists(g);
    this._writeGroupPrivs(g);
    this._writeGroupFooter(g);
  } // private void _writeGroup(g)

  // @since   1.1.0
  // TODO 20061003 DRY _writeStemFooter()
  private void _writeGroupFooter(Group g) 
    throws  IOException
  {
    this.xml.puts("</group>");
    this.xml.puts( this.xml.comment( U.q(g.getName() ) ) );
    this.xml.undent(); // Undent the surplus indent from the header
    this.xml.puts();
  } // private void _writeGroupFooter(g)

  // @since   1.1.0
  // TODO 20061003 DRY _writeStemHeader()
  private void _writeGroupHeader(Group g)  
    throws  IOException
  {
    this.xml.puts();
    this.xml.indent();
    this.xml.puts( this.xml.comment( U.q( g.getName() ) ) );
    this.xml.puts("<group extension=" + U.q( this._fixXmlAttribute(g.getExtension()) )         );
    this.xml.indent();
    this.xml.puts("displayExtension=" + U.q( this._fixXmlAttribute(g.getDisplayExtension()) )  );
    this.xml.puts("name="             + U.q( this._fixXmlAttribute(g.getName()) )              );
    this.xml.puts("displayName="      + U.q( this._fixXmlAttribute(g.getDisplayName()) )       );
    this.xml.puts("id="               + U.q( this._fixXmlAttribute(g.getUuid()) )              );
    this.xml.undent();
    this.xml.puts(">");
    this.xml.indent();
    this.xml.puts("<description>" + this._fixXmlAttribute(g.getDescription()) + "</description>");
    this.xml.undent();
    // Don't fully undent
  } // private void _writeGroupHeader(g)

  // @since   1.1.0
  private void _writeGroupPrivs(Group g) 
    throws  IOException,
            MemberNotFoundException
  {
    if (this._isAccessPrivExportEnabled()) {
      this._writePrivileges("admin" , g.getAdmins()   , g);
      this._writePrivileges("update", g.getUpdaters() , g);
      this._writePrivileges("read"  , g.getReaders()  , g);
      this._writePrivileges("view"  , g.getViewers()  , g);
      this._writePrivileges("optin" , g.getOptins()   , g);
      this._writePrivileges("optout", g.getOptouts()  , g);
    } 
  } // private void _writeGroupPrivs(g)

  // @since   1.1.0
  private void _writeGroupRef(Group group) 
    throws  IOException
  {
    this._writeGroupRef(group, false);
  } // private void _writeGroupRef(group)

  // @since   1.1.0
  private void _writeGroupRef(Group group, boolean writeAbsoluteName) 
    throws  IOException
  {
    this.xml.puts("<groupRef id='" + group.getUuid() + "'");
    String name = group.getName();
    if (!writeAbsoluteName) {
      name = this._fixGroupName(name);
    }
    this.xml.puts("        name='" + name + "'");
    this.xml.puts(" displayName='" + group.getDisplayName() + "'/>");
  } // private void _writeGroupRef(group, writeAbsoluteName)

  // @since   1.1.0
  private void _writeGroupType(Group g, GroupType gt)
    throws  IOException,
            SchemaException
  {
    this.xml.indent();
    this.xml.puts("<groupType name=" + U.q( this._fixXmlAttribute(gt.getName()) ) + ">");
    Iterator  it  = gt.getFields().iterator();
    while (it.hasNext()) {
      this._writeGroupTypeField(g, (Field) it.next());
    }
    this.xml.puts("</groupType>");
    this.xml.undent();
    this.xml.puts();
  } // private void _writeGroupType(group, groupType)

  // @since   1.1.0
  private void _writeGroupTypeField(Group g, Field f) 
    throws  IOException,
            SchemaException
  {
    if ( !f.getType().equals(FieldType.LIST) && g.canReadField(f) ) {
      try {
        String val = this._fixXmlAttribute(g.getAttribute(f.getName()));
        if (
                Validator.isNotNullOrBlank(val)
            &&  ":description:extension:displayExtension:".indexOf(":" + f.getName() + ":") == -1
        ) 
        {
          this.xml.indent();
          this.xml.puts(
            "<attribute name='"
            + this._fixXmlAttribute(f.getName()) + "'>" + val
            + "</attribute>"
          );
          this.xml.undent();
        }
      }
      catch (AttributeNotFoundException eANF) {
        LOG.error(eANF.getMessage());
      }
    }
  } // private void _writeGroupTypeField(g, f)

  // @since   1.1.0
  private void _writeAttributes(Group g) 
    throws  IOException,
            SchemaException
  {
    if (this._optionTrue("export.group.custom-attributes")) {
      Set types = g.getRemovableTypes(); 
      if (!types.isEmpty()) {
        this.xml.indent();
        this.xml.puts("<groupTypes>");
        Iterator it = types.iterator();
        while (it.hasNext()) {
          this._writeGroupType(g, (GroupType) it.next());
        }
        this.xml.puts("</groupTypes>");
        this.xml.undent();
      }
    }
  } // private void _writeAttributes(g)

  // @since   1.1.0
  private void _writeData(Owner o) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if (this._isDataExportEnabled()) {
      this.xml.indent();
      this.xml.puts("<data>");
      this._writeOwnerStack( this._getStackToWrite(o) );
      this.xml.puts("</data>");
      this.xml.undent();
      LOG.debug("Finished repository data as XML");
    }
  } // private void _writeData(o)

  // @since   1.1.0 
  private void _writeLists(Group g) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,  
            SubjectNotFoundException
  {
    List lists = new ArrayList();
    if (this._optionTrue("export.group.members")) {
      lists.add("members");
    }
    if (this._optionTrue("export.group.lists")) {
      lists.addAll( _getListFieldsForGroup(g) );
    }
    Iterator it = lists.iterator();
    while (it.hasNext()) {
      this._writeList(g, FieldFinder.find( (String) it.next() ) );
    }
  } // private void _writeLists(g)

  // @since   1.1.0
  private void _writeGroupType(GroupType gt) 
    throws  IOException
  {
    this.xml.indent();
    this.xml.puts("<groupTypeDef name=" + U.q( this._fixXmlAttribute(gt.getName()) ) + ">");
    Iterator it = gt.getFields().iterator();
    while (it.hasNext()) {
      this._writeFieldMetaData( (Field) it.next() );
    }
    this.xml.puts("</groupTypeDef>");
    this.xml.undent();
  } // private void _writeGroupTypesMetaData(gt)

  // @since   1.1.0
  private void _writeGroupTypesMetaData()
    throws  IOException 
  {
    Set types = GroupTypeFinder.findAll();
    if (types.isEmpty()) {
      return;
    }
    this.xml.indent();
    this.xml.puts("<groupTypesMetaData>");
    Iterator  it  = types.iterator();
    while (it.hasNext()) {
      this._writeGroupType( (GroupType) it.next() );
    }
    this.xml.puts("</groupTypesMetaData>");
    this.xml.undent();
  } // private void _writeGroupTypesMetaData()

  // @since   1.1.0
  private void _writeHeader()
    throws  GrouperException,
            IOException 
  {
    this.xml.puts("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    this.xml.puts("<registry>");
  } // private void _writeHeader()

  // @since   1.1.0
  private void _writeInternalAttribute(String attr, Long l)
    throws  IOException
  {
    Date d = new Date(l);
    this.xml.indent();
    this.xml.puts(
        "<internalAttribute name=" + U.q(attr) + ">" 
      + Long.toString( d.getTime() ) 
      + "</internalAttribute> " 
      + this.xml.comment( d.toString() )
    ); 
    this.xml.undent();
  } // private void _writeInternalAttribute(attr, d)

  // @since   1.1.0
  private void _writeInternalAttribute(String attr, String val)
    throws  IOException
  {
    if (!Validator.isNotNullOrBlank(val)) {
      // Since I'm now using the internal methods to access the attr values I
      // need to be more careful about NULLs
      val = GrouperConfig.EMPTY_STRING;
    }
    this.xml.indent();
    this.xml.puts(
        "<internalAttribute name=" + U.q(attr) + ">" 
      + this._fixXmlAttribute(val) + "</internalAttribute>"
    ); 
    this.xml.undent();
  } // private void _writeInternalAttribute(attr, val)

  // @since   1.1.0
  //private void _writeInternalAttribute(String attr, Subject subj) 
  private void _writeInternalAttribute(String attr, Member m) 
    throws  IOException,
            SubjectNotFoundException
  {
    Subject subj    = m.getSubject();
    String  idAttr  = "id";
    String  id      = subj.getId();
    if (subj.getType().getName().equals("group")) {
      idAttr  = "identifier";
      id      = this._fixGroupName(subj.getName());
    }
    String  txt   = 
        "<subject " + idAttr + "=" + U.q( this._fixXmlAttribute(id) )
      + " type="    + U.q( this._fixXmlAttribute(subj.getType().getName()) )
      + " source="  + U.q( this._fixXmlAttribute(subj.getSource().getId()) );
    if (idAttr.equals("identifier")) {
      txt += " id=" + U.q( subj.getId() );
    }
    txt += "/>";
    this.xml.indent();
    this.xml.puts("<internalAttribute name=" + U.q(attr) + ">");
    this.xml.indent();
    this.xml.puts(txt);
    this.xml.undent();
    this.xml.puts("</internalAttribute>");
    this.xml.undent();
  } // private void _writeInternalAttribute(attr, subj, comment)

  // @since   1.1.0
  private void _writeInternalAttributes(Owner o) 
    throws  IOException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if (
      ( o instanceof Group && this._isGroupInternalAttrsExportEnabled() )
      ||
      ( o instanceof Stem  && this._isStemInternalAttrsExportEnabled()  )
    )
    {
      this.xml.indent();
      this.xml.puts("<internalAttributes>");
      this._writeInternalAttribute( "parentStem"    , this._getParentStemName(o)  );
      this._writeInternalAttribute( "createSource"  , o.getCreate_source()        );
      this._writeInternalAttribute( "createSubject" , o.getCreator_id()           );
      this._writeInternalAttribute( "createTime"    , o.getCreate_time()          );
      this._writeInternalAttribute( "modifySource"  , o.getModify_source()        );
      this._writeInternalAttribute( "modifySubject" , o.getModifier_id()          );
      this._writeInternalAttribute( "modifyTime"    , o.getModify_time()          );
      this.xml.puts("</internalAttributes>");
      this.xml.undent();
      this.xml.puts();
    }
  } // private void _writeInternalAttributes(o)

  // @since   1.1.0
  private void _writeList(Group g, Field f) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    // TODO 20061005 refactor: this is ugly
    if (g.canReadField(f)) {
      boolean isComposite = false;
      Set     membersSet  = null;
      if (f.getName().equals("members") && g.hasComposite()) {
        isComposite = true;
        membersSet  = new HashSet();
      } 
      else {
        membersSet = g.getImmediateMemberships(f);
      }
      Collection members = new ArrayList();
      members.addAll(membersSet);
      if (
        ( f.getName().equals("members")   && !this._isGroupMemberImmediateOnlyExportEnabled() )
        || 
        ( !f.getName().equals("members")  && !this._isGroupListImmediateOnlyExportEnabled() )
      )
      {
        members.addAll( g.getEffectiveMemberships(f) );
        if (f.getName().equals("members") && g.hasComposite()) {
          members.addAll( g.getCompositeMemberships() );
        }
      }
      if (members.isEmpty() && !isComposite) {
        return;
      }
      this.xml.indent();
      this.xml.puts(
          "<list field="  + U.q( this._fixXmlAttribute(f.getName()) )
        + " groupType="   + U.q( this._fixXmlAttribute(f.getGroupType().getName()) )
        + ">"
      );
      if (isComposite) {
        this._writeComposite( CompositeFinder.findAsOwner(g) );
      }
      this._writeMembers(members, g, f);
      this.xml.puts(
        "</list> " + this.xml.comment( U.q( this._fixXmlAttribute(f.getName() ) ) ) 
      );
      this.xml.undent();
      this.xml.puts();
    }
  } // private void _writeListField(g, f)

  // @since   1.1.0
  private void _writeMembers(Collection members, Group group, Field field)
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
      this._writeSubject(subj, " immediate='" + isImmediate + "' ");
    }
  } // private void _writeMembers(members, group, field)

  // @since   1.1.0
  private void _writeMembership(Membership membership) 
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

    this.xml.puts("<membership>");
    this.xml.puts("<depth>" + membership.getDepth() + "</depth>");
    this.xml.puts("<listName>" + membership.getList().getName() + "</listName>");
    this.xml.puts("<immediate>" + isImmediate + "</immediate>");
    _writeGroupRef(membership.getGroup(), true);
    this._writeSubject(membership.getMember().getSubject());
    this.xml.puts("</membership>");
  } // private void _writeMembership(membership)

  // @since   1.1.0
  private void _writeMetaData()
    throws  GrouperException,
            IOException 
  {
    if (this._isMetadataExportEnabled()) {
      this.xml.indent();
      this.xml.puts("<metadata>");
      this._writeGroupTypesMetaData();
      this.xml.puts();
      this._writeSubjectSourcesMetaData();
      this.xml.puts("</metadata>");
      this.xml.undent();
    }
  } // private void _writeMetaData()

  // @since   1.1.0
  private void _writeOptions() 
    throws  IOException
  {
    this.xml.puts("<options>");
    this.xml.indent();
    String    key;
    List      opts  = new ArrayList( options.keySet() );
    Collections.sort(opts);
    Iterator  it    = opts.iterator();
    while (it.hasNext()) {
      key = (String) it.next();
      this.xml.puts(
        "<option key=" + U.q(key) + ">" + options.getProperty(key) + "</option>"
      );
    }
    this.xml.undent();
    this.xml.puts("</options>");
  } // private void _writeOptions()

  // @since   1.1.0
  private void _writePrivileges(String privilege, Set subjects, Owner o)
    throws  IOException,
            MemberNotFoundException
  {
    // TODO 20061005 refactor: aesthetically this is wrong
    if (subjects.size() == 1) {
      subjects.remove( SubjectFinder.findRootSubject() );
    }
    this.xml.puts();
    this.xml.puts("<privileges type='" + privilege + "'>");
    Subject   subject;
    boolean   isImmediate = false;
    Iterator  it          = subjects.iterator();
    while (it.hasNext()) {
      subject     = (Subject) it.next();
      isImmediate = XmlUtils.hasImmediatePrivilege(subject, o, privilege);
      if (
        (!subject.getId().equals("GrouperSystem"))
        && 
        (isImmediate || !_optionTrue("export.privs.immediate-only"))) 
      {
        this._writeSubject(subject, " immediate='" + isImmediate + "' ");
      }
    }
    this.xml.puts("</privileges> " + this.xml.comment(privilege));
  } // private void _writePrivileges(privilege, subjects, o)

  // @since   1.1.0
  private void _writeStem(Stem ns) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    this._writeStemHeader(ns);
    this._writeInternalAttributes(ns);
    this._writeStemPrivs(ns);
    this._writeStemChildren(ns);
    this._writeStemFooter(ns);
  } // private void _writeStem(ns)

  // @since   1.1.0
  private void _writeStemChildren(Stem ns) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    Iterator  itS = ns.getChildStems().iterator();
    while (itS.hasNext()) {
      this._writeStem( (Stem) itS.next() );
    }
    Iterator  itG = ns.getChildGroups().iterator();
    while (itG.hasNext()) {
      this._writeGroup( (Group) itG.next() );
    }
  } // private void _writeStemChildren(ns)

  // @since   1.1.0
  private void _writeStemFooter(Stem ns) 
    throws  IOException
  {
    this.xml.puts("</stem>");
    this.xml.puts( this.xml.comment( U.q(ns.getName() ) ) );
    this.xml.undent(); // Undent the surplus indent from the header
    this.xml.puts();
  } // private void _writeStemFooter(stem)

  // @since   1.1.0
  private void _writeStemHeader(Stem stem) 
    throws  IOException
  {
    this.xml.puts();
    this.xml.indent();
    this.xml.puts( this.xml.comment( U.q( stem.getName() ) ) );
    this.xml.puts("<stem extension="  + U.q( this._fixXmlAttribute(stem.getExtension()) )         );
    this.xml.indent();
    this.xml.puts("displayExtension=" + U.q( this._fixXmlAttribute(stem.getDisplayExtension()) )  );
    this.xml.puts("name="             + U.q( this._fixXmlAttribute(stem.getName()) )              );
    this.xml.puts("displayName="      + U.q( this._fixXmlAttribute(stem.getDisplayName()) )       );
    this.xml.puts("id="               + U.q( this._fixXmlAttribute(stem.getUuid()) )              );
    this.xml.undent();
    this.xml.puts(">");
    this.xml.indent();
    this.xml.puts("<description>" + this._fixXmlAttribute(stem.getDescription()) + "</description>");
    this.xml.undent();
    // Don't fully undent
  } // private void _writeStemHeader(stem)

  // @since   1.1.0
  private void _writeStemPrivs(Stem ns) 
    throws  IOException,
            MemberNotFoundException
  {
    if (this._isNamingPrivExportEnabled()) {
      // TODO 20061003 this originally called `getStemmers()` for both.  i
      //      should verify this.
      this._writePrivileges("stem"  , ns.getStemmers(), ns);
      this._writePrivileges("create", ns.getCreators(), ns);
    } 
  } // private void _writeStemPrivs(ns)

  // @since   1.1.0
  private void _writeOwnerStack(Stack stack) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    Object obj = stack.pop();
    if (obj instanceof Group) {
      this._writeGroup( (Group) obj );
      return;
    }
    Stem ns = (Stem) obj;
    if (stack.isEmpty()) {
      if (this.includeParent) {
        // TODO 20061003 won't this cause the parent to be exported but no the target?
        this._writeStem(ns);
      } 
      else {
        this._writeStemChildren(ns);
      }
    } 
    else {
      this._writeStemHeader(ns);
      if (this._isParentPrivsExportEnabled()) {
      	this._writeStemPrivs(ns);
      }
      this._writeOwnerStack(stack);
      this._writeStemFooter(ns);
    }
  } // private void _writeOwnerStack(stack)

  // @since   1.1.0
  private void _writeSubject(Subject subj) 
    throws  IOException 
  {
    this._writeSubject(subj, GrouperConfig.EMPTY_STRING);
  } // private void _writeSubject(subj)

  // @since   1.1.0
  private void _writeSubject(Subject subj, String immediate) 
    throws  IOException 
  {
    // TODO 20061005 this. is. ugly.
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
    if (Validator.isNullOrBlank(exportAttrs)) {
      this.xml.puts("/>");
      return;
    }
    this.xml.puts(">");
    String    attr;
    Iterator  attrIt;
    String    attrValue;
    Set       values;
    while (exportAttrs.hasNext()) {
      attr = (String) exportAttrs.next();
      values = subj.getAttributeValues(attr);
      this.xml.puts("<subjectAttribute name='" + attr + "'>");
      attrIt = values.iterator();
      while (attrIt.hasNext()) {
        attrValue = (String) attrIt.next();
        this.xml.puts("<value>" + attrValue + "</value>");
      }
      this.xml.puts("</subjectAttribute>");
    }
    this.xml.puts("</subject>");

  } // private void _writeSubject(subj, immediate)

  // @since   1.1.0
  private void _writeSubjectSourceMetaData(Source sa) 
    throws  IOException
  {
    this.xml.indent();
    this.xml.puts("<source id=" + U.q( this._fixXmlAttribute(sa.getId()) )  );
    this.xml.indent();
    this.xml.puts("name="       + U.q( sa.getName() )                       );
    this.xml.puts("class="      + U.q( sa.getClass().getName() )            );
    this.xml.undent();
    this.xml.puts(">");
    Iterator it = sa.getSubjectTypes().iterator();
    while (it.hasNext()) {
      this._writeSubjectSourceTypesMetaData( (SubjectType) it.next() );
    }
    this.xml.puts("</source>");
    this.xml.undent();
  } // private void _writeSubjectSourceMetaData(sa)

  // @since   1.1.0
  private void _writeSubjectSourcesMetaData()
    throws  GrouperException,
            IOException 
  {
    this.xml.indent();
    this.xml.puts("<subjectSourceMetaData>");
    try {
      Iterator it = SourceManager.getInstance().getSources().iterator();
      while (it.hasNext()) {
        this._writeSubjectSourceMetaData( (Source) it.next() );
      }
    }
    catch (Exception e) {
      throw new GrouperException(e.getMessage(), e);
    }
    finally {
      this.xml.puts("</subjectSourceMetaData>");
      this.xml.undent();
    }
  } // private void _writeSubjectSourcesMetaData()

  // @since   1.1.0
  private void _writeSubjectSourceTypesMetaData(SubjectType st) 
    throws  IOException
  {
    this.xml.indent();
    this.xml.puts("<subjectType name=" + U.q( st.getName() ) + "/>");
    this.xml.undent();
  } // private void _writeSubjectSourceTypesMetaData(st)
  
} // public class XmlExporter

