#!/bin/sh
set -eu
# Only the dedicated staging copy is replaced. The read-only host source is untouched.
test -f /source/package.json
test -f /source/pnpm-lock.yaml
mkdir -p /work/source /output
find /work/source -mindepth 1 -maxdepth 1 -exec rm -rf {} +
tar -C /source --exclude=node_modules --exclude=dist --exclude=.git --exclude=.env --exclude='*.local' -cf /work/source.tar .
tar -C /work/source -xf /work/source.tar
rm /work/source.tar
cd /work/source
corepack enable
corepack prepare pnpm@11.25.0 --activate
pnpm install --frozen-lockfile --store-dir /pnpm-store
pnpm run build
test -f dist/index.html
# Publish only after a successful build; run updates while the serving container is stopped.
find /output -mindepth 1 -maxdepth 1 -exec rm -rf {} +
cp -R dist/. /output/
