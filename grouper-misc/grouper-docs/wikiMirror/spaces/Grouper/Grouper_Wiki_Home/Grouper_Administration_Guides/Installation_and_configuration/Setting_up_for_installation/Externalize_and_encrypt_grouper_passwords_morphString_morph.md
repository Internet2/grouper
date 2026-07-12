---
title: "Externalize and encrypt grouper passwords morphString morph"
space: Grouper
pageId: 28549242
version: 18
lastUpdated: 2026-07-01T05:42:27.480Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549242/Externalize+and+encrypt+grouper+passwords+morphString+morph
---

You should externalize and encrypt Grouper LDAP and database passwords, especially in production. Grouper ships a **morphString** utility that uses a system key to symmetrically encrypt and decrypt sensitive values, so passwords never have to appear in clear text in your config files (GSH, GShell, morphString, morph).

 The goals are:

 

1. Config files can be shared (emailed, attached to tickets) without having to scrub passwords out of them first.
2. Config files and war files can be stored in version control without containing passwords.
3. Only people with access to the production host need to know the password, not developers who build and hand off the war to deploy.
4. If someone finds a config file, they cannot read the password, and there is no documented way to decrypt it without the encryption key.

 > **Applies to:** all current supported releases (the morphString utility and its configuration are unchanged across them). morphString is a long-standing core utility.
> 
>  **Access required:** this is a server-side deployment task, not a Grouper application privilege. You need filesystem access to the Grouper install to set the encryption key in `morphString.properties`, edit the config files that hold the passwords, and restart Grouper.

 

 

## Set the encryption key

 In `morphString.properties`, set `encrypt.key` to a random alphanumeric string (case sensitive), or to the absolute pathname of a file that contains that string, or use `encrypt.key.elConfig` for an expression-language scriptlet (for example to read the key from an environment variable or file). Keep the key off the deployed host’s shared config — anyone with the key can decrypt the passwords.

 If your passwords contain slashes and you are not externalizing them to files, set `encrypt.disableExternalFileLookup = true` so Grouper does not try to interpret the value as a file path.

 A typical `morphString.properties`:

 
```text
# A random alphanumeric string, or an absolute path to a file containing it
encrypt.key = /opt/grouper/conf/grouperEncryptKey.txt

# Set to true if your passwords contain slashes and you do NOT want
# values looked up as external files
encrypt.disableExternalFileLookup = false
```

 

## Encrypt a password with morphString

 Run the `Encrypt` utility from a directory that contains your `morphString.properties` (with the encryption key). Use the `grouperClient` jar that matches your Grouper version; the morphString classes ship inside it. You can download it from Maven Central if you don’t already have it on the host:

 
```bash
# optional: download the grouperClient jar for your version
wget https://repo1.maven.org/maven2/edu/internet2/middleware/grouper/grouperClient/<version>/grouperClient-<version>.jar

# put morphString.properties (with your encrypt.key) in this directory, then:
$ java -cp .:grouperClient-<version>.jar edu.internet2.middleware.morphString.Encrypt
Type the string to encrypt (note: pasting might echo it back):
The encrypted string is: qN28V6C3Qt7ffqI4lSf/iQ==
```

 The utility masks the typed input by default. If masking causes problems (it relies on JVM console behavior), pass `dontMask` to read the value as plain input instead. That also makes it easy to script:

 
```bash
java -cp .:grouperClient-<version>.jar edu.internet2.middleware.morphString.Encrypt dontMask <<< "somePass" \
  | sed -n '2p' | sed 's/The encrypted string is: //'
Ev3sDTJm0evgFaQsE69WHA==
```

 

## Reference the encrypted password in configuration

 Once you have an encrypted string, replace the clear-text password in `grouper.hibernate.properties`, `subject.properties`, or any other Grouper password property in one of three ways. Grouper attempts to decrypt the value at startup using the key from `morphString.properties`.

 

### Inline encrypted value

 Put the encrypted string directly as the property value:

 
```text
hibernate.connection.password = qN28V6C3Qt7ffqI4lSf/iQ==
```

 

### External file

 Write the encrypted string to a file and set the property value to that file’s **absolute** path. Grouper detects that the value is a file (it contains a directory delimiter), reads the file, and decrypts the contents. This keeps the encrypted password out of the config file entirely.

 
```bash
$ echo 'qN28V6C3Qt7ffqI4lSf/iQ==' > /opt/pass/myGrouper/db.pass

# in grouper.hibernate.properties:
hibernate.connection.password = /opt/pass/myGrouper/db.pass
```

 

### Expression language (.elConfig)

 Use the `.elConfig` form of the property with an expression-language scriptlet. This is the most explicit option and is the style used by the shipped defaults:

 
```text
hibernate.connection.password.elConfig = ${elUtils.readFileIntoStringUtf8('/opt/pass/myGrouper/db.pass')}
```

 > An absolute path is required for the external-file forms. Grouper uses the "/" directory delimiter to tell an external file reference apart from a literal password string. If your literal password contains slashes, set `encrypt.disableExternalFileLookup = true` so it is not mistaken for a file path.

 The same approach works for any password Grouper reads, including LDAP subject source and provisioner bind credentials — encrypt the value with morphString and reference it inline, by file path, or via `.elConfig`.
