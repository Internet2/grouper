---
title: "Amazon message encryption"
space: GrIntDev
pageId: 48824702
version: 4
lastUpdated: 2026-07-12T07:02:49.740Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48824702/Amazon+message+encryption
---

We should use encryption:

 

- To hide private messages to everyone but the sender/receiver
- To properly authenticate the sender
- To property authenticate the receiver

 In general we should use one of two methods:

 

| Encryption method | Algorithm | Description | Details | Ease of implementation | Ease of revoking / changing secrets | Can do pub/sub |
| --- | --- | --- | --- | --- | --- | --- |
| Shared secret | rijndael/aes128 | each endpoint needs to share the secret, should send 3 char prefix of secret for easy changing | Encrypt message and salt with shared secret | Easy | Not very easy | Yes |
| Public key | pgp/gpg | public keys can be in a dir on [https://secure.www.upenn.edu](https://secure.www.upenn.edu), or another way to share | Encrypt message and salt with public key or receiver (and optionally the private key of sender) | Not too hard with command line | Easy | No since different messages for each receiver. Sender would need to send multiple messages |

 

### Payload structure

 We should have a standard JSON payload structure for messages. Part of it will be clear text, the payload will be encrypted. Note: for SNS this might mean JSON inside of the SNS JSON wrapper. This will work fine. e.g.

 
```

{
  "encryptionType": "sharedSecret",
  "salt": "sdfjkl354jkl34",
  "secretPrefix": "abc",
  "payload": "sdfjhjk4h235kj3hjkh4kjh54k3j4h5k3jhrt5k3j4h5k3j45hk3j45hk345jh345"
}

```

 or

 
```

{
  "encryptionType": "publicKey",
  "salt": "sdfjkl354jkl34",
  "sender": "penngroups@example.com",
  "payload": "sdfjhjk4h235kj3hjkh4kjh54k3j4h5k3jhrt5k3j4h5k3j45hk3j45hk345jh345"
}

```

 

### Shared secret Java example

 
```

  /**
   * 
   */
  private static void twoWayEncryption() throws Exception {
    
    String message = "Some message";
    String salt = "abc123sdf8sf7d";
    String secret = "ertou8907d987fg";
    
    byte[] key = (salt + secret).getBytes("UTF-8");
    MessageDigest sha = MessageDigest.getInstance("SHA-1");
    key = sha.digest(key);
    key = Arrays.copyOf(key, 16); // use only first 128 bit

    SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
    
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
    byte[] cipherText = cipher.doFinal((message).getBytes());

    System.out.println( new String(new Base64().encode(cipherText), "UTF8") );

    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
    // Decrypt the ciphertext using the same key
    byte[] newPlainText = cipher.doFinal(cipherText);

    System.out.println( new String(newPlainText, "UTF8") );
    
    
  }

```

 Output

 
```

M4vdRLCuti6Dp23D1oyM3g==
Some message

```

 

### Public key example

 This is command line. Note, there is a bouncycastle library for Java which can do PGP, though it is not all that straightforward. Note: the file contents should be base64 encoded in the JSON message

 Encrypt

 
```

[harveycg@flash ~]$ gpg --gen-key
[harveycg@flash ~]$ gpg --export harveycg > harveycg.pub
[harveycg@flash ~]$ gpg --import /home/mchyzer/gpg/mchyzer.pub
[harveycg@flash ~]$ gpg --recipient mchyzer --encrypt perlModules.txt

```

 Decrypt

 
```

[mchyzer@flash ~]$ gpg --gen-key
[mchyzer@flash ~]$ gpg --export mchyzer > mchyzer.pub
[mchyzer@flash ~]$ gpg --decrypt /home/harveycg/gpg/perlModules.txt.gpg  > perlModules.txt

```

 sdf
