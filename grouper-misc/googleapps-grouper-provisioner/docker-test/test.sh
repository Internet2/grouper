#!/usr/bin/env bash
docker-compose rm -f
rm -r ./grouper/temp/
mkdir -p ./grouper/temp/

pushd ..
mvn clean package dependency:copy-dependencies -DskipTests -DincludeScope=runtime

popd
cp -r ../target/lib ./grouper/temp/
cp ../target/google-*.jar ./grouper/temp/lib/

docker-compose build
