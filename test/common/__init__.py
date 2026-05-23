#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import annotations

from typing import Any
from urllib import parse

from jtestlib import request_get, service_http_base

from jtest import PROJECT

# application-jtest.yml 中 jtest-seed.sql 预置的商品 id
JTEST_SEED_ITEM_ID = 90001


def assert_success_result(result: Any, *, action: str = "请求") -> dict:
    if result is None:
        raise RuntimeError(f"{action}失败: 无响应")
    if not isinstance(result, dict):
        raise RuntimeError(f"{action}失败: 响应不是 JSON 对象: {result!r}")
    if result.get("success") is not True:
        raise RuntimeError(f"{action}失败: {result}")
    data = result.get("data")
    return data if isinstance(data, dict) else {}


def update_item_title(item_id: int, new_title: str, *, service: str = "demo", node: int = 0) -> None:
    base = service_http_base(PROJECT, service, node)
    url = (
        f"{base}/item/update?"
        f"{parse.urlencode({'itemId': item_id, 'newTitle': new_title})}"
    )
    result, _ = request_get(url, timeout=30)
    assert_success_result(result, action="修改商品标题")


def get_item_from_api(item_id: int, *, service: str = "demo", node: int = 0) -> dict:
    base = service_http_base(PROJECT, service, node)
    url = f"{base}/item/get?{parse.urlencode({'id': item_id})}"
    result, _ = request_get(url, timeout=30)
    return assert_success_result(result, action="查询商品")


# RpcAspect 拦截 @TestForInterceptor 的 publish（与 AspectTest 一致）
PUBLISH_ASPECT_BLOCKED_MESSAGE = "时间区间不在可接收范围"


def publish_item(title: str, *, service: str = "demo", node: int = 0) -> dict:
    """调用 /item/publish，返回完整 Result JSON（不假定 success）。"""
    base = service_http_base(PROJECT, service, node)
    url = f"{base}/item/publish?{parse.urlencode({'title': title})}"
    result, err = request_get(url, timeout=30)
    if result is None:
        raise RuntimeError(f"发布商品请求失败: {err}")
    if not isinstance(result, dict):
        raise RuntimeError(f"发布商品响应不是 JSON 对象: {result!r}")
    return result
