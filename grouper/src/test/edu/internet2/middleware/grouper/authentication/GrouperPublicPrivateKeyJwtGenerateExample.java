package edu.internet2.middleware.grouper.authentication;

import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperPublicPrivateKeyJwtGenerateExample {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    //Subject subject = new GrouperPublicPrivateKeyJwt().assignBearerTokenHeader("Bearer jwtUser_NGM3M2I5Zjk4Zjk3NDk3NWEyMjM0YTk2ZDZiZjNjNzI=_sdfsdfsdf").decode("127.0.0.1");
    
    //System.out.println(subject);
    
    if (args.length != 1) {
      throw new RuntimeException("Pass in one argument: the filename of the private key");
    }
    
    File privateKeyFile = new File(args[0]);
    
    if (!privateKeyFile.exists()) {
      throw new RuntimeException("File doesnt exist: " + privateKeyFile.getAbsolutePath());
    }
    
    String privateKey = StringUtils.trim(GrouperUtil.readFileIntoString(privateKeyFile));
    
    MyTestRSAKeyProvider testRSAKeyProvider = new MyTestRSAKeyProvider(privateKey);
    Algorithm algorithm = Algorithm.RSA256(testRSAKeyProvider);
    
    Date issuedAt = new Date();
    System.out.println(GrouperUtil.timestampToString(issuedAt));
    String jwt = JWT.create().withIssuedAt(issuedAt).sign(algorithm);
    
    System.out.println(jwt);
  }
  
  public GrouperPublicPrivateKeyJwtGenerateExample() {
    super();
  }

  static class MyTestRSAKeyProvider implements RSAKeyProvider {

   public MyTestRSAKeyProvider(String thePrivateKey) {
     this.privateKeyString = thePrivateKey;
   }
   public String privateKeyString;
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      throw new RuntimeException("Who cares");
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
      PrivateKey privateKey = null;
      try {
        byte[] privateKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
       
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

}
