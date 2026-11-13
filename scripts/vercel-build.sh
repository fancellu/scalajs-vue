#!/usr/bin/env bash
# Vercel build command (see vercel.json). Vercel's build image (Amazon Linux 2023) has no Java,
# so this fetches a JDK and the sbt launcher, then runs `sbt dist`, which links the optimized JS
# and assembles the static site in target/dist with the Vuetify gallery as index.html.
# Also works on any Linux box or CI runner with curl and tar.
set -euo pipefail

TOOLS="${TMPDIR:-/tmp}/scalajs-vue-tools"
JDK_FEATURE=21
SBT_LAUNCHER=1.13.1 # launcher only; the sbt version used comes from project/build.properties

case "$(uname -m)" in
  x86_64) ARCH=x64 ;;
  aarch64 | arm64) ARCH=aarch64 ;;
  *) echo "Unsupported architecture: $(uname -m)" >&2; exit 1 ;;
esac

mkdir -p "$TOOLS"

if [ ! -x "$TOOLS/jdk/bin/java" ]; then
  echo "Downloading Temurin JDK $JDK_FEATURE ($ARCH)"
  mkdir -p "$TOOLS/jdk"
  curl -fsSL "https://api.adoptium.net/v3/binary/latest/$JDK_FEATURE/ga/linux/$ARCH/jdk/hotspot/normal/eclipse" |
    tar -xz -C "$TOOLS/jdk" --strip-components=1
fi

if [ ! -x "$TOOLS/sbt/bin/sbt" ]; then
  echo "Downloading sbt launcher $SBT_LAUNCHER"
  curl -fsSL "https://github.com/sbt/sbt/releases/download/v$SBT_LAUNCHER/sbt-$SBT_LAUNCHER.tgz" |
    tar -xz -C "$TOOLS"
fi

export JAVA_HOME="$TOOLS/jdk"
export PATH="$JAVA_HOME/bin:$TOOLS/sbt/bin:$PATH"
java -version

# --server: run sbt in this JVM; sbt 2's default thin client would leave a background sbt server running
sbt --server -batch -Dsbt.color=false -Dsbt.supershell=false dist
