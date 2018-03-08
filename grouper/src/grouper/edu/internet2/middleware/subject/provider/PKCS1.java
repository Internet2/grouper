/**
 * Copyright 2014 Internet2
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
/*
  Read a private key, in the PKCS1 format

  -----BEGIN RSA PRIVATE KEY-----
  MIICXwIBAAKBgQDMTApZEOCWwGf4lXk... (base 64 encoded stuff)
  -----END RSA PRIVATE KEY-----

*/

/** note: presently doesn't look fo rthe 'PRIVATE KEY' line,
          so the key file can have ONLY a private key
 **/

package edu.internet2.middleware.subject.provider;

import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;

import org.apache.commons.codec.binary.Base64;

// import sun.misc.BASE64Decoder;
// import org.bouncycastle.util.encoders.Base64;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class PKCS1 {
	int pos;
	byte[] code;
	BigInteger[] ints;

    private static Log log = LogFactory.getLog(PKCS1.class);

	public PKCS1() { }

	RSAPrivateCrtKeySpec keySpec() {
		return new RSAPrivateCrtKeySpec(ints[0] // modulus
				, ints[1]	// publicExponent
				, ints[2]	// privateExponent
				, ints[3]	// primeP
				, ints[4]	// primeQ
				, ints[5]	// primeExponentP
				, ints[6]	// primeExponentQ
				, ints[7]	// crtCoefficient
				);
	}
	int rdLen() throws IOException {
		int t;
		if ((code[pos] & 0x80) == 0x80) {
			int n = (int) code[pos] & 0x7f;
			pos = pos + 1;
			t = rdLongLen(n);
		} else {
			t = (int) code[pos];
			pos = pos + 1;
		}
		return t;
	}
	int rdLongLen(int n) throws IOException {
		int r = 0;
		for (int i = 0; i < n; ++i) {
			r = (r << 8) | (code[pos] & 0xff);
			pos = pos + 1;
		}
		return r;
	}
	void skipInteger() throws IOException {
		if (code[pos] != 2)
			throw new IOException("encountered invalid integer tag "
				+ ((int) code[pos]) + " at " + pos);
		pos = pos + 1;
		int len = rdLen();
		pos = pos + len;
	}
	BigInteger rdInteger() throws IOException {
		if (pos >= code.length)
			throw new EOFException("end of file at " + pos);
		if (code[pos] != 2)
			throw new IOException("encountered invalid integer tag "
				+ ((int) code[pos]) + " at " + pos);
		pos = pos + 1;
		int len = rdLen();
		byte[] x = new byte[len];
		System.arraycopy(code, pos, x, 0, len);
		pos = pos + len;
		return new BigInteger(x);
	}
	void rdKey(int nb) throws IOException {
		ints = new BigInteger[8];
		skipInteger();  // version
		for (int i = 0; i < 8; ++i)
			ints[i] = rdInteger();
	}
	public void extractIntegers(byte[] data) throws IOException {
		pos = 0;
		code = data;
		if (code[pos] == 0x30) {
			pos = 1;
			int nb = rdLen();
			rdKey(nb);
		} else
			throw new IOException("invalid private key leading tag "
					+ (int) code[pos]);
	}
	char[] readWrappedBody(String name) throws IOException {
		FileReader file = new FileReader(name);
		char[] ba = new char[2048];
		int i;
                StringBuffer banner = null;
		boolean bnl = false;
		boolean knl = false;
        try {
		for (i = 0; i < 2048; ) {
			int ic = file.read();
			char c = (char) ic;
			if (ic < 0) break;
			else if (c == '\n') {
                                if (bnl) {  // processing a banner?
                                   bnl = false;
                                   if (banner.indexOf("PRIVATE KEY")>0) knl = true;
                                }
			} else if (bnl) {
				banner.append(c);
			} else if (c=='-') {
                                bnl = true;
                                if (knl) break; // done with key
                                banner = new StringBuffer(128);
			} else if (knl) {
				ba[i++] = c;
			}
		}
                file.close();
		char[] contents = new char[i];
		System.arraycopy(ba, 0, contents, 0, i);
                log.debug("readWrapped done, length = " + i);
		return contents;
           } catch (Exception e) {
                log.debug("readWrapped error: " + e);
           }
           return null;
	}
	byte[] readDecodedBytes(String name) throws IOException {
		char[] ba = readWrappedBody(name);
		int n = ba.length;
		// return new BASE64Decoder().decodeBuffer(new String(ba, 0, n));
		return new Base64().decode(new String(ba, 0, n));
	}
	RSAPrivateCrtKeySpec readKeyFile(String name) throws IOException {
		byte[] data = readDecodedBytes(name);
		extractIntegers(data);
		return keySpec();
	}
	public PrivateKey readKey(String name) throws IOException {
		RSAPrivateCrtKeySpec sp = readKeyFile(name);
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("RSA: " + e.toString());
		}
		PrivateKey pk;
		try {
			 pk = kf.generatePrivate(sp);
		} catch (InvalidKeySpecException e) {
			throw new IOException(e.toString());
		}
		return pk;
	}
}
