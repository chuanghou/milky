#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
用法: mvn-deploy.sh <internal|central> [--run-tests]

  internal       发布到内网 Maven 仓库（仓库地址与凭据见本机 ~/.m2/settings.xml）
  central        发布到 Maven Central（须 GPG + Portal User Token）
  --run-tests    默认跳过测试

示例:
  ./scripts/mvn-deploy.sh internal
  ./scripts/mvn-deploy.sh central --run-tests
EOF
}

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

if ! command -v mvn >/dev/null 2>&1; then
  echo "未找到 mvn，请确认 Maven 已安装并在 PATH 中" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

MVN=(mvn clean deploy -Dmaven.deploy.skip=false)
if [[ "$SKIP_TESTS" == true ]]; then
  MVN+=(-DskipTests)
fi

TARGET_LC="$(echo "$DEPLOY_TARGET" | tr '[:upper:]' '[:lower:]')"
case "$TARGET_LC" in
  internal)
    echo "milky deploy -> 内网 Maven 仓库（使用 settings.xml 中的仓库配置）"
    MVN+=(-Dgpg.skip=true)
    ;;
  central)
    echo "milky deploy -> Maven Central"
    MVN+=(-Dgpg.skip=false -DskipPublishing=false -Dcentral.publishing.phase=deploy)
    ;;
esac

"${MVN[@]}"

if [[ "$TARGET_LC" == central ]]; then
  echo "Central deploy 已执行（门户侧可能仍在校验/发布）"
else
  echo "内网 deploy 完成"
fi
