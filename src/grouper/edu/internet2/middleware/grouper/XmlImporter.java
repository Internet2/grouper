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
import  java.net.*;
import  java.util.*;
import  javax.xml.parsers.*;
import  org.apache.commons.logging.*;
import  org.w3c.dom.*;

/**
 * Utility class which reads an XML file representing all or part of a Grouper
 * repository, and updates/creates the equivalent stems/groups in the current
 * Grouper repository. XmlImporter can be used to load data exported by
 * XmlExporter.
 * <p/>
 * @author  Gary Brown.
 * @version $Id: XmlImporter.java,v 1.3 2006-06-30 20:34:48 blair Exp $
 * @since   1.0
 */
public class XmlImporter {

  // PRIVATE CLASS CONSTANTS //  
  private static final Log    log     = LogFactory.getLog(XmlImporter.class);
  private static final String NS_ROOT = "Grouper.NS_ROOT";
  private final static String sep     = ":"; // TODO Expose elsewhere


  // PRIVATE INSTANCE VARIABLES //
  private List            accessPrivLists = new ArrayList();
  private List            accessPrivs     = new ArrayList();
  private Map             importedGroups;
  private Stem            importTo;
  private String          importToName;
  private List            membershipLists = new ArrayList();
  private List            memberships     = new ArrayList();
  private List            namingPrivLists = new ArrayList();
  private List            namingPrivs     = new ArrayList();
  private Properties      options         = new Properties();
  private Properties      safeOptions     = new Properties();
  private GrouperSession  s;

 
  // CONSTRUCTORS //
 
  /**
   * The import process is configured through the following properties: <table
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
   * <td><font face="Arial, Helvetica,
   * sans-serif">import.metadata.group-types </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether group
   * type meta data is used to create group types in the current Grouper
   * repository </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">import.metadata.group-type-attributes </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether fields
   * associated with group type meta data are created </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">import.data.apply-new-group-types </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">If a group defined in XML
   * already exists, this property determines whether additional group types
   * defined in the XML are applied to the existing group </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica,
   * sans-serif">import.data.update-attributes </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">true/false </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines if group
   * attributes in the XML overwrite those in the repository, where a group
   * already exists </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">import.data.lists </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">ignore/replace/add </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether
   * membership and custom membership lists are ignored, used to replace
   * existing lists, or used to supplement existing lists </font></td>
   * </tr>
   * <tr>
   * <td><font face="Arial, Helvetica, sans-serif">import.data.privileges
   * </font></td>
   * <td><font face="Arial, Helvetica, sans-serif">ignore/replace/add </font>
   * </td>
   * <td><font face="Arial, Helvetica, sans-serif">Determines whether lists
   * of privilegees are ignored, used to replace existing lists, or used to
   * supplement existing lists </font></td>
   * </tr>
   * </table> It is possible to specify import options in the XML file in
   * addition to or instead of those passed as a Properties object. Where keys
   * exist in the Propeties object and the XML, the value is taken from the
   * Properties object
   * @param   options
   * @throws  Exception
   * @since   1.0
   */
  public XmlImporter(Properties options) 
    throws  Exception 
  {
    this.options      = options;
    this.safeOptions  = (Properties) options.clone();
  } // public XmlImporter(options)

  /**
   * No argument constructor - so options must be included as part of the XML -
   * using importOptions and option tags, or the setOptions method called
   * <p/>
   * @throws  Exception
   * @since   1.0
   */
  public XmlImporter() throws Exception {
  } // public XmlImporter()


  // MAIN //

  /**
   * Process an Xml file as the 'root' user.
   * <p/>
   * @param   args    args[0] = name of Xml file to process
   * @throws  Exception
   * @since   1.0
   */
  public static void main(String[] args) 
    throws  Exception 
  {
    if (
      args.length == 0
      || "--h --? /h /? --help /help ${cmd}".indexOf(args[0]) > -1
    ) 
    {
      XmlImporter.commandLineUsage();
      System.exit(0);
    }

    String  arg;
    String  id                    = null;
    String  importFile            = null;
    String  importProperties      = "import.properties";
    int     inputPos              = 0;
    boolean list                  = false;
    Log     log                   = LogFactory.getLog(XmlImporter.class); // TODO Why?
    String  name                  = null;
    int     pos                   = 0;
    String  subjectIdentifier     = null;
    String  userImportProperties  = null;
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
          else if (arg.equals("-list")) {
            list = true;
            pos++;
            continue;
          } 
          else {
            throw new IllegalArgumentException("Unrecognised option " + arg);
          }
        }
        switch (inputPos) {
        case 0:
          subjectIdentifier = arg;
          break;
        case 1:
          importFile = arg;
          break;
        case 2:
          userImportProperties = arg;
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
      XmlImporter.commandLineUsage();
      System.exit(1);
    }
    Properties props = new Properties();
    if (userImportProperties != null) {
      log.info("Loading user-specified properties [" + userImportProperties + "]");
      props.load(new FileInputStream(userImportProperties));
    } 
    else {
      log.info("Loading default properties [" + importProperties + "]");
      try {
        props.load(new FileInputStream(importProperties));
      } 
      catch (Exception e) {
        log.info(
          "Failed to find [" + importProperties
          + "] in working directory, trying classpath"
        );
        try {
          InputStream is = XmlImporter.class.getResourceAsStream(importProperties);
          props.load(is);
        } 
        catch (Exception ioe) {
          log.info(
            "Failed to find [" + importProperties
            + "] in classpath, assume they are in XML"
          );
          props = null;
        }
      }
    }

    Document        doc       = XmlImporter.getDocument(importFile);
    XmlImporter     importer  = null;
    Subject         user      = SubjectFinder.findByIdentifier(subjectIdentifier);
    GrouperSession  s         = GrouperSession.start(user);

