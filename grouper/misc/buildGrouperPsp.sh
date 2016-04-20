#!/bin/bash                                                                                                                         

if [ $# -ne "3" ]
then
  echo
  echo "Give the PSP tag (e.g. 2.3.0), the grouper tag (e.g. GROUPER_2_3_0), and releaseToSonatype or noRelease!"
  echo "e.g. buildGrouperPsp.sh 2.3.0 GROUPER_2_3_0 noRelease"
  echo
  exit 1
fi

pspTag=$1
grouperTag=$2
if [ $3 == 'releaseToSonatype' ]; then
  release=true
elif [ $3 == 'noRelease' ]; then
  release=false
else
  echo "third argument must be releseToSonatype or noRelease!"
  exit 1
fi

export oldJavaHome=$JAVA_HOME
export oldPath=$PATH
export JAVA_HOME=/home/mchyzer/software/java
export PATH=$JAVA_HOME/bin:$PATH
export MAVEN_OPTS=-Xmx1024m


cd /home/mchyzer/tmp
rm -rf /home/mchyzer/tmp/mchyzer_build
mkdir /home/mchyzer/tmp/mchyzer_build
cd /home/mchyzer/tmp/mchyzer_build/
wget https://github.com/Internet2/grouper-psp/archive/"$pspTag".zip
mv "$pspTag" "$pspTag".zip
unzip "$pspTag".zip
wget https://github.com/Internet2/grouper/archive/"$grouperTag".zip
mv "$grouperTag" "$grouperTag".zip
unzip "$grouperTag".zip
rm *.zip
#mkdir ldappcng
#mv grouper-"$grouperTag"/grouper-misc/grouper-shib/ ldappcng/
cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$pspTag"
mv ../grouper-"$grouperTag"/grouper .
mv ../grouper-"$grouperTag"/grouper-parent/ .
mv ../grouper-"$grouperTag"/grouper-misc/ .
mv ../grouper-"$grouperTag"/subject/ .
mv ../grouper-"$grouperTag"/grouper-ui/ .
mv ../grouper-"$grouperTag"/grouper-ws/ .
cp grouper/.classpath.mvn grouper/.classpath
cp grouper-misc/grouperClient/.classpath.mvn grouper-misc/grouperClient/.classpath
cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$pspTag"/grouper 
ant clean
  
#Note: if there are errors about missing jars and maven, download them manually and make the parent dirs and copy the jars
ant
cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$pspTag"/grouper-parent
if [ "$release" = true ]; then
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn -Dmaven.wagon.provider.http=httpclient clean deploy -DskipTests -Prelease -Dlicense.skip=true
  cd ../psp-parent/
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn -Dmaven.wagon.provider.http=httpclient clean deploy -DskipTests -Prelease -Dlicense.skip=true
else
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn clean deploy -DskipTests -Dlicense.skip=true
  cd ../psp-parent/
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn clean deploy -DskipTests -Dlicense.skip=true
fi

cd ../psp-distribution-for-grouper/target
gzip -d grouper.psp-"$pspTag".tar.gz
tar xf grouper.psp-"$pspTag".tar
cd grouper.psp-"$pspTag"/lib/custom/
wget https://github.com/Internet2/grouper-psp/tree/master/psp-parent/lib/openspml2-1.0.jar?raw=true --no-check-certificate
cd ../../..
rm grouper.psp-"$pspTag".tar
tar cf grouper.psp-"$pspTag".tar grouper.psp-"$pspTag"
gzip grouper.psp-"$pspTag".tar
echo "Output is: "
pwd
echo grouper.psp-"$pspTag".tar.gz
