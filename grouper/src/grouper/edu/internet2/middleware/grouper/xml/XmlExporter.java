/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.grouper.xml;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.internal.util.XML;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.grouper.xml.userAudit.XmlUserAuditExport;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SourceManager;

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
 * @version $Id: XmlExporter.java,v 1.17 2009-12-07 07:31:09 mchyzer Exp $
 * @since   1.0
 */
public class XmlExporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final String CF          = "export.properties"; 
  private static final Log    LOG         = GrouperUtil.getLog(XmlExporter.class);


  // PRIVATE INSTANCE VARIABLES //
  private GrouperSession  s;
  private String          fromStem      = null;
  // TODO 20070321 i need to figure out all the details of how "includeParent" and "isRelative" interact
  private boolean         includeParent = false;
  private boolean         isRelative    = false;
  private boolean         childrenOnly= false;
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
   * <td>export.uuids</td>
   * <td>true/false</td>
   * <td>true</td>
   * <td>If true group and stem uuids will be exported</td>
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
   * <td>export.data.fail-on-unresolvable-subject</td>
   * <td>true/false</td>
   * <td>false</td>
   * <td>If true and there is a problem resolving a subject attribute abort.</td>
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
    throws  GrouperException
  {
    try {
      this.options  = XmlUtils.internal_getSystemProperties(LOG, CF);
    }
    catch (IOException eIO) {
      throw new GrouperException(eIO.getMessage(), eIO);
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
    if (XmlArgs.internal_wantsHelp(args)) {
      System.out.println( _getUsage() );
      //System.exit(0);
      return;
    }
    Properties rc = new Properties();
    try {
      rc = XmlArgs.internal_getXmlExportArgs(args);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.err.println();
      System.err.println( _getUsage() );
      //System.exit(1);
      return;
    }
    XmlExporter exporter = null;
    try {
      exporter = new XmlExporter(
        GrouperSession.start(
          SubjectFinder.findByIdentifier( rc.getProperty(XmlArgs.RC_SUBJ), true ), false),
        XmlUtils.internal_getUserProperties(LOG, rc.getProperty(XmlArgs.RC_UPROPS)));
      _handleArgs(exporter, rc);
      LOG.debug("Finished export to [" + rc.getProperty(XmlArgs.RC_EFILE) + "]");
    }
    catch (Exception e) {
      LOG.fatal("unable to export to xml: " + e.getMessage());
      try {
      exporter.xml.internal_close();
      }catch(IOException ioe){}
      //System.exit(1);
      return;
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
    //System.exit(0);
    return;
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
    this.childrenOnly   = true;
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
    export(writer, ns,relative,false);
  } // public void export(writer, ns, relative)


  /**
   * Export a single stem.
   * <p/>
   * @param   writer        Write XML here.
   * @param   ns            Export this stem.
   * @param   relative      If true export in a format suitable for relocating within the Groups Registry.
   * @param   childrenOnly  If true omit the actual stem and only export child stems/groups
   * @throws  GrouperException
   * @since   1.4.0
   */
  public void export(Writer writer, Stem ns, boolean relative, boolean childrenOnly)
    throws  GrouperException 
  {
    this.xml            = new XmlWriter(writer);
    this.isRelative     = relative;
    this.childrenOnly     = childrenOnly;
    //if (ns.isRootStem()) {
    //includeParent should just refer to groups
      this.includeParent = false; // TODO 20070321 includeParent is still sort of magic to /me
    //}
    this._export(ns);
  } // public void export(writer, ns, relative, childrenOnly)

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
  public void export(Writer writer, final Collection items, final String msg) 
    throws  GrouperException 
  {
    this.xml = new XmlWriter(writer);
    try {
      GrouperSession.callbackGrouperSession(this.s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            XmlExporter.this._exportCollection(items, msg);
          } catch (GrouperException grouperException) {
            throw new GrouperSessionException(grouperException);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException grouperSessionException) {
      if (grouperSessionException.getCause() instanceof GrouperException) {
        throw (GrouperException)grouperSessionException.getCause();
      }
      throw grouperSessionException;
    }
  } // public void _export(items, msg)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  public Properties internal_getOptions() {
    return (Properties) this.options.clone();
  } // protected Properties internal_getOptions()

  /**
   * Return a <i>String</i> containing a <i>Group</i> as XML.
   * @since   1.2.0
   */
  public String internal_groupToXML(Group g, boolean writeAbsoluteName) {
    StringBuffer sb = new StringBuffer();
    sb.append( "<groupRef ");
    if(_isUuidAttrsExportEnabled()){
    	sb.append( "id=" + Quote.single( XML.escape( g.getUuid() ) ) );
    	sb.append( GrouperConfig.NL );
    }
    
    sb.append( "        name=" );
    if (!writeAbsoluteName) {
      sb.append( Quote.single( XML.escape( this._fixGroupName( g.getName() ) ) ) );
    }
    else {
      sb.append( Quote.single( XML.escape( g.getName() ) ) );
    }
    sb.append( GrouperConfig.NL );
    sb.append( " displayName=" + Quote.single( XML.escape( g.getDisplayName() ) ) );
    sb.append( "/>" );
    sb.append( GrouperConfig.NL );
    return sb.toString();
  }

  /**
   * Return a <i>String</i> containing a <i>Membership</i> as XML.
   * @since   1.2.0
   */
  public String internal_membershipToXML(Membership ms) 
    throws  GroupNotFoundException,
            MemberNotFoundException,
            SubjectNotFoundException
  {
    // TODO 20070521 should these exceptions be thrown?  or replaced by something else?
    StringBuffer sb = new StringBuffer();
    sb.append( "<membership>" );
    sb.append( GrouperConfig.NL );
    sb.append( "  <depth>" + ms.getDepth() + "</depth>" );
    sb.append( GrouperConfig.NL );
    sb.append( "  <listName>" + XML.escape( ms.getList().getName() ) + "</listName>" );
    sb.append( GrouperConfig.NL );
    sb.append( "  <immediate>" );
    // TODO 20070521 how do composites fit in here?
    if ( MembershipType.EFFECTIVE.getTypeString().equals( ms.getType() ) ) {
      sb.append( false );
    }
    else {
      sb.append( true );
    }
    sb.append( "</immediate>" );
    sb.append( GrouperConfig.NL );
    sb.append( this.internal_groupToXML( ms.getOwnerGroup(), true ) );
    sb.append( this.internal_subjectToXML( ms.getMember().getSubject(), GrouperConfig.EMPTY_STRING ) );
    sb.append( "</membership>" );
    sb.append( GrouperConfig.NL );
    return sb.toString();
  }

  /**
   * Return a <i>String</i> containing a <i>Subject</i> as XML.
   * @since   1.2.0
   */
  public String internal_subjectToXML(Subject subj, String immediate) { 
    // TODO 20070521 rename/refactor "immediate"
    // TODO 20070521 this method is far too large
    StringBuffer sb = new StringBuffer();
    sb.append("<subject ");
    // TODO 20070521 i don't like how we treat groups different here
    if ( "group".equals( subj.getType().getName() ) ) {
      sb.append( "identifier="  + Quote.single( XML.escape( this._fixGroupName( subj.getName() ) ) ) );
      sb.append( " type="       + Quote.single( XML.escape( subj.getType().getName() ) ) );
      sb.append( " source="     + Quote.single( XML.escape( subj.getSource().getId() ) ) );
      sb.append( immediate );
      sb.append( " id="          + Quote.single( XML.escape( subj.getId() ) ) );
    }
    else {
      sb.append( "id="      + Quote.single( XML.escape( subj.getId() ) ) );
      sb.append( " type="   + Quote.single( XML.escape( subj.getType().getName() ) ) );
      sb.append( " source=" + Quote.single( XML.escape( subj.getSource().getId() ) ) );
      sb.append( immediate );
    } 
    sb.append( GrouperConfig.NL );
   
    // TODO 20070521 move to own method? 
    Iterator it = this._getExportAttributes(subj);
    if (it == null) {
      sb.append( "/>" );
    }
    else {
      sb.append( ">" );
      sb.append( GrouperConfig.NL );

      String    attr;
      Iterator  itAttr;
      Set       values;
      while (it.hasNext()) {
        attr    = (String) it.next();
        values  = subj.getAttributeValues(attr);
        sb.append( "<subjectAttribute name=" + Quote.single( XML.escape(attr) ) + ">" );
        sb.append( GrouperConfig.NL );
        itAttr = values.iterator();
        while (itAttr.hasNext()) {
          sb.append( "<value>" + XML.escape( (String) itAttr.next() ) + "</value>" );
        }
        sb.append( "</subjectAttribute>" );
        sb.append( GrouperConfig.NL );
      }

      sb.append( "</subject>" );
    }

    sb.append( GrouperConfig.NL );
    return sb.toString();
  }


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
            + "args: subjectIdentifier "             + GrouperConfig.NL
            + "      [-userAuditFilename <fileName>] [-userAuditOnly]"              + GrouperConfig.NL 
            + "      [(-id <id>] | [-name <name>)] [-relative]"                     + GrouperConfig.NL
            + "      [-includeParent] [-childrenOnly] fileName [properties]"        + GrouperConfig.NL
            + "e.g.  gsh -xmlexport -userAuditFilename f:/temp/prodAudit.xml GrouperSystem f:/temp/prod.xml"                 + GrouperConfig.NL
            +                                                                         GrouperConfig.NL
            + "  subjectIdentifier, Identifies a Subject 'who' will create a"       + GrouperConfig.NL
            + "                     GrouperSession"                                 + GrouperConfig.NL
            + "  -userAuditFilename,The file name where user audits should go"      + GrouperConfig.NL
            + "  -id,               The UUID of a Group or Stem to export"          + GrouperConfig.NL
            + "  -name,             The name of a Group or Stem to export"          + GrouperConfig.NL
            + "  -relative,         If id or name specified do not export parent"   + GrouperConfig.NL
            + "                     Stems"                                          + GrouperConfig.NL
            + "  -includeParent,    If id or name identifies a Group and -relative "+ GrouperConfig.NL
            + "                     is selected, export the Group and its "         + GrouperConfig.NL
            + "                     parent Stem"                                    + GrouperConfig.NL
            + "  -childrenOnly,     If id or name identifies a Stem and -relative " + GrouperConfig.NL
            + "                     is selected, export child Stems and Groups,   " + GrouperConfig.NL
            + "                     but not the stem itself                       " + GrouperConfig.NL
            + "  filename,          The file where exported data will be written."  + GrouperConfig.NL
            + "                     Will overwrite existing files"                  + GrouperConfig.NL
            + "  properties,        The name of an optional Java properties file. " + GrouperConfig.NL
            + "                     Values specified in this properties file will " + GrouperConfig.NL
            + "                     override the default export behavior."          + GrouperConfig.NL
            ;
  } // private static String _getUsage()

  // @since   1.1.0
  private static void _handleArgs(XmlExporter exporter, Properties rc) 
    throws  Exception {
    
    String userAuditFilename = rc.getProperty("userAuditFilename");
    if (!StringUtils.isBlank(userAuditFilename)) {
      
      XmlUserAuditExport.writeUserAudits(new File(userAuditFilename));
      
    }
    
    String userAuditOnly = rc.getProperty("userAuditOnly");
    if (StringUtils.equalsIgnoreCase("true", userAuditOnly)) {
      //we are done
      return;
    }
    
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
          group = GroupFinder.findByUuid(exporter.s, uuid, true);
          LOG.debug("Found group with uuid [" + uuid + "]");
        } 
        catch (GroupNotFoundException eGNF) {
          // Look for stem instead
          try {
            stem = StemFinder.findByUuid(exporter.s, uuid, true);
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
          group = GroupFinder.findByName(exporter.s, name, true);
          LOG.debug("Found group with name [" + name + "]");
        } 
        catch (GroupNotFoundException eGNF) {
          // Look for stem instead
          try {
            stem = StemFinder.findByName(exporter.s, name, true);
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
          Boolean.valueOf( rc.getProperty(XmlArgs.RC_RELATIVE) ).booleanValue(),
          Boolean.valueOf( rc.getProperty(XmlArgs.RC_PARENT) ).booleanValue()
        );
      } 
      else {
        exporter.export(writer, stem, Boolean.valueOf( rc.getProperty(XmlArgs.RC_RELATIVE) ).booleanValue(),Boolean.valueOf( rc.getProperty(XmlArgs.RC_CHILDREN) ).booleanValue() );
      }
    }
  } // private static void _handleArgs(exporter, rc)


  // PRIVATE INSTANCE METHODS //

  /**
   * @since   1.1.0
   * @param o
   * @throws GrouperException
   */
  private void _export(final Owner o)
    throws  GrouperException
  {
    try {
      GrouperSession.callbackGrouperSession(this.s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            XmlExporter.this._setFromStem(o);
            XmlExporter.this._writeHeader();
            XmlExporter.this._writeMetaData();
            XmlExporter.this._writeData(o);
            XmlExporter.this._writeExportParams(o);
            XmlExporter.this._writeFooter();
          }
          catch (GrouperException ge) {
            throw new GrouperSessionException(ge);
          }
          catch (CompositeNotFoundException eCNF) {
            throw new GrouperSessionException(new GrouperException(eCNF.getMessage(), eCNF));
          }
          catch (GroupNotFoundException eGNF)     {
            throw new GrouperSessionException(new GrouperException(eGNF.getMessage(), eGNF));
          }
          catch (IOException eIO) {
            throw new GrouperSessionException(new GrouperException(eIO.getMessage(), eIO));
          }
          catch (MemberNotFoundException eMNF)    {
            throw new GrouperSessionException(new GrouperException(eMNF.getMessage(), eMNF));
          }
          catch (SchemaException eS)              {
            throw new GrouperSessionException(new GrouperException(eS.getMessage(), eS));
          }
          catch (StemNotFoundException eNSNF)     {
            throw new GrouperSessionException(new GrouperException(eNSNF.getMessage(), eNSNF));
          }
          catch (SubjectNotFoundException eSNF)   {
            throw new GrouperSessionException(new GrouperException(eSNF.getMessage(), eSNF));
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException grouperSessionException) {
      if (grouperSessionException.getCause() instanceof GrouperException) {
        throw (GrouperException)grouperSessionException.getCause();
      }
      throw grouperSessionException;
    }
  } // private void _export(o)

  /**
   * @since   1.1.0
   * @param c
   * @param msg
   * @throws GrouperException
   */
  private void _exportCollection(Collection c, String msg) 
    throws  GrouperException {
    try {
      // TODO 20070531 this is atrocious
      this.fromStem         = "_Z";
      this._writeHeader();
      int     counter       = 0;
      if (this._isDataExportEnabled()) {
        this.xml.internal_indent();
        this.xml.internal_puts("<dataList>");
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
              this.xml.internal_puts("<exportOnly/>");
            }
            this.xml.internal_puts( this.internal_subjectToXML( (Subject) obj, GrouperConfig.EMPTY_STRING ) );
          } 
          else if (obj instanceof Member)     {
            if (counter == 1) {
              this.xml.internal_puts("<exportOnly/>");
            }
            this.xml.internal_puts( this.internal_subjectToXML( ( (Member) obj).getSubject(), GrouperConfig.EMPTY_STRING ) );
          } 
          else if (obj instanceof Membership) {
            if (counter == 1) {
              this.xml.internal_puts("<exportOnly/>");
            }
            this.xml.internal_puts( this.internal_membershipToXML( (Membership) obj ) );
          } 
          else {
            LOG.error("Don't know about exporting " + obj);
          }
          this.xml.internal_puts();
        }
        this.xml.internal_puts("</dataList>");
        this.xml.internal_undent();
        this.xml.internal_puts();
      }
      this.xml.internal_indent();
      this.xml.internal_puts("<exportComments><![CDATA[");
      this.xml.internal_indent();
      this.xml.internal_puts(msg);
      this.xml.internal_undent();
      this.xml.internal_puts("]]></exportComments>");
      this.xml.internal_undent();
      this.xml.internal_puts();
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
  } 

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
    // TODO 20070328 i'm not keen on these repetitive "if"s but i'm afraid to touch this code until
    //               i have added more black box testing
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(value);
    if (v.isInvalid()) {
      key   = "export.subject-attributes.source." + source;
      value = options.getProperty(key);
    }
    v = NotNullOrEmptyValidator.validate(value);
    if (v.isInvalid()) {
      key   = "export.subject-attributes.type." + type;
      value = options.getProperty(key);
    }
    v = NotNullOrEmptyValidator.validate(value);
    if (v.isInvalid()) {
      return null;
    }
    if (XmlUtils.SPECIAL_STAR.equals(value)) {
      return GrouperUtil.nonNull(subj.getAttributes()).keySet().iterator();
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
      if(o instanceof Stem && ((Stem)o).isRootStem()) {
    	//Root stem doesn't have parent stems - perhaps method should return empty Stack? 
    	  stems = new Stack();
    	  stems.add(o);
      }else{
    	  stems = this._getParentStems(o);
      }
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
    return XmlUtils.internal_getBooleanOption(this.options, "export.privs.access");
  } // private boolean _isAccessPrivExportEnabled()
  
  // @since   1.1.0
  private boolean _isDataExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.data");
  } // private boolean _isDataExportEnabled()

