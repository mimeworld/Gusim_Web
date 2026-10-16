#!/bin/sh
set -eu
test -f /source/pom.xml
mkdir -p /work/source /data/uploads
find /work/source -mindepth 1 -maxdepth 1 -exec rm -rf {} +
tar -C /source --exclude=target --exclude=uploads --exclude=.git --exclude=.env -cf /work/source.tar .
tar -C /work/source -xf /work/source.tar
rm /work/source.tar
cd /work/source
mvn -B verify
test -f target/gusim-api-1.0.0.jar
cp target/gusim-api-1.0.0.jar /work/app.jar.tmp
mv /work/app.jar.tmp /work/app.jar
chown -R 10001:10001 /data/uploads
