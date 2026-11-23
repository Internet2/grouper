/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigSecretMasking;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperMcpConfigSearchIndex;
import junit.textui.TestRunner;

/**
 * A ws bearer token additional header
 * value whose sibling valueIsSecret is true is masked by both admin_config_search modes, and is
 * not put in the Lucene index as searchable content.
 *
 * <p>The config id (headerLeakProof) has no pass, secret or private in it, so only the sibling
 * rule can catch it.  Before the fix the value comes back in clear, the secret text finds the
 * property, and GrouperConfigSecretMasking does not exist, so this does not compile.</p>
 */
public class GrouperMcpConfigSearchSecretMaskTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the secret header value */
  private static final String SECRET = "mcpheaderleakproof7b19";

  /** the visible header value */
  private static final String PLAIN = "mcpheaderplainvalue4c27";

  /** prefix of the config */
  private static final String PREFIX = "grouper.wsBearerToken.headerLeakProof.";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpConfigSearchSecretMaskTest("testRegexSearchMasksSiblingSecret"));
  }

  /**
   * @param name
   */
  public GrouperMcpConfigSearchSecretMaskTest(String name) {
    super(name);
  }

  /**
   * header 0 and header 4 are secret, header 1 is not
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    Map<String, String> override = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    override.put(PREFIX + "numberOfAdditionalHeaders", "5");
    override.put(PREFIX + "additionalHeader.0.name", "X-Proof-Key");
    override.put(PREFIX + "additionalHeader.0.valueIsSecret", "true");
    override.put(PREFIX + "additionalHeader.0.value", SECRET);
    override.put(PREFIX + "additionalHeader.1.name", "anthropic-version");
    override.put(PREFIX + "additionalHeader.1.valueIsSecret", "false");
    override.put(PREFIX + "additionalHeader.1.value", PLAIN);
    override.put(PREFIX + "additionalHeader.4.name", "X-Proof-Key-4");
    override.put(PREFIX + "additionalHeader.4.valueIsSecret", "true");
    override.put(PREFIX + "additionalHeader.4.value", SECRET + "four");

    GrouperMcpConfigSearchIndex.forceRebuild();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    Map<String, String> override = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    for (String key : new java.util.ArrayList<String>(override.keySet())) {
      if (key.startsWith(PREFIX)) {
        override.remove(key);
      }
    }
    GrouperMcpConfigSearchIndex.forceRebuild();
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * @param searchType regex or lucene
   * @param search the regex or query
   * @return the result text
   */
  private static String search(String searchType, String search) {
    GrouperSession.startRootSession();
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("searchType", searchType);
    arguments.put("configFile", "grouper-loader.properties");
    arguments.put("searchRegex", search);
    ObjectNode result = GrouperMcpAdminSearchConfigs.execute(arguments, authUser);
    assertFalse(result.toString(), result.get("isError").asBoolean());
    return result.get("content").get(0).get("text").asText();
  }

  /**
   * regex search returns the masked value, marked sensitive, for header 0 and header 4
   */
  public void testRegexSearchMasksSiblingSecret() {
    String text = search("regex", "^grouper\\.wsBearerToken\\.headerLeakProof\\.additionalHeader\\.[0-9]\\.value$");
    assertFalse(text, text.contains(SECRET));
    assertTrue(text, text.contains(GrouperConfigHibernate.ESCAPED_PASSWORD));
    assertTrue(text, text.contains("\"sensitive\""));
    // the header that is not secret stays visible
    assertTrue(text, text.contains(PLAIN));
  }

  /**
   * the secret is not searchable content: searching for it finds nothing
   */
  public void testLuceneSearchForSecretFindsNothing() {
    String text = search("lucene", SECRET);
    assertFalse(text, text.contains("headerLeakProof"));
    assertFalse(text, text.contains(SECRET));
  }

  /**
   * a Lucene search by property name finds the property, masked
   */
  public void testLuceneSearchByNameMasked() {
    String text = search("lucene", "headerLeakProof");
    assertTrue(text, text.contains("headerLeakProof"));
    assertFalse(text, text.contains(SECRET));
  }

  /**
   * a value that is not secret is still searchable by its content
   */
  public void testLuceneSearchForPlainValueFindsIt() {
    String text = search("lucene", PLAIN);
    assertTrue(text, text.contains(PLAIN));
  }

  /**
   * flipping valueIsSecret from false to true and rebuilding drops the old plaintext from the index
   */
  public void testFlipToSecretDropsFromIndex() {
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "additionalHeader.1.valueIsSecret", "true");
    GrouperMcpConfigSearchIndex.forceRebuild();
    String text = search("lucene", PLAIN);
    assertFalse(text, text.contains(PLAIN));
  }

  /**
   * the sibling rule itself
   */
  public void testSiblingRule() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    assertTrue(GrouperConfigSecretMasking.siblingValueIsSecret(config, PREFIX + "additionalHeader.0.value"));
    assertTrue(GrouperConfigSecretMasking.siblingValueIsSecret(config, PREFIX + "additionalHeader.0.value.elConfig"));
    assertFalse(GrouperConfigSecretMasking.siblingValueIsSecret(config, PREFIX + "additionalHeader.1.value"));
    // no flag at all
    assertFalse(GrouperConfigSecretMasking.siblingValueIsSecret(config, PREFIX + "additionalHeader.2.value"));
    // not a .value key
    assertFalse(GrouperConfigSecretMasking.siblingValueIsSecret(config, PREFIX + "additionalHeader.0.name"));
    // a flag that is not a boolean counts as secret
    config.propertiesOverrideMap().put(PREFIX + "additionalHeader.3.valueIsSecret", "maybe");
    assertTrue(GrouperConfigSecretMasking.siblingValueIsSecret(config, PREFIX + "additionalHeader.3.value"));
    assertTrue(GrouperConfigSecretMasking.isSensitive(null, null, config, PREFIX + "additionalHeader.0.value", SECRET));
    assertFalse(GrouperConfigSecretMasking.isSensitive(null, null, config, PREFIX + "additionalHeader.1.value", PLAIN));
  }
}
