#!/usr/bin/env bash
# Usage: ./jtestlib-source.sh dev|release
set -euo pipefail

MODE="${1:-}"
if [[ "$MODE" != "dev" && "$MODE" != "release" ]]; then
  echo "Usage: $0 dev|release"
  echo "  dev     - local ../../jtestlib (editable, via uv.toml)"
  echo "  release - git source from pyproject.toml"
  exit 1
fi

TEST_DIR="$(cd "$(dirname "$0")/.." && pwd)"
UV_TOML="$TEST_DIR/uv.toml"
UV_TOML_EXAMPLE="$TEST_DIR/uv.toml.example"

if [[ "$MODE" == "dev" ]]; then
  JTESTLIB_DIR="$(cd "$TEST_DIR/../../jtestlib" 2>/dev/null && pwd || true)"
  if [[ ! -f "${JTESTLIB_DIR}/pyproject.toml" ]]; then
    echo "Local jtestlib not found. Clone beside milky:"
    echo "  git clone https://gitee.com/chuanghou/jtestlib.git $(dirname "$TEST_DIR")/jtestlib"
    exit 1
  fi
  cp "$UV_TOML_EXAMPLE" "$UV_TOML"
  echo "Mode: dev (local editable)"
  echo "jtestlib: $JTESTLIB_DIR"
else
  rm -f "$UV_TOML"
  echo "Mode: release (git source from pyproject.toml)"
fi

cd "$TEST_DIR"
uv sync
uv run python -c "import jtestlib; print(jtestlib.__file__)"
