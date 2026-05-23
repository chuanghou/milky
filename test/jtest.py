#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import annotations

import sys
from pathlib import Path

from jtestlib import ProjectConfig, RuntimeModule, run_jtest_cli

TEST_DIR = Path(__file__).resolve().parent
ROOT = TEST_DIR.parent

GLOBAL_JAVA_OPTS = (
    "-Xms256m",
    "-Xmx512m",
    "-XX:+UseG1GC",
    "-Dfile.encoding=UTF-8",
)

PROJECT = ProjectConfig(
    project_root=ROOT,
    runtime_modules={
        "demo": RuntimeModule(
            directory=ROOT / "demo",
            maven_module="demo",
            main_class="com.stellariver.milky.demo.MilkyDemoApplication",
            port_base=28080,
        ),
    },
    support_modules=(),
    global_java_opts=GLOBAL_JAVA_OPTS,
    stop_port_span=10,
    suite_exclude=(),
)


def main() -> int:
    return run_jtest_cli(
        PROJECT,
        output_dir=TEST_DIR,
        usage_examples=(
            "示例: 在项目根: uv run --project test python test/jtest.py item_update",
            "示例: 在项目根: .\\scripts\\jtest.ps1 item_update",
            "示例: 在 test 目录: uv run python jtest.py -a",
        ),
    )


if __name__ == "__main__":
    sys.exit(main())
