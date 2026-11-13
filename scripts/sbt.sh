#!/usr/bin/env bash
# Runs sbt with a known JDK and sbt launcher, on any Linux machine with curl and tar:
#   bash scripts/sbt.sh <sbt commands...>
#
# Used by the hosted builds, which can't be trusted to provide a suitable sbt:
#   vercel.json  -> bash scripts/sbt.sh dist                (Vercel's build image has no Java)
#   jitpack.yml  -> bash scripts/sbt.sh clean publishM2     (JitPack's launcher varies by host, and
#                                                           1.6.x can't boot sbt 2)
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
# (.sbtopts says the same; repeated here so the script doesn't depend on it)
sbt --server -batch -Dsbt.color=false -Dsbt.supershell=false -Dsbt.log.noformat=true "$@"