//@since   1.4.0
  private boolean _isUuidAttrsExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.uuids");
  } // private boolean _isUuidAttrsExportEnabled()
  
  // @since   1.1.0
  private boolean _isGroupInternalAttrsExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.group.internal-attributes");
  } // private boolean _isGroupInternalAttrsExportEnabled()

  // @since   1.1.0
  private boolean _isGroupListImmediateOnlyExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.group.lists.immediate-only");
  } // private boolean _isGroupListImmediateOnlyExportEnabled()

  // @since   1.1.0
  private boolean _isGroupMemberImmediateOnlyExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.group.members.immediate-only");
  } // private boolean _isGroupMemberImmediateOnlyExportEnabled()

  // @since   1.1.0
  private boolean _isMetadataExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.metadata");
  } // private boolean _isMetadataExportEnabled()

  // @since   1.1.0
  private boolean _isNamingPrivExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.privs.naming");
  } // private boolean _isNamingPrivExportEnabled()

  // @since   1.1.0
  private boolean _isParentPrivsExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.privs.for-parents");
  } // private boolean _isParentPrivsExportEnabled()

  // @since   1.1.0
  private boolean _isStemInternalAttrsExportEnabled() {
    return XmlUtils.internal_getBooleanOption(this.options, "export.stem.internal-attributes");
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
        parent                    = parent.getParentStem();
        NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate( parent.getExtension() );
        if (v.isInvalid()) {
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
    this.xml.internal_puts("<composite>");
    this.xml.internal_puts( this.internal_groupToXML( comp.getLeftGroup(), false ) );
    this.xml.internal_puts();
    this.xml.internal_puts( "<compositeType>" + comp.getType().toString() + "</compositeType>" );
    this.xml.internal_puts();
    this.xml.internal_puts( this.internal_groupToXML( comp.getRightGroup(), false ) );
    this.xml.internal_puts("</composite>");
  } 

  // @since   1.1.0
  private void _writeExportParams(Owner o)
    throws  IOException
  {
    this.xml.internal_indent();
    this.xml.internal_puts("<exportParams>");
    this.xml.internal_indent();
    String s = "<node type='";
    if (o instanceof Group) {
      s += "group'>";
    }
    else {
      s += "stem'>";
    }
    this.xml.internal_puts( s + o.getName() + "</node>" );
    this.xml.internal_puts("<relative>" + isRelative + "</relative>");
    if (o instanceof Group) {
      this.xml.internal_puts("<includeParent>" + includeParent + "</includeParent>");
    }
    if (o instanceof Stem) {
        this.xml.internal_puts("<childrenOnly>" + childrenOnly + "</childrenOnly>");
      }
    this.xml.internal_undent();
    this.xml.internal_puts("</exportParams>");
    this.xml.internal_undent();
    this.xml.internal_puts();
  } // private void _writeExportParams(o)

  // @since   1.1.0
  private void _writeFieldMetaData(Field f) 
    throws  IOException
  {
    this.xml.internal_indent();
    this.xml.internal_puts("<field name="  + Quote.single( XML.escape( f.getName() ) ) );
    this.xml.internal_indent();
    this.xml.internal_puts("type="         + Quote.single( f.getType().toString() )       );
    this.xml.internal_puts("readPriv="     + Quote.single( f.getReadPriv().toString() )   );
    this.xml.internal_puts("writePriv="    + Quote.single( f.getWritePriv().toString() )  );
    this.xml.internal_puts(contextIdAttribute(f.getContextId(), false)  );
    this.xml.internal_undent();
    this.xml.internal_puts("/>");
    this.xml.internal_undent();
  } // private void _writeFieldMetaData(f)

  // @since   1.1.0
  private void _writeFooter()
    throws  IOException
  {
    Date    now       = new Date();
    long    duration  = (now.getTime() - this.startTime.getTime()) / 1000;
    this.xml.internal_indent();
    this.xml.internal_puts("<exportInfo>");
    this.xml.internal_indent();
    this.xml.internal_puts("<start>" + startTime + "</start>");
    this.xml.internal_puts("<end>" + now + "</end>");
    this.xml.internal_puts("<duration>" + duration + "</duration>");
    this._writeOptions();
    this.xml.internal_undent();
    this.xml.internal_puts("</exportInfo>");
    this.xml.internal_undent();
    this.xml.internal_puts("</registry>");
    this.xml.internal_puts();
    this.xml.internal_close();
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
  private void _writeGroupFooter(Group g) 
    throws  IOException
  {
    this.xml.internal_puts("</group>");
    this.xml.internal_puts( this.xml.internal_comment( Quote.single(g.getName() ) ) );
    this.xml.internal_undent(); // Undent the surplus indent from the header
    this.xml.internal_puts();
  } // private void _writeGroupFooter(g)

  // @since   1.1.0
  private void _writeGroupHeader(Group g)  
    throws  IOException
  {
    this.xml.internal_puts();
    this.xml.internal_indent();
    this.xml.internal_puts( this.xml.internal_comment( Quote.single( g.getName() ) ) );
    this.xml.internal_puts("<group extension=" + Quote.single( XML.escape( g.getExtension() ) )            );
    this.xml.internal_indent();
    this.xml.internal_puts("displayExtension=" + Quote.single( XML.escape( g.getDisplayExtension() ) )     );
    this.xml.internal_puts("name="             + Quote.single( XML.escape( g.getName() ) )                 );
    this.xml.internal_puts("displayName="      + Quote.single( XML.escape( g.getDisplayName() ) )          );
    if(_isUuidAttrsExportEnabled()){
    	this.xml.internal_puts("id="               + Quote.single( XML.escape( g.getUuid() ) )                 );
    }
    this.xml.internal_puts(contextIdAttribute(g.getContextId(), false));
    this.xml.internal_undent();
    this.xml.internal_puts(">");
    this.xml.internal_indent();
    this.xml.internal_puts("<description>" + XML.escape( g.getDescription() ) + "</description>"  );
    this.xml.internal_undent();
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
  private void _writeGroupType(Group g, GroupType gt)
    throws  IOException,
            SchemaException
  {
    this.xml.internal_indent();
    this.xml.internal_puts("<groupType name=" + Quote.single( XML.escape( gt.getName() ) ) + ">");
    Iterator  it  = gt.getFields().iterator();
    while (it.hasNext()) {
      this._writeGroupTypeField(g, (Field) it.next());
    }
    this.xml.internal_puts("</groupType>");
    this.xml.internal_undent();
    this.xml.internal_puts();
  } // private void _writeGroupType(group, groupType)

  // @since   1.1.0
  private void _writeGroupTypeField(Group g, Field f) 
    throws  IOException,
            SchemaException
  {
    if ( !f.getType().equals(FieldType.LIST) && g.canReadField(f) ) {
      try {
        String            val = XML.escape( g.getAttributeValue( f.getName(),false, true ) );
        NotNullValidator  v   = NotNullValidator.validate(val);
        if (
          v.isValid() && ":description:extension:displayExtension:".indexOf(":" + f.getName() + ":") == -1
        ) 
        {
          this.xml.internal_indent();
          this.xml.internal_puts(
            "<attribute name='"
            + XML.escape( f.getName() ) + "'>" + val
            + "</attribute>"
          );
          this.xml.internal_undent();
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
    if ( U.getBooleanProperty( this.options, "export.group.custom-attributes" ) ) {
      Set types = g.getRemovableTypes(); 
      if (!types.isEmpty()) {
        this.xml.internal_indent();
        this.xml.internal_puts("<groupTypes>");
        Iterator it = types.iterator();
        while (it.hasNext()) {
          this._writeGroupType(g, (GroupType) it.next());
        }
        this.xml.internal_puts("</groupTypes>");
        this.xml.internal_undent();
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
      this.xml.internal_indent();
      this.xml.internal_puts("<data>");
      this._writeOwnerStack( this._getStackToWrite(o) );
      this.xml.internal_puts("</data>");
      this.xml.internal_undent();
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
    if ( U.getBooleanProperty(this.options, "export.group.members") ) {
      lists.add("members");
    }
    if ( U.getBooleanProperty(this.options, "export.group.lists") ) {
      lists.addAll( _getListFieldsForGroup(g) );
    }
    Iterator it = lists.iterator();
    while (it.hasNext()) {
      this._writeList(g, FieldFinder.find( (String) it.next(), true ) );
    }
  } // private void _writeLists(g)

  /**
   * give an attribute for context id (with space before) if the context id is not blank 
   * @param contextId
   * @param includeOpeningSpace true to include an opening space
   * @return the string or empty string
   */
  private static String contextIdAttribute(String contextId, boolean includeOpeningSpace) {
    if (StringUtils.isBlank(contextId)) {
      return "";
    }
    return (includeOpeningSpace ? " " : "") + "contextId=" + Quote.single(XML.escape(contextId));
  }
  
  // @since   1.1.0
  private void _writeGroupType(GroupType gt) 
    throws  IOException
  {
    this.xml.internal_indent();
    
    this.xml.internal_puts("<groupTypeDef name=" + Quote.single( XML.escape( gt.getName() ) )
    		+ contextIdAttribute(gt.getContextId(), true) + ">");
    Iterator it = gt.getFields().iterator();
    while (it.hasNext()) {
      this._writeFieldMetaData( (Field) it.next() );
    }
    this.xml.internal_puts("</groupTypeDef>");
    this.xml.internal_undent();
  } // private void _writeGroupTypesMetaData(gt)

  // @since   1.1.0
  private void _writeGroupTypesMetaData()
    throws  IOException 
  {
    Set types = GroupTypeFinder.findAll();
    if (types.isEmpty()) {
      return;
    }
    this.xml.internal_indent();
    this.xml.internal_puts("<groupTypesMetaData>");
    Iterator  it  = types.iterator();
    while (it.hasNext()) {
      this._writeGroupType( (GroupType) it.next() );
    }
    this.xml.internal_puts("</groupTypesMetaData>");
    this.xml.internal_undent();
  } // private void _writeGroupTypesMetaData()

  // @since   1.1.0
  private void _writeHeader()
    throws  GrouperException,
            IOException 
  {
    this.xml.internal_puts("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    this.xml.internal_puts("<registry>");
  } // private void _writeHeader()

  // @since   1.1.0
  private void _writeInternalAttribute(String attr, long l)
    throws  IOException
  {
    Date d = new Date(l);
    this.xml.internal_indent();
    this.xml.internal_puts(
        "<internalAttribute name=" + Quote.single(attr) + ">" 
      + Long.toString( d.getTime() ) 
      + "</internalAttribute> " 
      + this.xml.internal_comment( d.toString() )
    ); 
    this.xml.internal_undent();
  } // private void _writeInternalAttribute(attr, d)

  // @since   1.1.0
  private void _writeInternalAttribute(String attr, String val)
    throws  IOException
  {
    NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(val);
    if (v.isInvalid()) {
      // Since I'm now using the internal methods to access the attr values I
      // need to be more careful about NULLs
      val = GrouperConfig.EMPTY_STRING;
    }
    this.xml.internal_indent();
    this.xml.internal_puts(
        "<internalAttribute name=" + Quote.single(attr) + ">" 
      + XML.escape(val) + "</internalAttribute>"
    ); 
    this.xml.internal_undent();
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
        "<subject " + idAttr + "=" + Quote.single( XML.escape(id)           )
      + " type="    + Quote.single( XML.escape( subj.getType().getName() )  )
      + " source="  + Quote.single( XML.escape( subj.getSource().getId() )  );
    if (idAttr.equals("identifier")) {
      txt += " id=" + Quote.single( subj.getId() );
    }
    txt += "/>";
    this.xml.internal_indent();
    this.xml.internal_puts("<internalAttribute name=" + Quote.single(attr) + ">");
    this.xml.internal_indent();
    this.xml.internal_puts(txt);
    this.xml.internal_undent();
    this.xml.internal_puts("</internalAttribute>");
    this.xml.internal_undent();
  } // private void _writeInternalAttribute(attr, subj, comment)

  // @since   1.2.0
  private void _writeInternalAttributes(Group g) 
    throws  IOException,
            MemberNotFoundException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if ( this._isGroupInternalAttrsExportEnabled() ) {
      this.xml.internal_indent();
      this.xml.internal_puts("<internalAttributes>");
      this._writeInternalAttribute( "parentStem",     this._getParentStemName(g)   );
      this._writeInternalAttribute( "createSubject",  MemberFinder.findByUuid( this.s, g.getCreatorUuid(), true ) );
      this._writeInternalAttribute( "createTime",     g.getCreateTimeLong() );
      if(g.getModifierUuid()!=null) {
    	  this._writeInternalAttribute( "modifySubject",  MemberFinder.findByUuid( this.s, g.getModifierUuid(), true ) );
    	  this._writeInternalAttribute( "modifyTime",     g.getModifyTimeLong() );
      }
      this.xml.internal_puts("</internalAttributes>");
      this.xml.internal_undent();
      this.xml.internal_puts();
    }
  } // private void _writeInternalAttributes(g)
  
  // @since   1.2.0
  private void _writeInternalAttributes(Stem ns) 
    throws  IOException,
            MemberNotFoundException,
            StemNotFoundException,
            SubjectNotFoundException
  {
    if ( this._isStemInternalAttrsExportEnabled() ) {
      this.xml.internal_indent();
      this.xml.internal_puts("<internalAttributes>");
      if(!ns.isRootStem()) {
    	  this._writeInternalAttribute( "parentStem",     this._getParentStemName(ns)   );
      }
      this._writeInternalAttribute( "createSubject",  MemberFinder.findByUuid( this.s, ns.getCreatorUuid(), true ) );
      this._writeInternalAttribute( "createTime",     ns.getCreateTimeLong() );
      if(ns.getModifierUuid()!=null) {
    	  this._writeInternalAttribute( "modifySubject",  MemberFinder.findByUuid( this.s, ns.getModifierUuid(), true ) );
    	  this._writeInternalAttribute( "modifyTime",     ns.getModifyTimeLong() );
      }
      this.xml.internal_puts("</internalAttributes>");
      this.xml.internal_undent();
      this.xml.internal_puts();
    }
  } // private void _writeInternalAttributes(ns)
  
  
  // @since   1.1.0
  private void _writeList(Group g, Field f) 
    throws  CompositeNotFoundException,
            GroupNotFoundException,
            IOException,
            MemberNotFoundException,
            SchemaException,
            SubjectNotFoundException
  {
    // TODO 20070321 refactor: this is ugly
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
//      if (
//        ( f.getName().equals("members")   && !this._isGroupMemberImmediateOnlyExportEnabled() )
//        || 
//        ( !f.getName().equals("members")  && !this._isGroupListImmediateOnlyExportEnabled() )
//      )
//      {
        members.addAll( g.getEffectiveMemberships(f) );
        if (f.getName().equals("members") && g.hasComposite()) {
          members.addAll( g.getCompositeMemberships() );
        }
//      }
      if (members.isEmpty() && !isComposite) {
        return;
      }
      this.xml.internal_indent();
      this.xml.internal_puts(
          "<list field="  + Quote.single( XML.escape( f.getName() )                 )
        + ">"
      );
      if (isComposite) {
        this._writeComposite( CompositeFinder.findAsOwner(g, true) );
      }
      this._writeMembers(members, g);
      this.xml.internal_puts(
        "</list> " + this.xml.internal_comment( Quote.single( XML.escape( f.getName() ) ) ) 
      );
      this.xml.internal_undent();
      this.xml.internal_puts();
    }
  } // private void _writeList(g, f)

  /**
   * @since  1.2.1
   */
  private void _writeMembers(Collection members, Group group)
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
      this.xml.internal_puts( this.internal_subjectToXML( subj, " immediate=" + Quote.single(isImmediate) + " " ) );
    }
  } 

  // @since   1.1.0
  private void _writeMetaData()
    throws  GrouperException,
            IOException 
  {
    if (this._isMetadataExportEnabled()) {
      this.xml.internal_indent();
      this.xml.internal_puts("<metadata>");
      this._writeGroupTypesMetaData();
      this.xml.internal_puts();
      this._writeSubjectSourcesMetaData();
      this.xml.internal_puts("</metadata>");
      this.xml.internal_undent();
    }
  } // private void _writeMetaData()

  // @since   1.1.0
  private void _writeOptions() 
    throws  IOException
  {
    this.xml.internal_puts("<options>");
    this.xml.internal_indent();
    String    key;
    List      opts  = new ArrayList( options.keySet() );
    Collections.sort(opts);
    Iterator  it    = opts.iterator();
    while (it.hasNext()) {
      key = (String) it.next();
      this.xml.internal_puts(
        "<option key=" + Quote.single(key) + ">" + options.getProperty(key) + "</option>"
      );
    }
    this.xml.internal_undent();
    this.xml.internal_puts("</options>");
  } // private void _writeOptions()

  // @since   1.1.0
  private void _writePrivileges(String privilege, Set subjects, Owner o)
    throws  IOException,
            MemberNotFoundException
  {
    // TODO 20070321 why the removal?
    if (subjects.size() == 1) {
      subjects.remove( SubjectFinder.findRootSubject() );
    }
    this.xml.internal_puts();
    this.xml.internal_puts("<privileges type='" + privilege + "'>");
    Subject   subject;
    boolean   isImmediate = false;
    Iterator  it          = subjects.iterator();
    while (it.hasNext()) {
      subject     = (Subject) it.next();
      isImmediate = XmlUtils.internal_hasImmediatePrivilege(subject, o, privilege);
      if (
        (!subject.getId().equals("GrouperSystem"))
        && 
        (isImmediate || !U.getBooleanProperty(this.options, "export.privs.immediate-only") )
      )
      {
        this.xml.internal_puts( this.internal_subjectToXML( subject, " immediate= " + Quote.single(isImmediate) + " " ) );
      }
    }
    this.xml.internal_puts("</privileges> " + this.xml.internal_comment(privilege));
  } 

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
    this.xml.internal_puts("</stem>");
    this.xml.internal_puts( this.xml.internal_comment( Quote.single(ns.getName() ) ) );
    this.xml.internal_undent(); // Undent the surplus indent from the header
    this.xml.internal_puts();
  } // private void _writeStemFooter(stem)

  // @since   1.1.0
  private void _writeStemHeader(Stem ns) 
    throws  IOException
  {
    this.xml.internal_puts();
    this.xml.internal_indent();
    this.xml.internal_puts( this.xml.internal_comment( Quote.single( ns.getName() ) ) );
    this.xml.internal_puts("<stem extension="  + Quote.single( XML.escape( ns.getExtension() ) )           );
    this.xml.internal_indent();
    this.xml.internal_puts("displayExtension=" + Quote.single( XML.escape( ns.getDisplayExtension() ) )    );
    this.xml.internal_puts("name="             + Quote.single( XML.escape( ns.getName()) ));
    this.xml.internal_puts(contextIdAttribute(ns.getContextId(), false));
    this.xml.internal_puts("displayName="      + Quote.single( XML.escape( ns.getDisplayName() ) )         );
    if(_isUuidAttrsExportEnabled()){
    	this.xml.internal_puts("id="           + Quote.single( XML.escape( ns.getUuid() ) )                );
    }
    this.xml.internal_undent();
    this.xml.internal_puts(">");
    this.xml.internal_indent();
    this.xml.internal_puts("<description>" + XML.escape( ns.getDescription() ) + "</description>" );
    this.xml.internal_undent();
    // Don't fully undent
  } // private void _writeStemHeader(ns)

  // @since   1.1.0
  private void _writeStemPrivs(Stem ns) 
    throws  IOException,
            MemberNotFoundException
  {
    if (this._isNamingPrivExportEnabled()) {
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
      if (!this.childrenOnly) {
        
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
  private void _writeSubjectSourceMetaData(Source sa) 
    throws  IOException
  {
    this.xml.internal_indent();
    this.xml.internal_puts("<source id=" + Quote.single( XML.escape( sa.getId() ) )  );
    this.xml.internal_indent();
    this.xml.internal_puts("name="       + Quote.single( XML.escape(sa.getName()) )                 );
    this.xml.internal_puts("class="      + Quote.single( sa.getClass().getName() )      );
    this.xml.internal_undent();
    this.xml.internal_puts(">");
    Iterator it = sa.getSubjectTypes().iterator();
    while (it.hasNext()) {
      this._writeSubjectSourceTypesMetaData( (SubjectType) it.next() );
    }
    this.xml.internal_puts("</source>");
    this.xml.internal_undent();
  } // private void _writeSubjectSourceMetaData(sa)

  // @since   1.1.0
  private void _writeSubjectSourcesMetaData()
    throws  GrouperException,
            IOException 
  {
    this.xml.internal_indent();
    this.xml.internal_puts("<subjectSourceMetaData>");
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
      this.xml.internal_puts("</subjectSourceMetaData>");
      this.xml.internal_undent();
    }
  } // private void _writeSubjectSourcesMetaData()

  // @since   1.1.0
  private void _writeSubjectSourceTypesMetaData(SubjectType st) 
    throws  IOException
  {
    this.xml.internal_indent();
    this.xml.internal_puts("<subjectType name=" + Quote.single( st.getName() ) + "/>");
    this.xml.internal_undent();
  } // private void _writeSubjectSourceTypesMetaData(st)
  
} // public class XmlExporter

