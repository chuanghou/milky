#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

from jtestlib.jtest_orm import Entity


@dataclass
class ItemDo(Entity):
  class Meta:
    table_name = "item"
    pk = "id"
    logic_delete_field = "deleted"
    logic_not_deleted_value = 0

  id: Optional[int] = None
  title: Optional[str] = None
  user_id: Optional[int] = None
  user_name: Optional[str] = None
  amount: Optional[int] = None
  store_code: Optional[str] = None
  price: Optional[str] = None
  channel_enum: Optional[str] = None
  ratio: Optional[int] = None
  version: Optional[int] = None
  deleted: Optional[int] = None
