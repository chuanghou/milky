#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import annotations

import sys
from pathlib import Path

from jtest import PROJECT
from jtestlib import run_suite

from common import PUBLISH_ASPECT_BLOCKED_MESSAGE, publish_item

CURRENT_DIR = Path(__file__).resolve().parent
PROFILE = CURRENT_DIR.name


def verify(_project) -> bool:
    title = f"jtest_{PROFILE}"
    result = publish_item(title)
    if result.get("success") is not False:
        raise RuntimeError(
            f"预期 publish 被 RpcAspect 拦截 (success=false)，实际: {result!r}"
        )
    message = result.get("message")
    if message != PUBLISH_ASPECT_BLOCKED_MESSAGE:
        raise RuntimeError(
            f"预期 message={PUBLISH_ASPECT_BLOCKED_MESSAGE!r}，实际 message={message!r}"
        )
    print(
        f"publish 切面校验通过: title={title!r} → success=false, "
        f"message={message!r}"
    )
    return True


def main() -> int:
    return run_suite(
        project=PROJECT,
        suite_name=PROFILE,
        verify=verify,
        module_nodes={"demo": (0,)},
    )


if __name__ == "__main__":
    sys.exit(main())
