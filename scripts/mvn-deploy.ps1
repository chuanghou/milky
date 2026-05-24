param(
    [Parameter(Position = 0)]
    [ValidateSet("internal", "central")]
    [string]$Target,

    [switch]$RunTests
)

function Show-MvnDeployUsage {
    Write-Host @"
用法: .\scripts\mvn-deploy.ps1 <internal|central> [-RunTests]

  internal     发布到内网 Nexus（脚本内 URL / serverId 须与 settings.xml 一致）
  central      发布到 Maven Central（须配置 GPG 与 central server）
  -RunTests    默认跳过测试；加此参数则执行测试后再 deploy

示例:
  .\scripts\mvn-deploy.ps1 internal
  .\scripts\mvn-deploy.ps1 central
  .\scripts\mvn-deploy.ps1 central -RunTests
"@ -ForegroundColor Yellow
}

if (-not $Target) {
    Write-Error "缺少部署目标: internal 或 central"
    Show-MvnDeployUsage
    exit 1
}

# ========== 发布配置（按需修改）==========
# 内网（Target = internal）
# settings.xml <server><id> 须与 InternalServerId 一致
$InternalServerId = "internal"
$InternalReleaseUrl = "http://repo.example.com/nexus/content/repositories/releases/"
$InternalSnapshotUrl = "http://repo.example.com/nexus/content/repositories/snapshots/"

# 中央仓库（Target = central）
# pom 中 publishingServerId=central；settings.xml 须配置 central / GPG
# =========================================

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$mvnArgs = @("clean", "deploy", "-Dmaven.deploy.skip=false")
if (-not $RunTests) {
    $mvnArgs += "-DskipTests"
}

switch ($Target.ToLowerInvariant()) {
    "internal" {
        Write-Host "milky deploy -> 内网 Maven 仓库" -ForegroundColor Cyan
        Write-Host "  serverId=$InternalServerId"
        Write-Host "  release=$InternalReleaseUrl"
        Write-Host "  snapshot=$InternalSnapshotUrl"

        $altRelease = "${InternalServerId}::default::${InternalReleaseUrl}"
        $altSnapshot = "${InternalServerId}::default::${InternalSnapshotUrl}"
        $mvnArgs += @(
            "-Dgpg.skip=true",
            "-DskipPublishing=true",
            "-DaltDeploymentRepository=$altRelease",
            "-DaltSnapshotDeploymentRepository=$altSnapshot"
        )
    }
    "central" {
        Write-Host "milky deploy -> Maven Central" -ForegroundColor Yellow
        $mvnArgs += @(
            "-Dgpg.skip=false",
            "-DskipPublishing=false"
        )
    }
}

& .\mvnw.cmd @mvnArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Target -eq "central") {
    Write-Host "Central deploy 已执行（门户侧可能仍在校验/发布）" -ForegroundColor Green
} else {
    Write-Host "内网 deploy 完成" -ForegroundColor Green
}