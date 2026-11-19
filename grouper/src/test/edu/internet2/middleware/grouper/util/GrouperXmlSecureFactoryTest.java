/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * <pre>
 * GrouperXmlSecureFactory (used by BplogixProvisionerDao for every SOAP response) must reject
 * documents with a DOCTYPE / external entity (XXE) and still parse the normal BPLogix
 * responses.  Tests the factory directly, no database or BPLogix server needed.
 *
 * Before the fix this class does not compile (GrouperXmlSecureFactory does not exist) and the
 * DAO used DocumentBuilderFactory.newInstance(), which expands the file entity below.
 * </pre>
 */
public class GrouperXmlSecureFactoryTest extends TestCase {

  /** marker written to a local file that an XXE payload tries to read */
  private static final String MARKER = "GROUPER_BPLG_XXE_MARKER_144";

  /**
   * run from the IDE
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperXmlSecureFactoryTest.class);
  }

  /**
   * @param name
   */
  public GrouperXmlSecureFactoryTest(String name) {
    super(name);
  }

  /** local file holding the marker */
  private File markerFile;

  /**
   * write the marker file
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.markerFile = File.createTempFile("grouperXxe144_", ".txt");
    this.markerFile.deleteOnExit();
    OutputStream outputStream = new FileOutputStream(this.markerFile);
    try {
      outputStream.write(MARKER.getBytes("UTF-8"));
    } finally {
      outputStream.close();
    }
  }

  /**
   * delete the marker file
   */
  @Override
  protected void tearDown() throws Exception {
    if (this.markerFile != null) {
      this.markerFile.delete();
    }
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * parse with the hardened factory
   * @param xml
   * @return the document
   * @throws Exception if the parser rejects it
   */
  private static Document parseSecure(String xml) throws Exception {
    DocumentBuilderFactory dbf = GrouperXmlSecureFactory.newDocumentBuilderFactory();
    DocumentBuilder db = dbf.newDocumentBuilder();
    return db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
  }

  /**
   * the hardened parser must throw on this document, and the marker must not leak
   * into the exception message
   * @param label
   * @param xml
   */
  private static void assertRejected(String label, String xml) {
    Document document = null;
    try {
      document = parseSecure(xml);
    } catch (Exception e) {
      String message = String.valueOf(e.getMessage());
      assertFalse(label + ": marker must not appear in the error: " + message, message.contains(MARKER));
      return;
    }
    String text = document.getDocumentElement() == null ? null : document.getDocumentElement().getTextContent();
    fail(label + ": hardened parser should reject a DOCTYPE, but parsed it, text: " + text);
  }

  /**
   * file URL of the marker file
   * @return the url
   */
  private String markerUrl() {
    return this.markerFile.toURI().toString();
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * the reporter's payload: the AuthenticateJSON response is a string element whose text
   * becomes the auth token sent back to the endpoint
   */
  public void testExternalFileGeneralEntityRejected() {
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE string [\n"
        + "  <!ENTITY xxe SYSTEM \"" + markerUrl() + "\">\n"
        + "]>\n"
        + "<string>&xxe;</string>";
    assertRejected("external file entity", xml);
  }

  /**
   * external http entity (SSRF); the port is closed, the parser must not get that far
   */
  public void testExternalHttpGeneralEntityRejected() {
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE string [\n"
        + "  <!ENTITY xxe SYSTEM \"http://127.0.0.1:1/grouperXxe144\">\n"
        + "]>\n"
        + "<string>&xxe;</string>";
    assertRejected("external http entity", xml);
  }

  /**
   * external parameter entity / external DTD (blind exfiltration)
   */
  public void testExternalParameterEntityAndDtdRejected() {
    String xmlParam = "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE string [\n"
        + "  <!ENTITY % remote SYSTEM \"" + markerUrl() + "\">\n"
        + "  %remote;\n"
        + "]>\n"
        + "<string>x</string>";
    assertRejected("external parameter entity", xmlParam);

    String xmlDtd = "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE string SYSTEM \"http://127.0.0.1:1/grouperXxe144.dtd\">\n"
        + "<string>x</string>";
    assertRejected("external DTD", xmlDtd);
  }

  /**
   * internal entity expansion (a small "billion laughs")
   */
  public void testInternalEntityExpansionRejected() {
    String xml = "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE string [\n"
        + "  <!ENTITY a \"aaaaaaaaaa\">\n"
        + "  <!ENTITY b \"&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;\">\n"
        + "  <!ENTITY c \"&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;\">\n"
        + "]>\n"
        + "<string>&c;</string>";
    assertRejected("internal entity expansion", xml);
  }

  /**
   * compatibility control: a normal BPLogix AuthenticateJSON response and a normal SOAP
   * response still parse, and the same XPath the DAO uses still finds the values
   * @throws Exception
   */
  public void testNormalResponsesParse() throws Exception {

    XPath xPath = XPathFactory.newInstance().newXPath();

    // AuthenticateJSON (retrieveWsAuthnToken uses XPath /string)
    Document authDocument = parseSecure("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<string xmlns=\"http://www.bplogix.com/WebServices/\">abc123token</string>");
    assertEquals("abc123token", xPath.compile("/string").evaluate(authDocument));

    // SOAP response like AddUserToGroup, read with the DAO's own XPath
    String soapXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
        + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
        + " xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
        + "  <soap:Body>\n"
        + "    <AddUserToGroupResponse xmlns=\"http://www.bplogix.com/WebServices/\">\n"
        + "      <AddUserToGroupResult>true</AddUserToGroupResult>\n"
        + "    </AddUserToGroupResponse>\n"
        + "  </soap:Body>\n"
        + "</soap:Envelope>";
    String daoXpath = "/Envelope/Body/AddUserToGroupResponse/AddUserToGroupResult";
    Document soapDocument = parseSecure(soapXml);
    String secureResult = (String) xPath.evaluate(daoXpath, soapDocument, XPathConstants.STRING);

    // same document through the default factory the DAO used before: same answer
    Document defaultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
        new ByteArrayInputStream(soapXml.getBytes("UTF-8")));
    String defaultResult = (String) xPath.evaluate(daoXpath, defaultDocument, XPathConstants.STRING);
    assertEquals("hardened and default parsers should give the DAO the same value", defaultResult, secureResult);
    assertEquals("true", secureResult);

    // node list, like GetUserTasksByUID
    NodeList nodeList = (NodeList) xPath.compile("/Envelope/Body/AddUserToGroupResponse/*")
        .evaluate(soapDocument, XPathConstants.NODESET);
    assertEquals(1, nodeList.getLength());

    // character references and the predefined entities are not DOCTYPE entities: still fine
    Document escapedDocument = parseSecure("<string>a&amp;b&lt;c&#65;</string>");
    assertEquals("a&b<cA", escapedDocument.getDocumentElement().getTextContent());
  }

  /**
   * the factory keeps the JAXP defaults the DAO relied on (namespace-unaware, not validating)
   */
  public void testFactorySettings() {
    DocumentBuilderFactory dbf = GrouperXmlSecureFactory.newDocumentBuilderFactory();
    assertFalse(dbf.isNamespaceAware());
    assertFalse(dbf.isValidating());
    assertFalse(dbf.isXIncludeAware());
    assertFalse(dbf.isExpandEntityReferences());
  }

}
