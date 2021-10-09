package edu.internet2.middleware.grouper.authentication;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import org.junit.Assert;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

public class GrouperPublicPrivateKeyJwtTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperPublicPrivateKeyJwtTest("testDecode"));    
  }
  
  public GrouperPublicPrivateKeyJwtTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperPublicPrivateKeyJwtTest(String name) {
    super(name);
  }
  
  
  class TestRSAKeyProvider implements RSAKeyProvider {

    
   public String privateKeyString;
   public String publicKeyString;
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      PublicKey publicKey = null;
      try {
        byte[] publicKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(publicKeyString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = kf.generatePublic(publicKeySpec);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(
            "Could not reconstruct the public key, the given algorithm could not be found.", e);
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException("Could not reconstruct the public key", e);
      }
      
      if (publicKey instanceof RSAPublicKey) {
        return (RSAPublicKey)publicKey;
      }
      return null;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
      PrivateKey privateKey = null;
      try {
        byte[] privateKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        //EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(publicKeyBytes);
       
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        
        privateKey = kf.generatePrivate(privateKeySpec);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(
            "Could not reconstruct the private key, the given algorithm could not be found.", e);
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException("Could not reconstruct the private key", e);
      }
      
      if (privateKey instanceof RSAPrivateKey) {
        return (RSAPrivateKey)privateKey;
      }
      return null;
    }

    @Override
    public String getPrivateKeyId() {
      return privateKeyString;
    }
    
  }
  
  public void testDecode() throws UnsupportedEncodingException, IllegalStateException {
    
    GrouperSession grouperSession = SessionHelper.getRootSession();
    
    String[] publicPrivateKey = GrouperUtil.generateRsaKeypair(2048);
    
    new GrouperPasswordSave()
      .assignAllowedFromCidrs("0.0.0.0/0")
      .assignApplication(GrouperPassword.Application.WS)
      .assignEncryptionType(GrouperPassword.EncryptionType.RS_2048)
      .assignEntityType("localEntity")
      .assignExpiresAt(System.currentTimeMillis() +365 * 60 * 60 * 1000)
      .assignMemberIdWhoSetPassword(grouperSession.getMember().getId())
      .assignPublicKey(publicPrivateKey[0])
      .assignUsername(grouperSession.getMember().getId())
      .assignMemberId(grouperSession.getMember().getId())
      .save();
    
    String base64EncodedMemberId = new String(new Base64().encode(grouperSession.getMember().getId().getBytes("UTF-8")));
    
    TestRSAKeyProvider testRSAKeyProvider = new TestRSAKeyProvider();
    testRSAKeyProvider.publicKeyString = publicPrivateKey[0];
    testRSAKeyProvider.privateKeyString = publicPrivateKey[1];
    Algorithm algorithm = Algorithm.RSA256(testRSAKeyProvider);
    
    String jwt = JWT.create()
        .withIssuedAt(new Date())
        .sign(algorithm);
    
    GrouperPublicPrivateKeyJwt grouperPublicPrivateKeyJwt = new GrouperPublicPrivateKeyJwt().assignBearerTokenHeader("Bearer jwtUser_"+base64EncodedMemberId+"_"+jwt);
    
    // when
    Subject subject = grouperPublicPrivateKeyJwt.decode("127.0.0.1");
    
    // then
    Assert.assertTrue(subject != null);
    Assert.assertEquals("GrouperSysAdmin", subject.getName());
    
    // issue JWT in the past
    Date lastYear = new Date(System.currentTimeMillis() - 366 * 60 * 60 * 1000);
    
    jwt = JWT.create()
        .withIssuedAt(lastYear)
        .sign(algorithm);
    
    grouperPublicPrivateKeyJwt = new GrouperPublicPrivateKeyJwt().assignBearerTokenHeader("Bearer jwtUser_"+base64EncodedMemberId+"_"+jwt);
    
    // when
    subject = grouperPublicPrivateKeyJwt.decode("127.0.0.1");
    
    // then
    Assert.assertTrue(subject == null);
    
    // jwt with expiration date
    jwt = JWT.create()
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() - 1))
        .sign(algorithm);
    
    grouperPublicPrivateKeyJwt = new GrouperPublicPrivateKeyJwt().assignBearerTokenHeader("Bearer jwtUser_"+base64EncodedMemberId+"_"+jwt);
    
    // when
    subject = grouperPublicPrivateKeyJwt.decode("127.0.0.1");
    
    // then
    Assert.assertTrue(subject == null);
    
  }

}
