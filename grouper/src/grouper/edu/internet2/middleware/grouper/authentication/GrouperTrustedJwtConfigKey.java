package edu.internet2.middleware.grouper.authentication;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * key in a trusted jwt
 * @author mchyzer
 *
 */
public class GrouperTrustedJwtConfigKey implements RSAKeyProvider {

  /**
   * private key and encryption type to PrivateKey
   */
  private static ExpirableCache<MultiKey, PublicKey> publicKeyCache = new ExpirableCache<MultiKey, PublicKey>(10);
  
  /**
   * get the public key and cache stuff
   * @param publicKeyString
   * @param encryptionType
   * @return public key
   */
  private static PublicKey retrievePublicKey(String publicKeyString, String encryptionType) {

    MultiKey multiKey = new MultiKey(publicKeyString, encryptionType);
    
    PublicKey publicKey = publicKeyCache.get(multiKey);

    if (publicKey == null) {

      try {
        byte[] publicKeyBytes = Base64.decodeBase64(publicKeyString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = kf.generatePublic(publicKeySpec);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(
            "Could not reconstruct the public key, the given algorithm could not be found.", e);
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException("Could not reconstruct the public key", e);
      }
      publicKeyCache.put(multiKey, publicKey);
    }
    return publicKey;
  }
  
  /**
   * lazy load the algorithm
   */
  private Algorithm algorithm = null;
  
  /**
   * get the public key and cache stuff
   * @param publicKeyString
   * @param encryptionType
   * @return public key
   */
  private Algorithm retrieveAlgorithm() {

    if (algorithm == null) {

      if (StringUtils.equals(encryptionType, "RS-256")) {
        algorithm = Algorithm.RSA256(this);
      } else if (StringUtils.equals(encryptionType, "RS-384")) {
        algorithm = Algorithm.RSA384(this);
      } else if (StringUtils.equals(encryptionType, "RS-512")) {
        algorithm = Algorithm.RSA512(this);
      } else {
        throw new RuntimeException("Invalid encryption type: '"+encryptionType+"'");
      }

    }
    return algorithm;
  }
  
  /**
   * encrypted public key of trusted authority
   * grouper.jwt.trusted.configId.key.0.publicKey = abc123
   */
  private String publicKey = null;
  
  /**
   * grouper.jwt.trusted.configId.key.0.encryptionType = RS-256
   */
  private String encryptionType = null;

  /**
   * optional: yyyy-mm-dd hh:mm:ss.SSS
   * grouper.jwt.trusted.configId.key.0.expiresOn = 2021-11-01 00:00:00.000
   */
  private Date expiresOn = null;

  /**
   * see if this key is expired
   * @return
   */
  public boolean isExpired() {
    return this.expiresOn != null && this.expiresOn.getTime() < System.currentTimeMillis();
  }
  
  /**
   * encrypted public key of trusted authority
   * grouper.jwt.trusted.configId.key.0.publicKey = abc123
   * @return public key
   */
  public String getPublicKey() {
    return this.publicKey;
  }

  /**
   * encrypted public key of trusted authority
   * grouper.jwt.trusted.configId.key.0.publicKey = abc123
   * @param publicKey1
   */
  public void setPublicKey(String publicKey1) {
    this.publicKey = publicKey1;
  }

  /**
   * grouper.jwt.trusted.configId.key.0.encryptionType = RS-256
   * @return encryption type
   */
  public String getEncryptionType() {
    return encryptionType;
  }

  /**
   * grouper.jwt.trusted.configId.key.0.encryptionType = RS-256
   * @param encryptionType
   */
  public void setEncryptionType(String encryptionType) {
    this.encryptionType = encryptionType;
  }

  /**
   * optional: yyyy-mm-dd hh:mm:ss.SSS
   * grouper.jwt.trusted.configId.key.0.expiresOn = 2021-11-01 00:00:00.000
   * @return
   */
  public Date getExpiresOn() {
    return expiresOn;
  }

  /**
   * optional: yyyy-mm-dd hh:mm:ss.SSS
   * grouper.jwt.trusted.configId.key.0.expiresOn = 2021-11-01 00:00:00.000
   * @param expiresOn
   */
  public void setExpiresOn(Date expiresOn) {
    this.expiresOn = expiresOn;
  }

  /**
   * 
   * @param decodedJwt
   * @return if this jwt is verified
   */
  public boolean verify(DecodedJWT decodedJwt) {
    
    if (this.isExpired()) {
      return false;
    }
    try {
      this.retrieveAlgorithm().verify(decodedJwt);
      return true;
    } catch (SignatureVerificationException e) {
      // not valid
    }
    return false;
  }

  @Override
  public RSAPublicKey getPublicKeyById(String keyId) {
    PublicKey publicKey = retrievePublicKey(this.publicKey, this.encryptionType);
    if (publicKey instanceof RSAPublicKey) {
      return (RSAPublicKey)publicKey;
    }
    return null;
  }

  @Override
  public RSAPrivateKey getPrivateKey() {
    throw new RuntimeException("Doesnt do private keys");
  }

  @Override
  public String getPrivateKeyId() {
    throw new RuntimeException("Doesnt do private keys");
  }
  
  
  
}
