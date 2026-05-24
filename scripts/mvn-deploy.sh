#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
用法: mvn-deploy.sh <internal|central> [--run-tests]

  internal       发布到内网 Nexus（脚本内 URL / serverId 须与 settings.xml 一致）
  central        发布到 Maven Central（须配置 GPG 与 central server）
  --run-tests    默认跳过测试；加此参数则执行测试后再 deploy

示例:
  ./scripts/mvn-deploy.sh internal
  ./scripts/mvn-deploy.sh central --run-tests
EOF
}

# ========== 发布配置（按需修改）==========
# 内网（Target = internal）
# settings.xml <server><id> 须与 INTERNAL_SERVER_ID 一致
INTERNAL_SERVER_ID="internal"
INTERNAL_RELEASE_URL="http://repo.example.com/nexus/content/repositories/releases/"
INTERNAL_SNAPSHOT_URL="http://repo.example.com/nexus/content/repositories/snapshots/"

# 中央仓库（Target = central）
# pom 中 publishingServerId=central；settings.xml 须配置 central / GPG
# =========================================

DEPLOY_TARGET=""
SKIP_TESTS=true

while [[ $# -gt 0 ]]; do
  case "$1" in
    internal|central)
      DEPLOY_TARGET="$1"
      shift
      ;;
    --run-tests)
      SKIP_TESTS=false
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "未知参数: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$DEPLOY_TARGET" ]]; then
  echo "缺少部署目标: internal 或 central" >&2
  usage
  exit 1
fi

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
esac

"${MVN[@]}"

if [[ "$TARGET_LC" == central ]]; then
  echo "Central deploy 已执行（门户侧可能仍在校验/发布）"
else
  echo "内网 deploy 完成"
fi
