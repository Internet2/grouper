/*******************************************************************************
 * Copyright 2012 Internet2
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
package edu.internet2.middleware.grouper.changeLog.esb.consumer;

/**
 * container around esb event
 * @author mchyzer
 *
 */
public class EsbEvents {

  /**
   * this is the encrypted payload which is another entire EsbEvents object
   */
  private String encryptedPayload;
  
  /**
   * this is the encrypted payload which is another entire EsbEvents object
   * @return the encryptedPayload
   */
  public String getEncryptedPayload() {
    return this.encryptedPayload;
  }
  
  /**
   * this is the encrypted payload which is another entire EsbEvents object
   * @param encryptedPayload the encryptedPayload to set
   */
  public void setEncryptedPayload(String encryptedPayload) {
    this.encryptedPayload = encryptedPayload;
  }

  /**
   * the encryption key used to encrypt, sha1 that, and take the
   * first 4, this is like a checksum so the receiver knows
   * which key is being used
   */
  private String encryptionKeySha1First4;
  
  /**
   * the encryption key used to encrypt, sha1 that, and take the
   * first 4, this is like a checksum so the receiver knows
   * which key is being used
   * @return the encryptionKeySha1First4
   */
  public String getEncryptionKeySha1First4() {
    return this.encryptionKeySha1First4;
  }
  
  /**
   * the encryption key used to encrypt, sha1 that, and take the
   * first 4, this is like a checksum so the receiver knows
   * which key is being used
   * @param _encryptionKeySha1First4 the encryptionKeySha1First4 to set
   */
  public void setEncryptionKeySha1First4(String _encryptionKeySha1First4) {
    this.encryptionKeySha1First4 = _encryptionKeySha1First4;
  }

  /**
   * if this transmission is encrypted
   */
  private boolean encrypted;
  
  /**
   * if this transmission is encrypted
   * @return the encrypted
   */
  public boolean isEncrypted() {
    return this.encrypted;
  }

  
  /**
   * if this transmission is encrypted
   * @param encrypted the encrypted to set
   */
  public void setEncrypted(boolean encrypted) {
    this.encrypted = encrypted;
  }

  /** */
	private EsbEvent[] esbEvent;

	/**
	 * 
	 * @return event array
	 */
	public EsbEvent[] getEsbEvent() {
		return esbEvent;
	}

	/**
	 * 
	 * @param esbEvent
	 */
	public void setEsbEvent(EsbEvent[] esbEvent) {
		this.esbEvent = esbEvent;
	}

	/**
	 * 
	 * @param esbEvent
	 */
	public void addEsbEvent(EsbEvent esbEvent) {
		if(this.esbEvent ==null) {
			this.esbEvent = new EsbEvent[] {esbEvent};
		} else {
			EsbEvent[] newArray = new EsbEvent[this.esbEvent.length + 1];
		      System.arraycopy(this.esbEvent, 0, newArray, 0,
		          this.esbEvent.length);
		      newArray[this.esbEvent.length + 1] = esbEvent;
		      this.esbEvent= newArray;
		}
	}
}
