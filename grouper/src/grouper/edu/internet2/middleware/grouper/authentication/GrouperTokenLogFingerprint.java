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
package edu.internet2.middleware.grouper.authentication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * <pre>
 * Log-safe fingerprint of a bearer token (access token, refresh token, code), so logs can tell
 * tokens apart without holding anything that can be replayed.
 *
 * Output: "sha256:" + the first 12 hex chars of the SHA-256 of the token + " len=" + token length,
 * e.g. sha256:1a2b3c4d5e6f len=812.  Null gives "null", blank gives "blank".
 *
 * Why not a prefix: a literal prefix is part of the credential, and the first 10 chars of nearly
 * every JWT are the same (eyJhbGciOi, the base64 of {"alg":"), so a prefix cannot tell tokens
 * apart either.  48 bits of a one-way hash is enough to correlate log lines and gives nothing to
 * replay.
 *
 * Plain JDK only, so the same file works in every Grouper release.
 * </pre>
 */
public class GrouperTokenLogFingerprint {

  /** hex digits */
  private static final char[] HEX = "0123456789abcdef".toCharArray();

  /** how many hex chars of the digest to keep */
  private static final int HEX_CHARS = 12;

  /**
   * no instances
   */
  private GrouperTokenLogFingerprint() {
  }

  /**
   * @param token bearer token, may be null
   * @return a fingerprint that is safe to log, never the token or any part of it
   */
  public static String fingerprint(String token) {
    if (token == null) {
      return "null";
    }
    if (token.trim().length() == 0) {
      return "blank";
    }
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder("sha256:");
      for (int i = 0; i < HEX_CHARS / 2; i++) {
        result.append(HEX[(digest[i] >> 4) & 0xf]).append(HEX[digest[i] & 0xf]);
      }
      result.append(" len=").append(token.length());
      return result.toString();
    } catch (Exception e) {
      // SHA-256 is always there, but never fall back to logging the token
      return "unavailable len=" + token.length();
    }
  }

}
