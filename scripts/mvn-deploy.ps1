# ========== 发布配置（按需修改）==========
# 目标：internal | central
$DeployTarget = "internal"
$SkipTests = $true

# 内网（DeployTarget = internal）
# settings.xml <server><id> 须与 InternalServerId 一致
$InternalServerId = "internal"
$InternalReleaseUrl = "http://repo.example.com/nexus/content/repositories/releases/"
$InternalSnapshotUrl = "http://repo.example.com/nexus/content/repositories/snapshots/"

# 中央仓库（DeployTarget = central）
# pom 中 publishingServerId=central；settings.xml 须配置 central / GPG
# =========================================

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$mvnArgs = @("clean", "deploy", "-Dmaven.deploy.skip=false")
if ($SkipTests) {
    $mvnArgs += "-DskipTests"
}

switch ($DeployTarget.ToLowerInvariant()) {
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
    default {
        Write-Error "DeployTarget 无效: '$DeployTarget'，请设为 internal 或 central"
    }
}

& .\mvnw.cmd @mvnArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($DeployTarget -eq "central") {
    Write-Host "Central deploy 已执行（门户侧可能仍在校验/发布）" -ForegroundColor Green
} else {
    Write-Host "内网 deploy 完成" -ForegroundColor Green
}
