#!/usr/bin/env bash
set -euo pipefail

# ========== 发布配置（按需修改）==========
# 目标：internal | central
DEPLOY_TARGET="internal"
SKIP_TESTS=true

# 内网（DEPLOY_TARGET=internal）
# settings.xml <server><id> 须与 INTERNAL_SERVER_ID 一致
INTERNAL_SERVER_ID="internal"
INTERNAL_RELEASE_URL="http://repo.example.com/nexus/content/repositories/releases/"
INTERNAL_SNAPSHOT_URL="http://repo.example.com/nexus/content/repositories/snapshots/"

# 中央仓库（DEPLOY_TARGET=central）
# pom 中 publishingServerId=central；settings.xml 须配置 central / GPG
# =========================================

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MVN=(./mvnw clean deploy -Dmaven.deploy.skip=false)
if [[ "$SKIP_TESTS" == true ]]; then
  MVN+=(-DskipTests)
fi

TARGET_LC="$(echo "$DEPLOY_TARGET" | tr '[:upper:]' '[:lower:]')"
case "$TARGET_LC" in
  internal)
    echo "milky deploy -> 内网 Maven 仓库"
    echo "  serverId=$INTERNAL_SERVER_ID"
    echo "  release=$INTERNAL_RELEASE_URL"
    echo "  snapshot=$INTERNAL_SNAPSHOT_URL"
    ALT_RELEASE="${INTERNAL_SERVER_ID}::default::${INTERNAL_RELEASE_URL}"
    ALT_SNAPSHOT="${INTERNAL_SERVER_ID}::default::${INTERNAL_SNAPSHOT_URL}"
    MVN+=(
      -Dgpg.skip=true
      -DskipPublishing=true
      "-DaltDeploymentRepository=${ALT_RELEASE}"
      "-DaltSnapshotDeploymentRepository=${ALT_SNAPSHOT}"
    )
    ;;
  central)
    echo "milky deploy -> Maven Central"
    MVN+=(-Dgpg.skip=false -DskipPublishing=false)
    ;;
  *)
    echo "DeployTarget 无效: '$DEPLOY_TARGET'，请设为 internal 或 central" >&2
    exit 1
    ;;
esac

"${MVN[@]}"

if [[ "$TARGET_LC" == central ]]; then
  echo "Central deploy 已执行（门户侧可能仍在校验/发布）"
else
  echo "内网 deploy 完成"
fi