    if (props == null) {
      importer = new XmlImporter();
    } 
    else {
      importer = new XmlImporter(props);
    }
    if (list) {
      importer.loadFlatGroupsOrStems(s, doc);
    } 
    else {
      if (id == null && name == null) {
        importer.load(s, doc);
      } 
      else {
        Stem stem = null;
        if (id != null) {
          try {
            stem = StemFinder.findByUuid(s, id);
            log.debug("Found stem with id [" + id + "]");
          } catch (StemNotFoundException e) {
          }

        } 
        else {
          try {
            stem = StemFinder.findByName(s, name);
            log.debug("Found stem with name [" + name + "]");
          } catch (StemNotFoundException e) {
          }
        }
        if (stem == null) {
          if (name != null) {
            throw new IllegalArgumentException(
              "Could not find stem with name [" + name + "]"
            );
          }
          throw new IllegalArgumentException(
            "Could not find stem with id [" + id + "]"
          );
        }
        importer.load(s, stem, doc);
      }
    }
    log.info("Finished import of [" + importFile + "]");
    s.stop();
  } // public static void main(args)


  // PUBLIC CLASS METHODS //

  /**
   * @since   1.0
   */
  public static void commandLineUsage() {
    System.out.println("Usage:");
    System.out.println("args: -h,            Prints this message");
    System.out.println("args: subjectIdentifier [(-id <id> | -name <name> | -list)]");
    System.out.println("      filename [properties]");
    System.out.println();
    System.out.println("  subjectIdentifier, Identifies a Subject 'who' will create a");
    System.out.println("                     GrouperSession");
    System.out.println("  -id,               The Uuid of a Stem, into which, data will be");
    System.out.println("                     imported*");
    System.out.println("  -name,             The name of a Stem, into which, data will be");
    System.out.println("                     imported*");
    System.out.println("                     *If no -id / -name is specified, use=ROOT stem");
    System.out.println("  -list,             File contains a flat list of Stems or Groups");
    System.out.println("                     which may be updated. Missing Stems and Groups");
    System.out.println("                     are not created");
    System.out.println("  filename,          The file to import");
    System.out.println("  properties,        The name of a standard Java properties file ");
    System.out.println("                     which configures the import. Check Javadoc for");
    System.out.println("                     a list of properties. If 'properties' is not ");
    System.out.println("                      specified, XmlImporter will look for ");
    System.out.println("                     'import.properties' in the working directory. ");
    System.out.println("                     If this file does not exist XmlImporter will ");
    System.out.println("                     look on the classpath. If 'properties' is not ");
    System.out.println("                     specified and 'import.properties' cannot be ");
    System.out.println("                     found and import options are not included in ");
    System.out.println("                     the XML, the import will fail.");
    System.out.println();
  } // public static void commandLineUsage()

  /**
   * Convenience method for getting a Document given a filename
   * <p/>
   * @param   filename
   * @throws  Exception
   * @since   1.0
   */
  public static Document getDocument(String filename) 
    throws  Exception 
  {
    DocumentBuilderFactory  dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder         db  = dbf.newDocumentBuilder();
    Document                doc = db.parse(new File(filename));
    return doc;
  } // public static Document getDocument(filename)

  /**
   * Convenience method for getting a Document given an InputStream
   * <p/> 
   * @param   is
   * @throws  Exception
   * @since   1.0
   */
  public static Document getDocument(InputStream is) 
    throws  Exception 
  {
    DocumentBuilderFactory  dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder         db  = dbf.newDocumentBuilder();
    Document                doc = db.parse(is);
    return doc;
  } // public static Document getDocument(is)

  /**
   * Convenience method for getting a Document given a URL
   * <p/>
   * @param   url
   * @throws  Exception
   * @since   1.0
   */
  public static Document getDocument(URL url) 
    throws  Exception 
  {
    DocumentBuilderFactory  dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder         db  = dbf.newDocumentBuilder();
    Document                doc = db.parse(url.openStream());
    return doc;
  } // public static Document getDocument(url)


  // PUBLIC INSTANCE METHODS //

  /**
   * @return  Returns the options - includes those set through XML
   * @since   1.0
   */
  public Properties getOptions() {
    return options;
  } // public Properties getOptions()

  /**
   * Recurse through the XML document and create any stems / groups starting
   * at the ROOT stem, accumulating memberships and privilege assignments for
   * later - this ensures that any groups which will become members or have
   * privileges granted to them actually exist! Create any memberships Grant
   * naming privileges Grant access privileges
   * <p/>
   * @param   s
   * @param   doc
   * @throws  Exception
   * @since   1.0
   */
  public void load(GrouperSession s, Document doc) 
    throws  Exception 
  {
    log.info("Starting load at ROOT stem");
    processProperties(doc);
    Stem stem = StemFinder.findRootStem(s);
    load(s, stem, doc);
    log.info("Ending load at ROOT stem");
  } // public void load(s, doc)

  /**
   * Recurse through the XML document and create any stems / groups starting
   * at the stem provided, accumulating memberships and privilege assignments
   * for later - this ensures that any groups which will become members or
   * have privileges granted to them actually exist! Create any memberships
   * Grant naming privileges Grant access privileges
   * <p/>
   * @param   s
   * @param   doc
   * @throws  Exception
   * @since   1.0
   */
  public void load(GrouperSession s, Stem rootStem, Document doc)
    throws  Exception 
  {
    log.info("Starting load at " + rootStem.getName());
    processProperties(doc);
    this.s            = s;
    this.importTo     = rootStem;
    this.importToName = rootStem.getName();
    if (this.importToName.equals(sep)) {
      importToName = "";
    }
    Element root = doc.getDocumentElement();

    _processMetaData(_getImmediateElement(root, "metadata"));
    if (_isEmpty(importToName)) {
      _process(_getImmediateElement(root, "data"), NS_ROOT);
    } 
    else {
      _process(_getImmediateElement(root, "data"), importToName);
    }
    Runtime.getRuntime().gc();
    _processMemberships();
    _processMembershipLists();
    Runtime.getRuntime().gc();
    _processNamingPrivs();
    _processNamingPrivLists();
    Runtime.getRuntime().gc();
    _processAccessPrivs();
    _processAccessPrivLists();
    log.info("Ending load at " + rootStem.getName());
  } // public void load(s, rootStem, doc)

  /**
   * Iterate over list of stems or groups and update any memberships, naming
   * privileges and access privileges accordingly. This method does not create
   * missing stems or groups
   * <p/> 
   * @param   s
   * @param   doc
   * @throws  Exception
   * @since   1.0
   */
  public void loadFlatGroupsOrStems(GrouperSession s, Document doc)
    throws  Exception 
  {
    log.info("Starting flat load");
    processProperties(doc);
    this.s = s;

    Element root = doc.getDocumentElement();

    _processMetaData(_getImmediateElement(root, "metadata"));
    Element dataE = _getImmediateElement(root, "dataList");
    Collection groupElements = _getImmediateElements(dataE, "group");
    if (groupElements != null) {
      Iterator groupsIterator = groupElements.iterator();
      Element groupElement;
      while (groupsIterator.hasNext()) {
        groupElement = (Element) groupsIterator.next();
        _processGroup(groupElement);
      }
    }

    Collection stemElements = _getImmediateElements(root, "stem");
    if (stemElements != null) {
      Iterator stemsIterator = stemElements.iterator();
      Element stemElement;
      while (stemsIterator.hasNext()) {
        stemElement = (Element) stemsIterator.next();
    
      _processStem(stemElement);
      }
    }
    Runtime.getRuntime().gc();

    _processMembershipLists();
    Runtime.getRuntime().gc();

    _processNamingPrivLists();
    Runtime.getRuntime().gc();

    _processAccessPrivLists();
    log.info("Ending flat load");
  } // public void loadFlatGropusOrStems(s, doc)

  /**
   * @param   options   The options to set. These options supplement
   *                    and override those defined in the XML.
   * @since   1.0
   */
  public void setOptions(Properties options) {
    this.options = options;
  } // public void setOptions(options)


  // PROTECTED INSTANCE METHODS //

  // @since   1.0
  protected Properties getImportOptionsFromXml(Document doc) 
    throws  Exception 
  {
    log.debug("Attempting to find importOptions in XML");
    Element rootE = doc.getDocumentElement();
    Element importOptionsE = _getImmediateElement(rootE, "importOptions");
    if (importOptionsE == null) {
      log.debug("No importOptions tag in XML");
      return null;
    }
    log.debug("Found importOptions tag in XML - loading options");
    Collection options = _getImmediateElements(importOptionsE, "options");
    Element optionE;
    Properties props = new Properties();
    Iterator it = options.iterator();
    while (it.hasNext()) {
      optionE = (Element) it.next();
      props.put(optionE.getAttribute("key"), _getText(optionE));
      log.debug("Loading " + optionE.getAttribute("key") + "="
          + _getText(optionE));
    }
    log.debug("Finished loading options from XML");

    return props;
  } // protected Properties getImportedOptionsFromXml(doc)

  // @since   1.0
  protected void processProperties(Document doc) 
    throws  Exception 
  {
    options               = safeOptions;
    Properties xmlOptions = getImportOptionsFromXml(doc);
    if (xmlOptions == null && options.isEmpty()) {
      throw new IllegalStateException("No options have been set");
    }
    if (xmlOptions == null)
      return;
    log.info("Merging user supplied options with XML options. Former take precedence");
    options = new Properties(xmlOptions);
    options.putAll(safeOptions);
  } // protected void processProperties(doc)


  // PRIVATE CLASS METHODS //

  /*
   * Assumes tag only occurs once and contains only text / CDATA If tag does
   * not exist 'nullable' determines if an Exception is thrown
   * @since   1.0
   */
  private static String _getText(Element element) 
    throws  Exception 
  {
    element.normalize();
    NodeList nl = element.getChildNodes();
    if (nl.getLength() != 1) {
      throw (new Exception("Cannot process " + element.getTagName() + " tag"));
    }
    Node n = nl.item(0);
    if (
      n.getNodeType() != Node.TEXT_NODE
      && n.getNodeType() != Node.CDATA_SECTION_NODE
    ) 
    {
      throw (new Exception("Cannot process " + element.getTagName() + " tag"));
    }
    return ((CharacterData) n).getData().trim();
  } // private static String _getText(element)


  // PRIVATE INSTANCE METHODS //

  // @since   1.0
  private String _getAbsoluteName(String name, String stem) {
    if ("*SELF*".equals(name)) {
      return stem;
    }
    if (name != null && name.startsWith(".")) {
      if (name.startsWith("." + sep)) {
        name = stem + name.substring(1);
      } 
      else {
        while (name.startsWith(".." + sep)) {
          name = name.substring(3);
          stem = stem.substring(0, stem.lastIndexOf(sep));
        }
        name = stem + sep + name;
      }
    }
    if (
      !_isEmpty(importToName)
      && importedGroups.containsKey(importToName + sep + name)
    ) 
    {
      return importToName + sep + name;
    }
    return name;
  } // private String _getAbsoluteName(name, stem)

  // Returns immediate child element with given name
  // @since   1.0
  private Element _getImmediateElement(Element element, String elementName)
    throws  Exception 
  {
    NodeList    nl        = element.getElementsByTagName(elementName);
    Collection  elements  = new Vector();
    if (nl.getLength() < 1) {
      return null;
    }
    if (nl.getLength() > 1) {
      throw new IllegalArgumentException(
        elementName + " occurs more than once - should only occur once"
      );
    }
    return (Element) nl.item(0);
  } // private Element _getImmediateElement(element, elementName)

  // Returns immediate child elements with given name
  // @since   1.0
  private Collection _getImmediateElements(Element element, String elementName)
    throws Exception 
  {
    NodeList    nl        = element.getElementsByTagName(elementName);
    Collection  elements  = new Vector();
    if (nl.getLength() < 1) {
      return elements;
    }
    Element child;
    for (int i = 0; i < nl.getLength(); i++) {
      child = (Element) nl.item(i);
      if (child.getParentNode().equals(element)) {
        elements.add(child);
      }
    }
    return elements;
  } // private Collection _getImmediateElements(element, elementName)

  // @since   1.0
  private Subject _getSubjectById(String id, String type) 
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (_isEmpty(type)) {
      return SubjectFinder.findById(id);
    }
    return SubjectFinder.findById(id, type);
  } // private Subject _getSubjectById(id, type)

  // @since   1.0
  private Subject _getSubjectByIdentifier(String identifier, String type)
    throws  SubjectNotFoundException,
            SubjectNotUniqueException
  {
    if (_isEmpty(type)) {
      return SubjectFinder.findByIdentifier(identifier);
    }
    return SubjectFinder.findByIdentifier(identifier, type);
  } // private Subject _getSubjectByIdentifier(identifier, type)

  // @since   1.0
  // TODO Isn't this also in XmlExporter?
  private boolean _isEmpty(Object obj) {
    if (obj == null || "".equals(obj)) {
      return true;
    }
    return false;
  } // private boolean _isEmpty(obj)

  // @since   1.0
  // TODO This can be replaced
  private String _joinStem(String stem, String extension) {
    if (stem.equals(NS_ROOT)) {
      return extension;
    }
    return stem + sep + extension;
  } // private String _joinStem(stem, extension)

  // @since   1.0
  // TODO     Is this even called?
  private boolean _optionDefaultEquals(String key, String value, String value1) {
    if (_isEmpty(value) || _isEmpty(key)) {
      return false;
    }
    if (!_isEmpty(value1)) {
      return value.equals(value1);
    }
    return value1.equals(options.getProperty(key));
  } // private boolean _optionDefaultEquals(key, value, value1)

  // @since   1.0
  // TODO     Is this even called?
  private boolean _optionEquals(String key, String value) {
    if (_isEmpty(key) || _isEmpty(value)) {
      return false;
    }
    return value.equals(options.getProperty(key));
  } // private boolean _optionEquals(key, value)

  // @since   1.0
  private boolean _optionTrue(String key) {
    if (_isEmpty(key)) {
      options.setProperty(key, "false");
      return false;
    }
    return "true".equals(options.getProperty(key));
  } // private boolean _optionTrue(key)

  /*
   * For each stem list and process any child stems. List and process any
   * child groups
   * <p/> 
   * @param   e
   * @param   stem
   * @throws  Exception
   * @since   1.0
   */
  private void _process(Element e, String stem) 
    throws  Exception 
  {
    if (e == null) {
      return;
    }
    Collection paths = _getImmediateElements(e, "path");
    paths.addAll(_getImmediateElements(e, "stem"));
    if (log.isDebugEnabled()) {
      log.debug("Found " + paths.size() + " stems");
    }

    Iterator it = paths.iterator();
    while (it.hasNext()) {
      Element path = (Element) it.next();
      _processPath(path, stem);
    }

    Collection groups = _getImmediateElements(e, "group");
    if (log.isDebugEnabled()) {
      log.debug("Found " + groups.size() + " groups");
    }
    it = groups.iterator();
    while (it.hasNext()) {
      Element group = (Element) it.next();
      _processGroup(group, stem);
    }
  } // private void _process(e, stem)

  // @since   1.0
  private void _processAccess(Element e, String stem) 
    throws  Exception 
  {
    Collection  accesses  = _getImmediateElements(e, "access");
    Iterator    it        = accesses.iterator();
    Element     access;
    Map         map;
    while (it.hasNext()) {
      access = (Element) it.next();
      map = new HashMap();
      map.put("stem", stem);
      map.put("access", access);
      accessPrivs.add(map);
    }
  } // private void _processAccess(e, stem)

  // @since   1.0
  private void _processAccessPrivLists() 
    throws  Exception 
  {
    if (accessPrivLists == null || accessPrivLists.size() == 0) {
      return;
    }
    Collection  subjects;
    Iterator    subjectsIterator;
    Element     subjectE;
    Element     privileges;
    Member      member            = null;
    Map         map;
    String      group;
    Subject     subject           = null;
    String      subjectType;
    boolean     isImmediate       = false;
    Group       privGroup;
    Group       focusGroup        = null;
    String      subjectId;
    String      subjectIdentifier;
    String      privilege;
    Privilege   grouperPrivilege;
    String      importOption;
    String      lastGroup         = "";
    for (int i = 0; i < accessPrivLists.size(); i++) {
      map   = (Map) accessPrivLists.get(i);
      group = (String) map.get("group");

      //Save a call if we are dealing with same group
      if (!group.equals(lastGroup)) {
        if (!_isEmpty(lastGroup)) {
          if (log.isInfoEnabled())
            log.info("Finished loading Access privs for " + lastGroup);
        }
        focusGroup = GroupFinder.findByName(s, group);
        if (log.isInfoEnabled()) {
          log.info("Loading Access privs for " + group);
        }
      }

      lastGroup     = group;
      privileges    = (Element) map.get("privileges");
      privilege     = privileges.getAttribute("type");
      importOption  = privileges.getAttribute("importOption");
      if (_isEmpty(importOption))
        importOption = options.getProperty("import.data.privileges");

      if (_isEmpty(importOption) || "ignore".equals(importOption)) {
        if (log.isInfoEnabled()) {
          log.info("Ignoring any '" + privilege + "' privileges");
        }
        continue; //No instruction so ignore
      }
      grouperPrivilege = Privilege.getInstance(privilege);
      if ("replace".equals(importOption)) {
        if (log.isInfoEnabled()) {
          log.info("Revoking current '" + privilege + "' privileges");
        }
        focusGroup.revokePriv(grouperPrivilege);
      }
      subjects          = _getImmediateElements(privileges, "subject");
      subjectsIterator  = subjects.iterator();
      while (subjectsIterator.hasNext()) {
        subjectE    = (Element) subjectsIterator.next();
        isImmediate = "true".equals(subjectE.getAttribute("immediate"));
        if (_isEmpty(subjectE.getAttribute("immediate"))) {
          isImmediate = true; //default is to assign
        }
        if (!isImmediate) {
          continue;
        }

        subjectId         = subjectE.getAttribute("id");
        subjectIdentifier = subjectE.getAttribute("identifier");
        subjectType       = subjectE.getAttribute("type");
        if ("group".equals(subjectType)) {
          if (
            subjectIdentifier.startsWith("*")
            && !subjectIdentifier.endsWith("*")
          ) 
          {
            //relative import
            if (!_isEmpty(importToName)) {
              subjectIdentifier = importToName + sep + subjectIdentifier.substring(1);
            }
            else {
              subjectIdentifier = subjectIdentifier.substring(1);
            }
          } 
          else {
            if ("*SELF*".equals(subjectIdentifier)) {
              subjectIdentifier = group;
            }
            else {
              subjectIdentifier = _getAbsoluteName(
                  subjectIdentifier, focusGroup.getParentStem().getName()
              );
            }
          }
          try {
            privGroup = GroupFinder.findByName(s, subjectIdentifier);
          } 
          catch (Exception e) {
            log.warn("Could not find Group identified by " + subjectIdentifier);
            continue;
          }

          subject = privGroup.toMember().getSubject();
        } 
        else {
          try {
            if (_isEmpty(subjectId)) {
              subject = _getSubjectByIdentifier(subjectIdentifier, subjectType);
            } 
            else {
              subject = _getSubjectById(subjectId, subjectType);
            }
          } 
          catch (Exception e) {
            String msg = "Could not find subject with ";
            if (_isEmpty(subjectId)) {
              msg = msg + "identifier=" + subjectIdentifier;
            }
            else {
              msg = msg + "id=" + subjectId;
            }
            log.error(msg);
            continue;
          }
        }

        if (
          !XmlExporter.hasImmediatePrivilege( subject, focusGroup, privilege)
        ) 
        {
          if (log.isDebugEnabled()) {
            log.debug("Assigning " + privilege + " to " + subject.getName() + " for " + group);
          }
          focusGroup.grantPriv(subject, Privilege.getInstance(privilege));
          if (log.isDebugEnabled()) {
            log.debug("... finished assignment");
          }
        } 
        else {
          if (log.isDebugEnabled()) {
            log.debug(privilege + " already assigned to " + subject.getName() + " so skipping");
          }
        }
      }
    }
    if (log.isInfoEnabled()) {
      log.info("Finished assigning Access privs");
    }
    accessPrivLists = null;
  } // private void _processAccessPrivLists()

  // @since   1.0
  private void _processAccessPrivs() 
    throws  Exception 
  {
    Element access;
    String  stem;
    Member  member = null;
    Group   grouperGroup;
    Map     map;
    String  group;
    String  subject;
    String  priv;
    String  absoluteGroup;
    Group   privGroup;
    Subject subj;
    for (int i = 0; i < accessPrivs.size(); i++) {
      map           = (Map) accessPrivs.get(i);
      access        = (Element) map.get("access");
      stem          = (String) map.get("stem");
      group         = access.getAttribute("group");
      subject       = access.getAttribute("subject");
      priv          = access.getAttribute("priv").toLowerCase();
      grouperGroup  = GroupFinder.findByName(s, stem);
      if (!_isEmpty(group)) {
        absoluteGroup = _getAbsoluteName(group, stem);
        privGroup = GroupFinder.findByName(s, absoluteGroup);
        member = MemberFinder.findBySubject(
          s, SubjectFinder.findById( privGroup.getUuid(), "group")
        );

        System.out.println(
          "Assigning " + priv + " to " + absoluteGroup + " for " + stem
        );
      } 
      else if (!_isEmpty(subject)) {
        try {
          subj = SubjectFinder.findByIdentifier(subject);
        } 
        catch (SubjectNotFoundException e) {
          subj = SubjectFinder.findById(subject);
        }
        member = MemberFinder.findBySubject(s, subj);
        System.out.println(
          "Assigning " + priv + " to " + subj.getName() + " for " + stem
        );
      }
      if (
        !XmlExporter.hasImmediatePrivilege(
          member.getSubject(), grouperGroup, priv
        )
      ) 
      {
        grouperGroup.grantPriv(
          member.getSubject(), Privilege.getInstance(priv)
        );
        System.out.println("...assigned");
      } 
      else {
        System.out.println("...already assigned - skiping");
      }
    }
    accessPrivs = null;
  } // private void _processAccessPrivs()

  // @since   1.0
  private void _processAttributes(Element e, String stem) 
    throws  Exception 
  {
    Element groupTypes = _getImmediateElement(e, "groupTypes");
    if (groupTypes == null) {
      return;
    }
    Collection  types               = _getImmediateElements(groupTypes, "groupType");
    Element     groupType;
    Iterator    typesIterator       = types.iterator();
    Collection  attributes;
    Element     attribute;
    String      name;
    String      value;
    String      origValue           = null;
    Iterator    attributesIterator;
    Group       group               = GroupFinder.findByName(s, stem);
    GroupType   grouperGroupType    = null;
    while (typesIterator.hasNext()) {
      groupType = (Element) typesIterator.next();
      if ("base".equals(groupType.getAttribute("name"))) {
        continue;
      }
      try {
        grouperGroupType = GroupTypeFinder.find(groupType
            .getAttribute("name"));
      } 
      catch (Exception ex) {
        continue;
      }
      if (!group.hasType(grouperGroupType)) {
        if (_optionTrue("import.metadata.apply-new-group-types")) {
          group.addType(grouperGroupType);
        } 
        else {
          continue;
        }
      }
      attributes = _getImmediateElements(groupType, "attribute");
      attributesIterator = attributes.iterator();
      Field field = null;
      while (attributesIterator.hasNext()) {
        attribute = (Element) attributesIterator.next();
        name      = attribute.getAttribute("name");
        field     = FieldFinder.find(name);
        if (!group.canWriteField(field)) {
          log.info(
            "No write privilege. Attribute [" + name + "] for [" + group.getName() + "] ignored"
          );
          continue;
        }
        value = ((Text) attribute.getFirstChild()).getData();
        try {
          origValue = group.getAttribute(name);
        }   
        catch (Exception ex) {
        }
        if (
          value != null
          && !value.equals(origValue)
          && (_isEmpty(origValue) || _optionTrue("import.data.update-attributes"))
        )
        {
          group.setAttribute(name, value);
        }
      }
    }
  } // private void _processAttributes(e, stem) 

  // @since   1.0
  private void _processComposite(Element composite, Group group)
    throws  Exception 
  {
    log.debug("Processing composite for " + group.getName());
    if (group.hasComposite()) { 
      log.warn(group.getName() + " already has composite - skipping");
      return;
    }
    Group         leftGroup   =  null;
    CompositeType compType    = null;
    Group         rightGroup  = null;
    composite.normalize();
    Element[]     elements    = new Element[3];
    NodeList      nl          = composite.getChildNodes();
    int           elCount     = -1;
    Node          node;
    for (int i = 0; i < nl.getLength(); i++) {
      node = nl.item(i);
      if (node instanceof Element) {
        elCount++;
        if (elCount > 2) {
          throw new IllegalStateException(
              "Too many tags in <composite>. Expect <groupRef><compositeType><groupRef>"
          );
        }
        elements[elCount] = (Element) node;
      }
    }
    try {
      Element leftE   = elements[0];
      leftGroup       = _processGroupRef(leftE, group.getParentStem().getName());
      Element typeE   = elements[1];
      compType        = _processCompositeType(typeE);
      Element rightE  = elements[2];
      rightGroup      = _processGroupRef(rightE, group.getParentStem() .getName());
    } 
    catch (Exception e) {
      log.error("Error processing composite for " + group.getName(), e);
      return;
    }
    try {
      group.addCompositeMember(compType, leftGroup, rightGroup);
    } 
    catch (Exception e) {
      log.error("Error adding composite for " + group.getName(), e);
    }
  } // private void _processComposite(composite, group)

  // @since   1.0
  private CompositeType _processCompositeType(Element typeE) 
    throws  Exception 
  {
    CompositeType type    = null;
    String        tagName = typeE.getTagName();
    if (!"compositeType".equals(tagName)) {
      throw new IllegalStateException(
          "Expected tag: <compositeType> but found <" + tagName + ">"
      );
    }
    String name = _getText(typeE);
    if ("intersection".equals(name)) {
      type = CompositeType.INTERSECTION;
    } else if ("union".equals(name)) {
      type = CompositeType.UNION;
    } else if ("complement".equals(name)) {
      type = CompositeType.COMPLEMENT;
    } else {
      throw new IllegalStateException(
        "Invalid CompositeType [" + name + "]. union, intersection or complement allowed"
      );
    }
    return type;
  }  // private CompositeType _processCompositeType(typeE)

  // @since   1.0
  private void _processGroup(Element e) 
    throws  Exception 
  {
    String  extension         = e.getAttribute("extension");
    String  displayExtension  = e.getAttribute("displayExtension");
    Element descE             = _getImmediateElement(e, "description");
    String  description       = "";

    if (descE != null) {
      description = _getText(descE);
    }
    String  id                = e.getAttribute("id");
    String  name              = e.getAttribute("name");

    Group   existingGroup     = null;
    String  updateAttributes  = e.getAttribute("updateAttributes");
    if (_isEmpty(updateAttributes)) {
      updateAttributes = options.getProperty("import.data.update-attributes");
    }
    try {
      if (!_isEmpty(id)) {
        existingGroup = GroupFinder.findByUuid(s, id);
      } 
      else if (!_isEmpty(name)) {
        existingGroup = GroupFinder.findByName(s, name);
      } 
      else {
        log.error("Group does not have id or name=" + extension);
        return;
      }
      if ("true".equals(updateAttributes)) {
        if (
          !_isEmpty(displayExtension)
          && !displayExtension.equals(existingGroup.getDisplayExtension())
        )
        {
          existingGroup.setDisplayExtension(displayExtension);
        }
        if (
          !_isEmpty(description)
          && !description.equals(existingGroup.getDescription())
        )
        {
          existingGroup.setDescription(description);
        }
        _processAttributes(e, existingGroup.getName());
      }

    } 
    catch (GroupNotFoundException ex) {
      log.error("Cannot find Group identified by id=" + id + " or name:" + name);
      return;
    }

    _processLists(e, existingGroup.getName());
    _processPrivileges(e, existingGroup.getName(), "access");
  } // private void _processGroup(e)

  // @since   1.0
  private void _processGroup(Element e, String stem) 
    throws  Exception 
  {
    if (importedGroups == null) {
      importedGroups = new HashMap();
    }
    String  extension        = e.getAttribute("extension");
    String  displayExtension = e.getAttribute("displayExtension");
    String  description      = e.getAttribute("description");
    String  newGroup         = _joinStem(stem, extension);
    if (log.isInfoEnabled()) {
      log.info("Creating group [" + newGroup + "]");
    }
    Group   existingGroup     = null;
    String  updateAttributes  = e.getAttribute("updateAttributes");
    if (_isEmpty(updateAttributes)) {
      updateAttributes = options.getProperty("import.data.update-attributes");
    }
    try {
      existingGroup = GroupFinder.findByName(s, newGroup);
      if ("true".equals(updateAttributes)) {
        if (
          !_isEmpty(displayExtension)
          && !displayExtension.equals(existingGroup.getDisplayExtension())
        )
        {
          existingGroup.setDisplayExtension(displayExtension);
        }
        if (
          !_isEmpty(description) 
          && !description.equals(existingGroup.getDescription())
        )
        {
          existingGroup.setDescription(description);
        }
        _processAttributes(e, stem);
      }
      importedGroups.put(existingGroup.getName(), "e");
      if (log.isInfoEnabled()) {
        log.info(newGroup + " already exists - skipping");
      }
    } 
    catch (GroupNotFoundException ex) {
    }
    if (existingGroup == null) {
      Stem  parent  = StemFinder.findByName(s, stem);
      Group gg      = parent.addChildGroup(extension, displayExtension);
      importedGroups.put(gg.getName(), "c");
      if (log.isInfoEnabled()) {
        log.info(newGroup + " added");
      }
      if (description != null && description.length() != 0) {
        gg.setDescription(description);
      }
    }
    _processSubjects(e, newGroup);
    _processLists(e, newGroup);
    _processPrivileges(e, newGroup, "access");
    _processAccess(e, newGroup);
  } // private void _processGroup(e, stem)

  // @since   1.0
  private Group _processGroupRef(Element groupE, String stem) 
    throws  Exception 
  {
    Group   group   = null;
    String  tagName = groupE.getTagName();
    if (!"groupRef".equals(tagName)) {
      throw new IllegalStateException(
        "Expected tag: <groupRef> but found <" + tagName + ">"
      );
    }
    String name = groupE.getAttribute("name");
    if (_isEmpty(name)) {
      throw new IllegalStateException(
        "Expected 'name' atribute for <groupRef>"
      );
    }
    String actualName = _getAbsoluteName(name, stem);
    group             = GroupFinder.findByName(s, actualName);
    return group;
  } // private Group _processGroupRef(groupE, stem)

  // @since   1.0
  private void _processLists(Element e, String group) 
    throws  Exception 
  {
    Collection  lists = _getImmediateElements(e, "list");
    Iterator    it    = lists.iterator();
    Element     list;
    Map map;
    while (it.hasNext()) {
      list  = (Element) it.next();
      map   = new HashMap();
      map.put("group", group);
      map.put("list", list);
      membershipLists.add(map);
    }
  } // private void _processLists(e, group)

  // @since   1.0
  private void _processMembershipLists() 
    throws  Exception 
  {
    if (this.membershipLists == null || this.membershipLists.size() == 0) {
      return;
    }
    Collection  subjects;
    Iterator    subjectsIterator;
    Element     subjectE;
    Element     list;
    String      listName;
    Field       field             = null;
    Subject     subject;
    String      groupName;
    String      lastGroupName     = "";
    Member      member;
    Group       group             = null;
    Map         map;
    String      subjectId;
    String      subjectIdentifier;
    String      subjectType;
    Group       privGroup;
    boolean     isImmediate;
    String      importOption;
/* FIXME !!! */
    for (int i = 0; i < membershipLists.size(); i++) {
      map           = (Map) membershipLists.get(i);
      list          = (Element) map.get("list");
      importOption  = list.getAttribute("importOption");
      if (_isEmpty(importOption)) {
        importOption = options.getProperty("import.data.lists");
      }
      if (_isEmpty(importOption) || "ignore".equals(importOption)) {
        continue; //No instruction so ignore
      }
      groupName = (String) map.get("group");

      //Save a call if we are dealing with same group
      if (!groupName.equals(lastGroupName)) {
        if (!_isEmpty(lastGroupName)) {
          if (log.isInfoEnabled()) {
            log.info("Finished loading memberships for " + lastGroupName);
          }
        }
        group = GroupFinder.findByName(s, groupName);
        if (log.isInfoEnabled()) {
          log.info("Loading memberships for " + groupName);
        }
      }

      lastGroupName = groupName;

      listName      = list.getAttribute("field");
      try {
        field = FieldFinder.find(listName);
        if (!field.getType().equals(FieldType.LIST)) { 
          log.error(listName + " is not a list");
          continue;
        }
      } 
      catch (Exception e) {
        log.error("Cannot find list " + listName);
        continue;
      }
      //TODO add admin check?
      if (!group.hasType(field.getGroupType())) {
        if (_optionTrue("import.data.apply-new-group-types")) {
          if (log.isInfoEnabled()) {
            log.info("Adding group type " + field.getGroupType());
          }
          group.addType(field.getGroupType());
        } 
        else {
          if (log.isInfoEnabled()) {
            log.info("Ignoring field " + field.getName());
          }
          continue;
        }
      }
      if (!group.canReadField(field)) {
        log.info("No write privilege - ignoring field " + field.getName());
        continue;
      }
      boolean hasComposite  = group.hasComposite();
      boolean hasMembers    = false;
      if (!hasComposite && group.getImmediateMembers().size() > 0) {
        hasMembers = true;
      }
      Element compE = _getImmediateElement(list, "composite");

      if ("replace".equals(importOption)) {
        if (hasComposite) {
          group.deleteCompositeMember();
        } 
        else {
          Set       members         = group.getImmediateMembers(field);
          Iterator  membersIterator = members.iterator();
          Member    memb;
          if (log.isInfoEnabled()) {
            log.info("Removing all memberships for " + groupName);
          }
          while (membersIterator.hasNext()) {
            memb = (Member) membersIterator.next();
            group.deleteMember(memb.getSubject());
          }
        }
      }
      if (compE != null && (!"add".equals(importOption) || hasMembers)) {
        _processComposite(compE, group);
        return;
      }
      if (compE != null && hasMembers) {
        log.warn("Skipping composite - cannot ad to existing members for " + groupName);
        return;
      }
      subjects          = _getImmediateElements(list, "subject");
      subjectsIterator  = subjects.iterator();
      while (subjectsIterator.hasNext()) {
        subjectE    = (Element) subjectsIterator.next();
        isImmediate = "true".equals(subjectE.getAttribute("immediate"));
        if (_isEmpty(subjectE.getAttribute("immediate"))) {
          isImmediate = true;
        }
        if (!isImmediate) {
          continue;
        }

        subjectId         = subjectE.getAttribute("id");
        subjectIdentifier = subjectE.getAttribute("identifier");
        subjectType       = subjectE.getAttribute("type");
        if ("group".equals(subjectType)) {
          if (
              subjectIdentifier.startsWith("*")
              && !subjectIdentifier.endsWith("*")
          ) 
          {
            //relative import
            if (!_isEmpty(importToName)) {
              subjectIdentifier = importToName + sep + subjectIdentifier.substring(1);
            }
            else {
              subjectIdentifier = subjectIdentifier.substring(1);
            }
          } else {
            if ("*SELF*".equals(subjectIdentifier)) {
              subjectIdentifier = groupName;
            }
            else {
              subjectIdentifier = _getAbsoluteName(
                subjectIdentifier, group.getParentStem().getName()
              );
            }
          }
          try {
            privGroup = GroupFinder.findByName(s, subjectIdentifier);
          } 
          catch (Exception e) {
            log.warn("Could not find Group identified by " + subjectIdentifier);
            continue;
          }

          subject = privGroup.toMember().getSubject();
        } 
        else {
          try {
            if (_isEmpty(subjectId)) {
              subject = _getSubjectByIdentifier(subjectIdentifier, subjectType);
            } 
            else {
              subject = _getSubjectById(subjectId, subjectType);
            }
          } 
          catch (Exception e) {
            String msg = "Could not find subject with ";
            if (_isEmpty(subjectId)) {
              msg = msg + "identifier=" + subjectIdentifier;
            }
            else {
              msg = msg + "id=" + subjectId;
            }
            log.error(msg);
            }
            continue;
          }
        }
        if (!group.hasImmediateMember(subject, field)) {
          if (log.isDebugEnabled()) {
            log.debug(
              "Making " + subject.getName()
              + " a member of " + group.getName() + "(list="
              + listName + ")"
            );
          }
          group.addMember(subject, field);
          if (log.isDebugEnabled()) {
            log.debug("...assigned");
          }
        } 
        else {
          if (log.isDebugEnabled()) {
            log.debug(
              subject.getName()
              + " is already a member of " + group.getName()
              + "- skipping"
            );
          }
        }
    }
    this.membershipLists = null;
  } // private void _processMembershipLists()

  // @since   1.0
  private void _processMemberships() 
    throws  Exception 
  {
    Element subject;
    String  stem;
    Member  member;
    Group   group;
    Map     map;
    for (int i = 0; i < memberships.size(); i++) {
      map         = (Map) memberships.get(i);
      subject     = (Element) map.get("subject");
      stem        = (String) map.get("stem");
      String  id = subject.getAttribute("id");
      group       = GroupFinder.findByName(s, stem);
      if (id != null && id.length() != 0) {
        System.out.println("Making " + id + " a member of " + group.getName());
        member = MemberFinder.findBySubject(s, SubjectFinder.findById( id, "person"));

        if (group != null && !group.hasMember(member.getSubject())) {
          group.addMember(member.getSubject());
          System.out.println("...added");
        } 
        else {
          System.out.println("...already a member - skipping");
        }
      } 
      else {
        String groupName = subject.getAttribute("group");
        if (groupName != null && groupName.length() != 0) {
          if ("relative".equals(subject.getAttribute("location"))) {
            groupName = group.getParentStem().getName() + sep + groupName;
          }
          System.out.println("Making [" + groupName + "] a member of " + group.getName());
          Group   groupSubj = GroupFinder.findByName(s, groupName);
          member            = MemberFinder.findBySubject(
            s, SubjectFinder.findById(groupSubj.getUuid(), "group")
          );
          if (group != null && !group.hasMember(member.getSubject())) {
            group.addMember(member.getSubject());
            System.out.println("...added");
          } 
          else {
            System.out.println("...already a member - skipping");
          }
        }
      }
    }
    memberships = null;
  } // private void _processMemberships()

  // @since   1.0
  private void _processMetaData(Element e) 
    throws  Exception 
  {
    if (!_optionTrue("import.metadata.group-types") || e == null) {
      return;
    }
    log.debug("import.metadata.group-types=true - loading group-types");
    Element     groupTypesMetaData  = _getImmediateElement(e, "groupTypesMetaData");
    Collection  groupTypes          = _getImmediateElements(groupTypesMetaData, "groupTypeDef");
    Iterator    groupTypesIterator  = groupTypes.iterator();
    Element     groupType;
    GroupType   grouperGroupType;
    Collection  fields;
    Iterator    fieldsIterator;
    Field       grouperField;
    Element     field;
    String      groupTypeName;
    String      fieldName;
    String      readPriv;
    String      writePriv;
    String      fieldType;
    boolean     required;
    boolean     isNew               = false;
    while (groupTypesIterator.hasNext()) {
      isNew         = false;
      groupType     = (Element) groupTypesIterator.next();
      groupTypeName = groupType.getAttribute("name");
      try {
        grouperGroupType = GroupTypeFinder.find(groupTypeName);
        log.debug("Found existing GroupType - " + groupTypeName);
      } 
      catch (SchemaException ex) {
        grouperGroupType  = GroupType.createType(s, groupTypeName);
        isNew             = true;
        log.debug("Found and created new GroupType - " + groupTypeName);
      }
      fields = _getImmediateElements(groupType, "field");
      if (fields.size() > 0) {
        log.debug("import.metadata.group-type-attributes=true");
      }
      fieldsIterator = fields.iterator();
      while (fieldsIterator.hasNext()) {
        field     = (Element) fieldsIterator.next();
        fieldName = field.getAttribute("name");
        fieldType = field.getAttribute("type");
        required  = "true".equals(field.getAttribute("required"));
        readPriv  = field.getAttribute("readPriv");
        writePriv = field.getAttribute("writePriv");
        try {
          grouperField = FieldFinder.find(fieldName);
          log.debug("Found existing Field - " + fieldName);
        } 
        catch (SchemaException ex) {
          grouperField = null;
        }
        if (
          (isNew || _optionTrue("import.metadata.group-type-attributes"))
          && grouperField == null
        ) 
        {
          log.debug("Found new Field - "  + fieldName + " - now adding");
          log.debug("Field Type="         + fieldType);
          log.debug("Field readPriv="     + readPriv);
          log.debug("Field writePriv="    + writePriv);
          log.debug("Field required="     + required);

          if (fieldType.equals("list")) {
            grouperGroupType.addList(
              s, fieldName, 
              Privilege.getInstance(readPriv), 
              Privilege.getInstance(writePriv)
            );
          } 
          else if (fieldType.equals("attribute")) {
            grouperGroupType.addAttribute(
              s, fieldName, 
              Privilege.getInstance(readPriv), 
              Privilege.getInstance(writePriv), required
            );
          } 
          else {

          }
        }
      }
    }
    log.debug("Finished processing group types and fields");
  } // private void _processMetaData(e)

  // @since   1.0
  private void _processNaming(Element e, String stem) 
    throws  Exception 
  {
    Collection  namings = _getImmediateElements(e, "naming");
    Iterator    it      = namings.iterator();
    Element     naming;
    Map map;
    while (it.hasNext()) {
      naming  = (Element) it.next();
      map     = new HashMap();
      map.put("stem", stem);
      map.put("naming", naming);
      namingPrivs.add(map);
    }
  } // private void _processNaming(e, stem)

  // @since   1.0
  private void _processNamingPrivLists() 
    throws  Exception 
  {
    if (namingPrivLists == null || namingPrivLists.size() == 0) {
      return;
    }
    Collection  subjects;
    Iterator    subjectsIterator;
    Element     subjectE;
    Element     privileges;
    Member      member            = null;
    Map         map;
    String      stem;
    String      lastStem          = "";
    Subject     subject           = null;
    String      subjectType;
    boolean     isImmediate       = false;
    Group       privGroup;
    Stem        focusStem         = null;
    String      subjectId;
    String      subjectIdentifier;
    String      privilege;
    Privilege   grouperPrivilege;
    String      importOption;
    for (int i = 0; i < namingPrivLists.size(); i++) {
      map   = (Map) namingPrivLists.get(i);
      stem  = (String) map.get("stem");

      //Save a call if we are dealing with same group
      if (!stem.equals(lastStem)) {
        if (!_isEmpty(lastStem)) {
          if (log.isInfoEnabled()) {
            log.info("Finished loading Naming privs for " + lastStem);
          }
        }
        focusStem = StemFinder.findByName(s, stem);
        if (log.isInfoEnabled()) {
          log.info("Loading Naming privs for " + stem);
        }
      }

      lastStem      = stem;

      privileges    = (Element) map.get("privileges");
      privilege     = privileges.getAttribute("type");
      importOption  = privileges.getAttribute("importOption");
      if (_isEmpty(importOption)) {
        importOption = options.getProperty("import.data.privileges");
      }
      if (_isEmpty(importOption) || "ignore".equals(importOption)) {
        if (log.isInfoEnabled()) {
          log.info("Ignoring any '" + privilege + "' privileges");
        }
        continue; //No instruction so ignore
      }

      grouperPrivilege = Privilege.getInstance(privilege);
      if ("replace".equals(importOption)) {
        if (log.isDebugEnabled()) {
          log.info("Revoking current '" + privilege + "' privileges");
        }
        focusStem.revokePriv(grouperPrivilege);
      }

      subjects          = _getImmediateElements(privileges, "subject");
      subjectsIterator  = subjects.iterator();
      while (subjectsIterator.hasNext()) {
        subjectE    = (Element) subjectsIterator.next();
        isImmediate = "true".equals(subjectE.getAttribute("immediate"));
        if (_isEmpty(subjectE.getAttribute("immediate"))) {
          isImmediate = true; //default is to assign
        }
        if (!isImmediate) {
          continue;
        }

        subjectId         = subjectE.getAttribute("id");
        subjectIdentifier = subjectE.getAttribute("identifier");
        subjectType       = subjectE.getAttribute("type");
        if ("group".equals(subjectType)) {
          if (
              subjectIdentifier.startsWith("*")
              && !subjectIdentifier.endsWith("*")
          ) 
          {
            //relative import
            if (!_isEmpty(importToName)) {
              subjectIdentifier = importToName + sep + subjectIdentifier.substring(1);
            }
            else {
              subjectIdentifier = subjectIdentifier.substring(1);
            }
          } 
          else {
            subjectIdentifier = _getAbsoluteName(subjectIdentifier, stem);
          }
          try {
            privGroup = GroupFinder.findByName(s, subjectIdentifier);
          } 
          catch (Exception e) {
            log.warn("Could not find Stem identified by " + subjectIdentifier);
            continue;
          }

          subject = privGroup.toMember().getSubject();
        } 
        else {
          try {
            if (_isEmpty(subjectId)) {
              subject = _getSubjectByIdentifier(subjectIdentifier, subjectType);
            } 
            else {
              subject = SubjectFinder.findById(subjectId, subjectType);
            }
          } 
          catch (Exception e) {
            String msg = "Could not find subject with ";
            if (_isEmpty(subjectId)) {
              msg = msg + "identifier=" + subjectIdentifier;
            }
            else {
              msg = msg + "id=" + subjectId;
            }
            log.error(msg);
            }
            continue;
          }

        }

        if (
          !XmlExporter.hasImmediatePrivilege( subject, focusStem, privilege)
        ) 
        {
          if (log.isDebugEnabled()) {
            log.debug("Assigning " + privilege + " to " + subject.getName() + " for " + stem);
          }
          focusStem.grantPriv(subject, Privilege.getInstance(privilege));
          if (log.isDebugEnabled()) {
            log.debug("...assigned");
          }
        } 
        else {
          if (log.isDebugEnabled()) {
            log.debug(privilege + " already assigned to " + subject.getName() + " so skipping");
          }
        }
      // } FIXME!
    }
    if (log.isInfoEnabled()) {
      log.info("Finished assigning Naming privs");
    }
    namingPrivLists = null;
  } // private void _processNamingPrivLists()

  // @since   1.0
  private void _processNamingPrivs() 
    throws  Exception 
  {
    String  absoluteGroup;
    String  group;
    Stem    grouperStem;
    Map     map;
    Member  member = null;
    Element naming;
    String  priv;
    Group   privGroup;
    String  stem;
    String  subject;
    Subject subj;

    for (int i = 0; i < namingPrivs.size(); i++) {

      map     = (Map) namingPrivs.get(i);
      naming  = (Element) map.get("naming");
      stem    = (String) map.get("stem");
      group   = naming.getAttribute("group");
      subject = naming.getAttribute("subject");
      priv    = naming.getAttribute("priv").toLowerCase();
      if (!_isEmpty(group)) {
        absoluteGroup = _getAbsoluteName(group, stem);
        privGroup = GroupFinder.findByName(s, absoluteGroup);
        member = MemberFinder.findBySubject(s, SubjectFinder.findById(
            privGroup.getUuid(), "group"));

        System.out.println("Assigning " + priv + " to " + absoluteGroup
            + " for " + stem);
      } 
      else if (!_isEmpty(subject)) {
        try {
          subj = SubjectFinder.findByIdentifier(subject);
        } 
        catch (SubjectNotFoundException e) {
          subj = SubjectFinder.findById(subject);
        }
        member = MemberFinder.findBySubject(s, subj);
        System.out.println(
          "Assigning " + priv + " to " + subj.getName() + " for " + stem
        );
      }

      grouperStem = StemFinder.findByName(s, stem);
      if (
        !XmlExporter.hasImmediatePrivilege(
          member.getSubject(), grouperStem, priv
        )
      ) 
      {
        grouperStem.grantPriv(member.getSubject(), Privilege .getInstance(priv));
        System.out.println("...assigned");
      } 
      else {
        System.out.println("...already assigned - skiping");
      }
    }
    namingPrivs = null;
  } // private void _processNamingPrivs()

  // @since   1.0
  private void _processPath(Element e, String stem) 
    throws  Exception 
  {
    String  extension        = e.getAttribute("extension");
    String  displayExtension = e.getAttribute("displayExtension");
    String  description      = e.getAttribute("description");
    String  newStem          = _joinStem(stem, extension);
    if (log.isInfoEnabled()) {
      log.info("Creating stem " + newStem);
    }
    Stem    existingStem      = null;
    String  updateAttributes  = e.getAttribute("updateAttributes");
    if (_isEmpty(updateAttributes)) {
      updateAttributes = options.getProperty("import.data.update-attributes");
    }
    try {
      existingStem = StemFinder.findByName(s, newStem);
      if (log.isInfoEnabled()) {
        log.info(newStem + " already exists - skipping");
      }
      if ("true".equals(updateAttributes)) {
        if (
          !_isEmpty(displayExtension) 
          && !displayExtension.equals(existingStem.getDisplayExtension())
        ) 
        {
          existingStem.setDisplayExtension(displayExtension);
        }
        if (
          !_isEmpty(description)
          && !description.equals(existingStem.getDescription())
        )
        {
          existingStem.setDescription(description);
        }
      }
    } 
    catch (StemNotFoundException ex) {
    }
    if (existingStem == null) {
      Stem parent = null;
      try {
        parent = StemFinder.findByName(s, stem);
      } 
      catch (StemNotFoundException ex) {
        if (NS_ROOT.equals(stem)) {
          parent = StemFinder.findRootStem(s);
        }
      }
      Stem gs = parent.addChildStem(extension, displayExtension);
      if (log.isInfoEnabled()) {
        log.info(newStem + " added");
      }
      if (description != null && description.length() != 0) {
        gs.setDescription(description);
      }
    }
    _processPrivileges(e, newStem.replaceAll(NS_ROOT + sep, ""), "naming");
    _processNaming(e, newStem.replaceAll(NS_ROOT + sep, ""));
    _process(e, newStem.replaceAll(NS_ROOT + sep, ""));
  } // private void _processPath(e, stem)

  // @since   1.0
  private void _processPrivileges(Element e, String stem, String type)
    throws  Exception 
  {
    Collection  privileges  = _getImmediateElements(e, "privileges");
    Iterator    it          = privileges.iterator();
    Element     privilege;
    String      priv;
    Map         map;
    boolean     isGroup     = "access".equals(type);
    while (it.hasNext()) {
      privilege = (Element) it.next();
      priv      = privilege.getAttribute("type");
      map       = new HashMap();

      map.put(type, priv);
      map.put("privileges", privilege);
      if (isGroup) {
        map.put("group", stem);
        accessPrivLists.add(map);
      } else {
        map.put("stem", stem);
        namingPrivLists.add(map);
      }
    }
  } // private void _processPrivileges(e, stem, type)

  // @since   1.0
  private void _processStem(Element e) 
    throws  Exception 
  {
    String  extension         = e.getAttribute("extension");
    String  displayExtension  = e.getAttribute("displayExtension");
    Element descE             = _getImmediateElement(e, "description");
    String  description       = "";

    if (descE != null) {
      description = _getText(descE);
    }
    String  id                = e.getAttribute("id");
    String  name              = e.getAttribute("name");

    Stem    existingStem      = null;
    String  updateAttributes  = e.getAttribute("updateAttributes");
    if (_isEmpty(updateAttributes)) {
      updateAttributes = options.getProperty("import.data.update-attributes");
    }
    try {
      if (!_isEmpty(id)) {
        existingStem = StemFinder.findByUuid(s, id);
      } 
      else if (!_isEmpty(name)) {
        existingStem = StemFinder.findByName(s, name);
      } 
      else {
        log.error("Stem does not have id or name:" + extension);
        return;
      }

      if ("true".equals(updateAttributes)) {
        if (
          !_isEmpty(displayExtension)
          && !displayExtension.equals(existingStem.getDisplayExtension())
        )
        {
          existingStem.setDisplayExtension(displayExtension);
        }
        if (
          !_isEmpty(description)
          && !description.equals(existingStem.getDescription())
        )
        {
          existingStem.setDescription(description);
        }
      }
    } 
    catch (StemNotFoundException ex) {
      log.error("Cannot find stem identified by id=" + id + " or name=" + name);
      return;
    }

    _processPrivileges(e, existingStem.getName(), "naming");
  } // private void _processStem(e)

  // @since   1.0
  private void _processSubjects(Element e, String stem) 
    throws  Exception 
  {
    Collection  subjects  = _getImmediateElements(e, "subject");
    Iterator    it        = subjects.iterator();
    Element     subject;
    Map         map;
    while (it.hasNext()) {
      subject = (Element) it.next();
      map     = new HashMap();
      map.put("stem", stem);
      map.put("subject", subject);
      memberships.add(map);
    }
  } // private void _processSubjects(e, stem)

} // public class XmlImporter

