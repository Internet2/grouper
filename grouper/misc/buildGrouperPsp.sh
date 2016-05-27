#!/bin/bash                                                                                                                         

if [ $# -ne "3" ]
then
  echo
  echo "Give the PSP tag (e.g. 2.3.0), the grouper tag (e.g. GROUPER_2_3_0), and releaseToSonatype or noRelease!"
  echo "e.g. buildGrouperPsp.sh 2.3.0 GROUPER_2_3_0 noRelease"
  echo
  exit 1
fi

gitPspTag=$1
gitGrouperTag=$2
if [ $3 == 'releaseToSonatype' ]; then
  release=true
elif [ $3 == 'noRelease' ]; then
  release=false
else
  echo "third argument must be releaseToSonatype or noRelease!"
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
wget https://github.com/Internet2/grouper-psp/archive/"$gitPspTag".zip
mv "$gitPspTag" "$gitPspTag".zip
unzip "$gitPspTag".zip
wget https://github.com/Internet2/grouper/archive/"$gitGrouperTag".zip
mv "$gitGrouperTag" "$gitGrouperTag".zip
unzip "$gitGrouperTag".zip
rm *.zip

#mkdir ldappcng
#mv grouper-"$gitGrouperTag"/grouper-misc/grouper-shib/ ldappcng/
cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$gitPspTag"
mv ../grouper-"$gitGrouperTag"/grouper .
mv ../grouper-"$gitGrouperTag"/grouper-parent/ .
mv ../grouper-"$gitGrouperTag"/grouper-misc/ .
mv ../grouper-"$gitGrouperTag"/subject/ .
mv ../grouper-"$gitGrouperTag"/grouper-ui/ .
mv ../grouper-"$gitGrouperTag"/grouper-ws/ .
cp grouper/.classpath.mvn grouper/.classpath
cp grouper-misc/grouperClient/.classpath.mvn grouper-misc/grouperClient/.classpath
cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$gitPspTag"/grouper 
ant clean
  
# Get the full (aka effective) pom so we can search through it for 
# build properties (version, etc)
FULLPOM=effective-pom.xml
echo "Getting our effective pom"
mvn -q help:effective-pom "-Doutput=$FULLPOM"

ARTIFACT=$(xpath "$FULLPOM" 'project/artifactId/text()')
VERSION=$(xpath "$FULLPOM" 'project/version/text()')
TYPE=$(xpath "$FULLPOM" 'project/packaging/text()')
NAME=$(xpath "$FULLPOM" 'project/name/text()')
[ -z "$TYPE" ] && TYPE=jar


#Note: if there are errors about missing jars and maven, download them manually and make the parent dirs and copy the jars
ant
cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-"$gitPspTag"/grouper-parent
if [ "$release" = true ]; then
  echo RELEASE IS TRUE
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn -Dmaven.wagon.provider.http=httpclient clean deploy -DskipTests -Prelease -Dlicense.skip=true
  cd ../psp-parent/
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn -Dmaven.wagon.provider.http=httpclient clean deploy -DskipTests -Prelease -Dlicense.skip=true
else
  echo RELEASE IS NOT TRUE
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn clean deploy -DskipTests -Dlicense.skip=true
  cd ../psp-parent/
  /home/mchyzer/software/apache-maven-3.3.3/bin/mvn clean deploy -DskipTests -Dlicense.skip=true
fi

cd ../psp-distribution-for-grouper/target
gzip -d grouper.psp-"$VERSION".tar.gz
tar xf grouper.psp-"$VERSION".tar
cd grouper.psp-"$VERSION"/lib/custom/
wget https://github.com/Internet2/grouper-psp/blob/master/psp-parent/lib/openspml2-1.0.jar?raw=true --no-check-certificate
mv openspml2-1.0.jar\?raw\=true openspml2-1.0.jar
cd ../../..
rm grouper.psp-"$VERSION".tar
tar cf grouper.psp-"$VERSION".tar grouper.psp-"$VERSION"
gzip grouper.psp-"$VERSION".tar

echo "Output is: "
pwd
echo grouper.psp-"$VERSION".tar.gz

pspOutput="$PWD"


cd /home/mchyzer/tmp/mchyzer_build/grouper-psp-master/grouper-misc/grouper-pspng/


GROUPER_D="../../grouper"

PSPNG_STAGING="target/grouper.pspng-$VERSION"
rm -rf "$PSPNG_STAGING"

mkdir -p "$PSPNG_STAGING/dist"
mkdir -p "$PSPNG_STAGING/lib/custom"

cp -p target/grouper.pspng-"$VERSION".jar "$PSPNG_STAGING/dist"
cp -rp src "$PSPNG_STAGING/"
cp -p pom.xml "$PSPNG_STAGING/"
cp -p README.txt "$PSPNG_STAGING/"


# We want to only tar up jars that are not included in the grouper project itself

# Run copy-dependency in pspng
mvn dependency:copy-dependencies

#Run copy-dependencies in grouper
(cd "$GROUPER_D"; mvn dependency:copy-dependencies)


for f in target/dependency/*.jar; do
  f_base=$(basename "$f")
  if [ -e "$GROUPER_D/target/dependency/$f_base" -o -e "$GROUPER_D/target/$f_base" ]; then
    echo "File did exist in grouper project: $f"
  else
    echo "File did not exist in grouper project: $f"
    cp -p $f "$PSPNG_STAGING/lib/custom"
  fi
done

tar -czvf "target/grouper.pspng-$VERSION.tar.gz" -C target "grouper.pspng-$VERSION"


echo "PSP built to $pspOutput/grouper.psp-$VERSION.tar.gz"

cp target/grouper.pspng-$VERSION.tar.gz $pspOutput
echo "PSPNG build to $pspOutput/grouper.pspng-$VERSION.tar.gz"
