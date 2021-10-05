package edu.internet2.middleware.grouper.authentication;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;
import edu.internet2.middleware.subject.Subject;

public class GrouperPublicPrivateKeyJwt {
  
  /**
   * string like: 
   * Bearer jwtTrusted_configId_abc123def456
   */
  private String bearerTokenHeader = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPublicPrivateKeyJwt.class);
  
  /**
   * bearer token pattern
   */
  private static Pattern bearerTokenPattern = Pattern.compile("^Bearer jwtUser_([^_]+)_(.*)$");
  
  /**
   * string like:
   * Bearer jwtTrusted_configId_abc123def456
   * @param theBearerTokenHeader
   * @return this for chaining
   */
  public GrouperPublicPrivateKeyJwt assignBearerTokenHeader(String theBearerTokenHeader) {
    this.bearerTokenHeader = theBearerTokenHeader;
    return this;
  }
  
  /**
   * result of decoding jwt
   */
  private GrouperTrustedJwtResult grouperTrustedJwtResult = null;
  
  
  
  /**
   * result of decoding jwt
   * @return result
   */
  public GrouperTrustedJwtResult getGrouperTrustedJwtResult() {
    return this.grouperTrustedJwtResult;
  }



  /**
   * result of decoding jwt
   * @param grouperTrustedJwtResult1
   */
  public void setGrouperTrustedJwtResult(GrouperTrustedJwtResult grouperTrustedJwtResult1) {
    this.grouperTrustedJwtResult = grouperTrustedJwtResult1;
  }

  /**
   * 
   * @return the subject
   */
  public Subject decode(String requesterIpAddress) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startNanos = System.nanoTime();

    if (StringUtils.isBlank(this.bearerTokenHeader)) {
      this.grouperTrustedJwtResult = GrouperTrustedJwtResult.ERROR_MISSING_TOKEN;
      throw new RuntimeException("bearerTokenHeader is required");
    }
    
    try {
      
      debugMap.put("bearerTokenHeader", StringUtils.abbreviate(this.bearerTokenHeader, 50));
      
      Matcher matcher = bearerTokenPattern.matcher(this.bearerTokenHeader);
      
      if (!matcher.matches()) {
        this.grouperTrustedJwtResult = GrouperTrustedJwtResult.ERROR_TOKEN_INVALID;
        throw new RuntimeException("bearerTokenHeader is invalid!");
      }
      
      String base64OfMemberId = matcher.group(1);
      
      String memberId = new String(new Base64().decode(base64OfMemberId.getBytes("UTF-8")));
      
      GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(memberId,
          GrouperPassword.Application.WS.name());
      
      Subject subject = (Subject)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return MemberFinder.findByUuid(grouperSession, memberId, true).getSubject();
        }
      });
      
      if (grouperPassword == null) {
        throw new RuntimeException("Can't find public key for member id '"+memberId+"'");
      }
      
      if (grouperPassword.getExpiresMillis() != null && grouperPassword.getExpiresMillis() < System.currentTimeMillis()) {
        debugMap.put("expiredByExpiresAt", true);
        return null;
      }
      
      if (StringUtils.isNotBlank(grouperPassword.getAllowedFromCidrs())) {
        boolean isIpAllowed = GrouperUtil.ipOnNetworks(requesterIpAddress, grouperPassword.getAllowedFromCidrs());
        if (!isIpAllowed) {
          debugMap.put("isIpAllowed", false);
          return null;
        }
      } 
      
      String jwtString = matcher.group(2);
      
      DecodedJWT decodedJwt = JWT.decode(jwtString);
      debugMap.put("decodeJwt", decodedJwt != null);
          
      Date expiresAt = decodedJwt.getExpiresAt();
          
      if (expiresAt != null) {
        debugMap.put("expiresAt", expiresAt);
        
        if (expiresAt.getTime() < System.currentTimeMillis()) {
          debugMap.put("expiredByExpiresAt", true);
          return null;
        }
      }
      
      int maxValidSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.selfService.jwt.maxValidTimeInSeconds", 600);
      
      Date issuedAt = decodedJwt.getIssuedAt();
      debugMap.put("issuedAt", issuedAt);
      if (issuedAt == null || issuedAt.getTime() + maxValidSeconds * 1000 < System.currentTimeMillis() ) {
        debugMap.put("expiredByMaxValidTimeInSeconds", true);
        return null;
      }

      boolean verified = grouperPassword.verify(decodedJwt);

      debugMap.put("verified", verified);

      if (!verified) {
        return null;
      }
      
      return subject;
    } catch (Exception e) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      
      debugMap.put("tookMs", (System.nanoTime() - startNanos) / 1000000);
      if (debugMap.get("exception") != null) {
        LOG.error(GrouperUtil.mapToString(debugMap));
      } else if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

}
