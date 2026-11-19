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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <pre>
 * Hardened JAXP factories for parsing XML that comes from outside Grouper (e.g. SOAP
 * responses from a provisioning target).  A default DocumentBuilderFactory.newInstance()
 * resolves DOCTYPE declarations and external entities, so a malicious or compromised
 * endpoint could make Grouper read local files or call internal URLs (XXE).
 *
 * The factory from newDocumentBuilderFactory():
 * - rejects any DOCTYPE (disallow-doctype-decl), which blocks internal and external entities
 *   and entity expansion bombs
 * - turns off external general and parameter entities and external DTD loading, in case
 *   a DOCTYPE ever gets through
 * - turns off XInclude and entity reference expansion
 * - turns on FEATURE_SECURE_PROCESSING (JAXP limits)
 *
 * It fails closed: if the parser on the classpath cannot apply a required setting, a
 * RuntimeException is thrown instead of returning a weaker parser.
 *
 * Only plain JAXP is used so this class is the same in every release.
 * </pre>
 */
public class GrouperXmlSecureFactory {

  /** reject any document that has a DOCTYPE declaration */
  public static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

  /** do not resolve external general entities */
  public static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

  /** do not resolve external parameter entities */
  public static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

  /** do not load an external DTD even when not validating */
  public static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

  /**
   * no instances, static methods only
   */
  private GrouperXmlSecureFactory() {
  }

  /**
   * new namespace-unaware DocumentBuilderFactory hardened against XXE.  Same defaults as
   * DocumentBuilderFactory.newInstance() otherwise, so callers can swap it in directly.
   * @return the factory
   * @throws RuntimeException if a required setting is not supported by the parser
   */
  public static DocumentBuilderFactory newDocumentBuilderFactory() {

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    // required settings: fail closed if the parser does not support one of them
    setFeatureRequired(documentBuilderFactory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
    setFeatureRequired(documentBuilderFactory, FEATURE_DISALLOW_DOCTYPE_DECL, true);
    setFeatureRequired(documentBuilderFactory, FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
    setFeatureRequired(documentBuilderFactory, FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
    setFeatureRequired(documentBuilderFactory, FEATURE_LOAD_EXTERNAL_DTD, false);

    try {
      documentBuilderFactory.setXIncludeAware(false);
    } catch (UnsupportedOperationException uoe) {
      // old parsers without XInclude support cannot process XInclude anyway
    }
    documentBuilderFactory.setExpandEntityReferences(false);

    // extra defense in depth: block external DTD and schema access by protocol.  Older
    // standalone Xerces jars do not know these attributes; DOCTYPE is already rejected above,
    // so that is not a reason to fail
    setAttributeOptional(documentBuilderFactory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
    setAttributeOptional(documentBuilderFactory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

    return documentBuilderFactory;
  }

  /**
   * set a feature that must be honored, else throw
   * @param documentBuilderFactory
   * @param feature
   * @param value
   */
  private static void setFeatureRequired(DocumentBuilderFactory documentBuilderFactory, String feature, boolean value) {
    try {
      documentBuilderFactory.setFeature(feature, value);
    } catch (ParserConfigurationException pce) {
      throw new RuntimeException("XML parser " + documentBuilderFactory.getClass().getName()
          + " does not support required security feature " + feature + "=" + value, pce);
    }
  }

  /**
   * set an attribute if the parser knows it
   * @param documentBuilderFactory
   * @param attribute
   * @param value
   */
  private static void setAttributeOptional(DocumentBuilderFactory documentBuilderFactory, String attribute, String value) {
    try {
      documentBuilderFactory.setAttribute(attribute, value);
    } catch (IllegalArgumentException iae) {
      // not supported by this parser, the required features above still apply
    }
  }

}
