# milky-test

基于 [jtestlib](https://gitee.com/chuanghou/jtestlib) 的 Java 多模块本地集成测试编排（uv 管理 Python 依赖）。

## 目录约定

建议将两个仓库**并列**克隆：

```text
IdeaProjects/
├── jtestlib/     # 框架源码（你主要在这里改 Python）
└── milky/
    └── test/     # 本目录，联调 demo / Java 服务
```

## 配置优先级

`pyproject.toml` 与 `uv.toml` 可放在同一目录（`test/`）。**两者共存时，`uv.toml` 优先级更高**，同名项（例如 `[tool.uv.sources]`）以 `uv.toml` 为准。

因此：

- 仓库里只提交 `pyproject.toml`（git 源），团队 / CI 行为一致；
- 本地调试时增加 `uv.toml`，覆盖为 editable path，**不必改** `pyproject.toml`。

## 依赖模式

| 模式 | 适用场景 | 配置 |
|------|----------|------|
| **release**（默认） | 日常跑测试、提交 milky、CI | 仅 `pyproject.toml`（git 源） |
| **dev** | 调试 / 改 jtestlib 框架 | 增加 `uv.toml`（不提交，覆盖为本地 path） |

切换命令见下一节 `jtestlib-source` 脚本。

`pyproject.toml` 中默认从 Gitee 安装：

```toml
[dependency-groups]
dev = ["jtestlib[full]"]

[tool.uv.sources]
jtestlib = { git = "https://gitee.com/chuanghou/jtestlib.git" }
```

## 快速开始（release，git 依赖）

```bash
cd test
uv sync
# 或显式:  scripts/jtestlib-source.ps1 release
```

安装位置在 `test/.venv/Lib/site-packages/jtestlib/`；uv 的 git 缓存在 `%LOCALAPPDATA%\uv\cache\`（勿改缓存里的代码）。

## 切换 jtestlib 来源

一个脚本，参数 `dev` / `release`：

| 参数 | 行为 |
|------|------|
| `dev` | 生成 `uv.toml`，使用 `../../jtestlib`（editable） |
| `release` | 删除 `uv.toml`，使用 `pyproject.toml` 的 git 源 |

```powershell
cd test
.\scripts\jtestlib-source.ps1 dev      # 本地调试框架
.\scripts\jtestlib-source.ps1 release  # 提交 milky 前 / 日常
```

```bash
cd test
chmod +x scripts/jtestlib-source.sh
./scripts/jtestlib-source.sh dev
./scripts/jtestlib-source.sh release
```

也可手动：复制 `uv.toml.example` → `uv.toml`（dev），或删除 `uv.toml`（release），再 `uv sync`。

## 改完框架后的发布流程

1. 在 **jtestlib** 仓库里 commit / push 到 Gitee  
2. 在 **milky/test** 执行 `.\scripts\jtestlib-source.ps1 release`（或确保无 `uv.toml`）  
3. `uv lock --upgrade-package jtestlib && uv sync`  
4. 提交 `uv.lock`（及必要时 `pyproject.toml`）

## 在 jtestlib 仓库里单独开发

```bash
cd ../../jtestlib
uv sync
uv run python -c "import jtestlib; ..."
```

框架自身的单元测试、工具脚本应在 jtestlib 仓库维护；milky 用于对 demo 做集成联调。

## 运行集成测

```bash
cd test
uv sync
uv run python jtest.py -kd item_update
uv run python jtest.py -a
```

配置在 `test/jtest.py`；套件目录为 `test/<suite>/test.py`。
