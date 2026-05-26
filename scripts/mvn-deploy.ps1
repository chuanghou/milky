param(
    [Parameter(Position = 0)]
    [ValidateSet("internal", "central")]
    [string]$Target,

    [switch]$RunTests
)

function Show-MvnDeployUsage {
    Write-Host @"
用法: .\scripts\mvn-deploy.ps1 <internal|central> [-RunTests]

  internal     发布到内网 Maven 仓库（仓库地址与凭据见本机 ~/.m2/settings.xml）
  central      发布到 Maven Central（须 GPG + Portal User Token）
  -RunTests    默认跳过测试

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

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "未找到 mvn，请确认 Maven 已安装并在 PATH 中"
    exit 1
}

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$mvnArgs = @("clean", "deploy", "-Dmaven.deploy.skip=false")
if (-not $RunTests) {
    $mvnArgs += "-DskipTests"
}

switch ($Target.ToLowerInvariant()) {
    "internal" {
        Write-Host "milky deploy -> 内网 Maven 仓库（使用 settings.xml 中的仓库配置）" -ForegroundColor Cyan
        # 不启用 Central 发布插件；部署目标由 settings.xml（如 altDeploymentRepository 或 distributionManagement）决定
        $mvnArgs += "-Dgpg.skip=true"
    }
    "central" {
        Write-Host "milky deploy -> Maven Central" -ForegroundColor Yellow
        $mvnArgs += @(
            "-Dgpg.skip=false",
            "-DskipPublishing=false",
            "-Dcentral.publishing.phase=deploy"
        )
    }
}

& mvn @mvnArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Target -eq "central") {
    Write-Host "Central deploy 已执行（门户侧可能仍在校验/发布）" -ForegroundColor Green
} else {
    Write-Host "内网 deploy 完成" -ForegroundColor Green
}
