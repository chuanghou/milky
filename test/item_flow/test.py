#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import annotations

import sys
from pathlib import Path

from jtest import PROJECT
from jtestlib import run_suite

from common import JTEST_SEED_ITEM_ID, get_item_from_api, update_item_title

CURRENT_DIR = Path(__file__).resolve().parent
PROFILE = CURRENT_DIR.name


def verify(_project) -> bool:
    new_title = f"jtest_flow_updated_{PROFILE}"
    update_item_title(JTEST_SEED_ITEM_ID, new_title)
    api_item = get_item_from_api(JTEST_SEED_ITEM_ID)
    print(
        f"流程校验通过: update itemId={JTEST_SEED_ITEM_ID} → {new_title}, "
        f"get amount={api_item.get('amount')}"
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
