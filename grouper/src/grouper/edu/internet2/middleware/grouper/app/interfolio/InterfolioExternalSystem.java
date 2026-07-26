package edu.internet2.middleware.grouper.app.interfolio;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * External system for the Interfolio faculty information system.
 *
 * Interfolio exposes a few distinct API surfaces, all authenticated with the same HMAC-SHA1 "INTF"
 * scheme but living on different hosts:
 *
 *   - IAM API (iamUrl, e.g. https://iam-api.interfolio.com) - user identity: create / update users.
 *   - byc/core API (bycUrl, e.g. https://logic.interfolio.com) - search users and grant/remove access
 *     to the products RPT (byc-tenure) and FS (byc-search) via subscribe/unsubscribe.
 *
 * (Interfolio also has a Faculty180/FAR API on faculty180.interfolio.com, but it is a separate auth
 * realm that our credentials are not authorized for, so it is intentionally not modeled here.)
 *
 * The HMAC signature string is: VERB + "\n\n\n" + timestamp + "\n" + requestString, HMAC-SHA1'd with
 * the private key, base64-encoded, and sent as "Authorization: INTF {publicKey}:{signature}" along
 * with a "TimeStamp" header.  The requestString is everything after the host (path + query), and the
 * query string - if any - must be signed verbatim.
 *
 * Config (in grouper-loader.properties), prefix grouper.interfolio.{configId}. :
 *   publicKey   - the INTF public key
 *   privateKey  - the INTF private key (secret)
 *   databaseId  - the Interfolio tenant id (used in the URL path)
 *   bycUrl      - base url for the byc/core API (search, subscribe, unsubscribe)
 *   iamUrl      - base url for the IAM API (create, update)
 */
public class InterfolioExternalSystem extends GrouperExternalSystem {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.interfolio." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.interfolio)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myInterfolio";
  }

  /**
   * Validate the configuration: the required properties are present, and (if they are) a lightweight
   * search call authenticates against Interfolio.
   */
  @Override
  public List<String> test() throws UnsupportedOperationException {

    List<String> ret = new ArrayList<String>();

    String configPrefix = "grouper.interfolio." + this.getConfigId() + ".";
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();

    for (String suffix : new String[] {"publicKey", "privateKey", "databaseId", "bycUrl", "iamUrl"}) {
      String property = configPrefix + suffix;
      if (GrouperUtil.isBlank(config.propertyValueString(property))) {
        ret.add("Undefined or blank property: " + property);
      }
    }

    if (ret.size() > 0) {
      return ret;
    }

    try {
      // a search with no term returns the institution roster (paged); page 1, small limit is enough
      // to prove the credentials authenticate
      GrouperInterfolioApiCommands.searchUsers(this.getConfigId(), null, 1, 1);
    } catch (Exception e) {
      ret.add(logAndDescribeTestException("Unable to connect to Interfolio", e));
    }

    return ret;
  }

  /**
   * Read a config value for this external system's configId.
   * @param configId external system config id
   * @param suffix the property suffix after grouper.interfolio.{configId}.
   * @return the value (required - throws if missing)
   */
  public static String retrieveConfigValue(String configId, String suffix) {
    return GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired("grouper.interfolio." + configId + "." + suffix);
  }

  /**
   * Attach the Interfolio HMAC authorization headers to an http client.
   *
   * Signs VERB + "\n\n\n" + timestamp + "\n" + requestString with HMAC-SHA1 using the private key,
   * base64-encodes it, and sets:
   *   TimeStamp: {timestamp}
   *   Authorization: INTF {publicKey}:{signature}
   *
   * IMPORTANT: requestString must be exactly the path (plus query string, if any) that follows the
   * host in the URL, and must match what is actually sent on the wire, or the signature will fail.
   *
   * @param configId external system config id (to look up the public/private key)
   * @param grouperHttpClient the http client to add headers to
   * @param requestVerb HTTP verb (GET, POST, PUT, ...)
   * @param requestString the path + query that follows the host, signed verbatim
   */
  public static void attachInterfolioHmacHeaders(String configId, GrouperHttpClient grouperHttpClient,
      String requestVerb, String requestString) {

    try {

      String publicKey = retrieveConfigValue(configId, "publicKey");
      String privateKey = retrieveConfigValue(configId, "privateKey");

      // timestamp in UTC, format yyyy-MM-dd HH:mm:ss
      String timestampString = ZonedDateTime.now(ZoneOffset.UTC)
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

      String verbRequestString = requestVerb + "\n\n\n" + timestampString + "\n" + requestString;

      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
      byte[] digest = mac.doFinal(verbRequestString.getBytes(StandardCharsets.UTF_8));
      String signedHash = Base64.getEncoder().encodeToString(digest);

      grouperHttpClient.addHeader("TimeStamp", timestampString);
      grouperHttpClient.addHeader("Authorization", "INTF " + publicKey + ":" + signedHash);

    } catch (Exception e) {
      throw new RuntimeException("Error building Interfolio HMAC headers", e);
    }
  }

}
