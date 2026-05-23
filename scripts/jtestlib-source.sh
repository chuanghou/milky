#!/usr/bin/env bash
# Usage: ./scripts/jtestlib-source.sh dev|release
set -euo pipefail

MODE="${1:-}"
if [[ "$MODE" != "dev" && "$MODE" != "release" ]]; then
  echo "Usage: $0 dev|release"
  echo "  dev     - local ../../jtestlib (editable, pyproject.toml)"
  echo "  release - git source from pyproject.toml"
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TEST_DIR="$ROOT/test"
PYPROJECT="$TEST_DIR/pyproject.toml"
JTESTLIB_DIR="$(cd "$ROOT/../jtestlib" 2>/dev/null && pwd || true)"

DEV_SOURCE='jtestlib = { path = "../../jtestlib", editable = true }'
RELEASE_SOURCE='jtestlib = { git = "https://gitee.com/chuanghou/jtestlib.git" }'

if [[ "$MODE" == "dev" ]]; then
  if [[ ! -f "${JTESTLIB_DIR}/pyproject.toml" ]]; then
    echo "Local jtestlib not found. Clone beside milky:"
    echo "  git clone https://gitee.com/chuanghou/jtestlib.git $(dirname "$ROOT")/jtestlib"
    exit 1
  fi
  content="$(cat "$PYPROJECT")"
  content="${content//jtestlib = { git = \"https://gitee.com/chuanghou/jtestlib.git\" }/$DEV_SOURCE}"
  content="${content//jtestlib = { path = \"..\/..\/jtestlib\", editable = true }/$DEV_SOURCE}"
  printf '%s' "$content" >"$PYPROJECT"
  rm -f "$TEST_DIR/uv.toml"
  echo "Mode: dev (local editable, pyproject.toml [tool.uv.sources])"
  echo "jtestlib: $JTESTLIB_DIR"
else
  content="$(cat "$PYPROJECT")"
  content="${content//jtestlib = { path = \"..\/..\/jtestlib\", editable = true }/$RELEASE_SOURCE}"
  content="${content//jtestlib = { git = \"https://gitee.com/chuanghou/jtestlib.git\" }/$RELEASE_SOURCE}"
  printf '%s' "$content" >"$PYPROJECT"
  rm -f "$TEST_DIR/uv.toml"
  echo "Mode: release (git source from pyproject.toml)"
fi

cd "$TEST_DIR"
uv sync
uv run python -c "import jtestlib; print(jtestlib.__file__)"
