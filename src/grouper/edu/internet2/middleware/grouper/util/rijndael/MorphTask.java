package edu.internet2.middleware.grouper.util.rijndael;

/**
 * 
 */
import org.apache.tools.ant.Task;

/**
 * encrypt or decrypt passwords via ant
 * 
 * @author mchyzer
 */
public class MorphTask extends Task {

//  <taskdef name="grouper.password" classname="edu.internet2.middleware.grouper.util.rijndael.PasswordTask" >
//    <classpath refid="lib.class.path"/>
//    <classpath>
//      <pathelement location="${build.classes.dir}"/>  
//    </classpath>    
//  </taskdef>
//  <!-- task to encrypt the password -->
//  <target name="encryptPassword" description="encrypt a password for a properties file">
//      <input message="Please input the password to encrypt: "
//         addproperty="input.password" />
//      <pww.password action="encrypt" password="${input.password}" 
//         property="result.encryptedPassword" />
//      
//      <echo message="The encrypted password is: ${result.encryptedPassword}" />
// 
//  </target>

  
  /**
   * 
   */
  public MorphTask() {
    super();
  }
  
  /** encrypted or decrypted pass */
  private String password = null;
  
  /** where to put the answer */
  private String property = null;
  
  /**
   * encrypted or decrypted pass
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * encrypted or decrypted pass
   * @param thePassword
   */
  public void setPassword(String thePassword) {
    this.password = thePassword;
  }

  /**
   * where to put the answer
   * @return the property
   */
  public String getProperty() {
    return this.property;
  }

    /**
   * Runs the task
   * 
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() {
    
    String newPass = null;
    
    try {
      newPass = Morph.encrypt(this.password);
    } catch (Exception e) {
      throw new RuntimeException("Problem with morph task, " + e.getMessage(), e);
    }
    getProject().setNewProperty(this.property, newPass);
  }

  /**
   * where to put the answer
   * 
   * @param theProperty
   */
  public void setProperty(String theProperty) {
    this.property = theProperty;
  }
  
  
  
}
