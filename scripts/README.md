# milky 脚本说明

在 **仓库根目录** 下执行（除非注明）。Windows 优先用 `.ps1` / `.bat`，Linux / macOS 用 `.sh`。

## 脚本一览

| 脚本 | 平台 | 作用 |
|------|------|------|
| [jtest.ps1](jtest.ps1) / [jtest.sh](jtest.sh) | 全平台 | 集成测试入口：调用 `test/jtest.py`（jtestlib 编排 demo 启停、编译、跑套件） |
| [jtestlib-source.ps1](jtestlib-source.ps1) / [.sh](jtestlib-source.sh) / [.bat](jtestlib-source.bat) | 全平台 | 切换 `test/` 里 Python 依赖 **jtestlib** 的来源（本地 editable / Gitee git） |
| [mvn-deploy.ps1](mvn-deploy.ps1) / [mvn-deploy.sh](mvn-deploy.sh) | 全平台 | 全仓库 `mvn clean deploy`（内网 Nexus 或 Maven Central，见脚本内配置） |

---

## jtest — 集成测试

**文件：** `jtest.ps1`、`jtest.sh`

等价于：

```bash
uv run --project test python test/jtest.py <参数>
```

项目配置在 [`test/jtest.py`](../test/jtest.py)（运行模块 `demo`、`support_modules`、套件目录等）。

### 常用命令

```powershell
.\scripts\jtest.ps1 -kd              # kill 端口 + 编译 + install 辅助模块 + copy demo/libs
.\scripts\jtest.ps1 -a               # 先 -kd，再跑全部套件
.\scripts\jtest.ps1 item_update      # 跑单个套件
.\scripts\jtest.ps1 -kd item_publish # 准备依赖后跑 publish 套件
```

```bash
./scripts/jtest.sh -kd
./scripts/jtest.sh -a
./scripts/jtest.sh item_flow
```

### jtest 准备依赖时做什么（`-d` / `-kd`）

1. `mvn compile -DskipTests` — 编译整个 reactor（含 demo 的 AspectJ 编织，`demo/pom.xml` 已绑在 `compile` 阶段）
2. `mvn install -pl <support_modules> -am` — 将 demo 依赖的内部模块装进本地 `~/.m2`
3. `mvn dependency:copy-dependencies` — 把 demo 运行时依赖复制到 `demo/libs/`

### 首次使用 Python 环境

```bash
cd test
uv sync
```

jtestlib 安装位置：`test/.venv/Lib/site-packages/jtestlib/`；uv 的 git 缓存在 `%LOCALAPPDATA%\uv\cache\`（勿改缓存里的代码）。

### `test/` 目录与配置

| 路径 | 说明 |
|------|------|
| [`test/jtest.py`](../test/jtest.py) | 运行模块 `demo`、`support_modules`、JVM 参数、`suite_exclude` 等 |
| `test/pyproject.toml` / `uv.lock` | Python 依赖；jtestlib 默认 Gitee git 源 |
| `test/<suite>/test.py` | 套件入口（`verify` + `run_suite`） |
| `test/<suite>/application-demo-<suite>-0.yml` | 该套件 demo 节点的 Spring profile |
| `test/application-jtest.yml` | 全局 jtest 环境（H2、种子 SQL 等） |

### 套件一览

| 套件目录 | 说明 |
|----------|------|
| `item_flow` | 更新标题后 GET 校验 |
| `item_update` | 调用 `/item/update` 更新标题 |
| `item_publish` | `/item/publish` 被 `RpcAspect` 拦截，返回业务错误（对齐 `AspectTest`） |

### 不经过 scripts 包装

```bash
cd test
uv run python jtest.py -kd item_update
uv run python jtest.py -a
```

---

## jtestlib-source — 切换 jtestlib 依赖源

**文件：** `jtestlib-source.ps1`、`jtestlib-source.sh`、`jtestlib-source.bat`（bat 转调 ps1）

用于在 **改 jtestlib 框架** 与 **日常用 git 版 jtestlib** 之间切换。脚本会修改 `test/pyproject.toml` 里的 `[tool.uv.sources]`，然后在 `test/` 下执行 `uv sync`。

建议仓库布局（jtestlib 与 milky 并列）：

```text
IdeaProjects/
├── jtestlib/
└── milky/
    ├── scripts/    # 本目录
    └── test/
```

| 参数 | 行为 |
|------|------|
| `dev` | `jtestlib = { path = "../../jtestlib", editable = true }`；要求已 clone 本地 jtestlib |
| `release` | `jtestlib = { git = "https://gitee.com/chuanghou/jtestlib.git" }` |

```powershell
.\scripts\jtestlib-source.ps1 dev
.\scripts\jtestlib-source.ps1 release
.\scripts\jtestlib-source.bat release
```

```bash
chmod +x scripts/jtestlib-source.sh
./scripts/jtestlib-source.sh dev
./scripts/jtestlib-source.sh release
```

### 改完 jtestlib 后同步到 milky

1. 在 **jtestlib** 仓库 commit / push  
2. milky 根目录：`.\scripts\jtestlib-source.ps1 release`  
3. `cd test && uv lock --upgrade-package jtestlib && uv sync`  
4. 提交 `test/uv.lock`（及必要时 `test/pyproject.toml`）

---

## mvn-deploy — Maven 发布

**文件：** `mvn-deploy.ps1`、`mvn-deploy.sh`

在仓库根执行 `mvn clean deploy`（使用系统 PATH 中的 Maven，非 mvnw）。**部署目标通过参数传入**。

- **internal**：走标准 `maven-deploy-plugin`，不启用 `central-publishing-maven-plugin`；内网 Nexus 的 URL、`serverId`、凭据由本机 `~/.m2/settings.xml`（如 `altDeploymentRepository` 或已激活的 profile）提供，脚本内无需填写仓库地址。
- **central**：开启 GPG 与 `central-publishing-maven-plugin`（须已配置 Portal User Token）。

| 参数 | 含义 |
|------|------|
| `internal` \| `central` | 必填。内网 Nexus 或 Maven Central |
| `-RunTests` / `--run-tests` | 可选。默认 `-DskipTests`；加此参数则先跑测试再 deploy |

```powershell
.\scripts\mvn-deploy.ps1 internal
.\scripts\mvn-deploy.ps1 central
.\scripts\mvn-deploy.ps1 central -RunTests
Get-Help .\scripts\mvn-deploy.ps1 -Full
```

```bash
./scripts/mvn-deploy.sh internal
./scripts/mvn-deploy.sh central
./scripts/mvn-deploy.sh central --run-tests
./scripts/mvn-deploy.sh --help
```

---

## 平台对照

| 用途 | Windows | Unix |
|------|---------|------|
| 集成测试 | `jtest.ps1` | `jtest.sh` |
| jtestlib 源 | `jtestlib-source.ps1` 或 `.bat` | `jtestlib-source.sh` |
| Maven 发布 | `mvn-deploy.ps1` | `mvn-deploy.sh` |
