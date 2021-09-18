package edu.internet2.middleware.grouper.authentication;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperTrustedJwt {

  /**
   * string like: 
   * Bearer jwtTrusted_configId_abc123def456
   */
  private String bearerTokenHeader = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperTrustedJwt.class);
  
  /**
   * bearer token pattern
   */
  private static Pattern bearerTokenPattern = Pattern.compile("^Bearer jwtTrusted_([^_]+)_(.*)$");
  
  /**
   * string like:
   * Bearer jwtTrusted_configId_abc123def456
   * @param theBearerTokenHeader
   * @return this for chaining
   */
  public GrouperTrustedJwt assignBearerTokenHeader(String theBearerTokenHeader) {
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
  public Subject decode() {

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
      
      String configId = matcher.group(1);
      debugMap.put("configId", configId);
      
      GrouperTrustedJwtConfig grouperTrustedJwtConfig = GrouperTrustedJwtConfig.retrieveFromConfigOrCache(configId);
      
      if (grouperTrustedJwtConfig == null) {
        throw new RuntimeException("Cant find trusted jwt config: '" + configId + "'");
      }
      
      String jwtString = matcher.group(2);
      
      DecodedJWT decodedJwt = JWT.decode(jwtString);
      debugMap.put("decodeJwt", decodedJwt != null);
          
      Date expiresAt = decodedJwt.getExpiresAt();
          
      if (expiresAt != null) {
        debugMap.put("expiresAt",expiresAt);
        
        if (expiresAt.getTime() > System.currentTimeMillis()) {
          debugMap.put("expiredByExpiresAt", true);
          return null;
        }
      }

      // grouper.jwt.trusted.configId.expirationSeconds
      int expirationSeconds = grouperTrustedJwtConfig.getExpirationSeconds();
          
      if (expirationSeconds > 0) {
        debugMap.put("expirationSeconds",expirationSeconds);
        Date issuedAt = decodedJwt.getIssuedAt();
        debugMap.put("issuedAt",issuedAt);
        if (issuedAt == null || issuedAt.getTime() < System.currentTimeMillis() + expirationSeconds * 1000) {
          debugMap.put("expiredByExpirationSeconds", true);
          return null;
        }
      }

      boolean verified = false;
      for (GrouperTrustedJwtConfigKey grouperTrustedJwtConfigKey : grouperTrustedJwtConfig.getGrouperTrustedJwtConfigKeys()) {
        if (grouperTrustedJwtConfigKey.verify(decodedJwt)) {
          verified = true;
          break;
        }
        
      }

      debugMap.put("verified", verified);

      if (!verified) {
        return null;
      }
      
      final Set<String> subjectSourceIds = grouperTrustedJwtConfig.getSubjectSourceIds();
      
      Claim claim = decodedJwt.getClaim("subjectSourceId");
      if (claim != null && !claim.isNull() && !StringUtils.isBlank(claim.asString())) {
        subjectSourceIds.clear();
        subjectSourceIds.add(claim.asString());
      }
      
      debugMap.put("subjectSourceIds", subjectSourceIds);
      
      Subject subject = (Subject) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Subject subject = null;
          {
            String subjectIdClaimName = StringUtils.isBlank(grouperTrustedJwtConfig.getSubjectIdType()) ? "subjectId" : null;
            if (StringUtils.equals(grouperTrustedJwtConfig.getSubjectIdType(), "subjectId") && !StringUtils.isBlank(grouperTrustedJwtConfig.getSubjectIdClaimName())) {
              
              subjectIdClaimName = grouperTrustedJwtConfig.getSubjectIdClaimName();
              
            }
      
            if (!StringUtils.isBlank(subjectIdClaimName)) {
             
              debugMap.put("subjectIdClaimName", subjectIdClaimName);
              
              Claim claim = decodedJwt.getClaim(subjectIdClaimName);
              if (claim != null && !claim.isNull() && !StringUtils.isBlank(claim.asString())) {
                String subjectId = claim.asString();
                debugMap.put("subjectId", subjectId);
                
                if (GrouperUtil.nonNull(subjectSourceIds).size() > 0) {
                  subject = SubjectFinder.findByIdOrIdentifierOrBothAndSourceIds("subjectId", subjectId, subjectSourceIds, false);
                } else {
                  subject = SubjectFinder.findById(subjectId, false);
                }
                
              }
              
            }
          }

          {
            String subjectIdentifierClaimName = StringUtils.isBlank(grouperTrustedJwtConfig.getSubjectIdType()) ? "subjectIdentifier" : null;
            if (StringUtils.equals(grouperTrustedJwtConfig.getSubjectIdType(), "subjectIdentifier") && !StringUtils.isBlank(grouperTrustedJwtConfig.getSubjectIdClaimName())) {
              
              subjectIdentifierClaimName = grouperTrustedJwtConfig.getSubjectIdClaimName();
              
            }
      
            if (!StringUtils.isBlank(subjectIdentifierClaimName)) {
             
              debugMap.put("subjectIdentifierClaimName", subjectIdentifierClaimName);
              
              Claim claim = decodedJwt.getClaim(subjectIdentifierClaimName);
              if (claim != null && !claim.isNull() && !StringUtils.isBlank(claim.asString())) {
                String subjectIdentifier = claim.asString();
                debugMap.put("subjectIdentifier", subjectIdentifier);
                if (GrouperUtil.nonNull(subjectSourceIds).size() > 0) {
                  subject = SubjectFinder.findByIdOrIdentifierOrBothAndSourceIds("subjectIdentifier", subjectIdentifier, subjectSourceIds, false);
                } else {
                  subject = SubjectFinder.findByIdentifier(subjectIdentifier, false);
                }
              }
              
            }
          }

          {
            String subjectIdOrIdentifierClaimName = StringUtils.isBlank(grouperTrustedJwtConfig.getSubjectIdType()) ? "subjectIdOrIdentifier" : null;
            if (StringUtils.equals(grouperTrustedJwtConfig.getSubjectIdType(), "subjectIdOrIdentifier") && !StringUtils.isBlank(grouperTrustedJwtConfig.getSubjectIdClaimName())) {
              
              subjectIdOrIdentifierClaimName = grouperTrustedJwtConfig.getSubjectIdClaimName();
              
            }
      
            if (!StringUtils.isBlank(subjectIdOrIdentifierClaimName)) {
             
              debugMap.put("subjectIdOrIdentifierClaimName", subjectIdOrIdentifierClaimName);
              
              Claim claim = decodedJwt.getClaim(subjectIdOrIdentifierClaimName);
              if (claim != null && !claim.isNull() && !StringUtils.isBlank(claim.asString())) {
                String subjectIdentifier = claim.asString();
                debugMap.put("subjectIdOrIdentifier", subjectIdentifier);
                if (GrouperUtil.nonNull(subjectSourceIds).size() > 0) {
                  subject = SubjectFinder.findByIdOrIdentifierOrBothAndSourceIds("subjectIdOrIdentifier", subjectIdentifier, subjectSourceIds, false);
                } else {
                  subject = SubjectFinder.findByIdOrIdentifier(subjectIdentifier, false);
                }
              }
              
            }
          }
          
          return subject;
        }
      });

      debugMap.put("subjectFound", subject != null);
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
