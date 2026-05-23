param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("dev", "release")]
    [string]$Mode
)

$ErrorActionPreference = "Stop"
$TestDir = Split-Path -Parent $PSScriptRoot
$Pyproject = Join-Path $TestDir "pyproject.toml"
$JtestlibPath = Join-Path $TestDir "..\..\jtestlib"
$JtestlibPyproject = Join-Path $JtestlibPath "pyproject.toml"

$DevSource = 'jtestlib = { path = "../../jtestlib", editable = true }'
$ReleaseSource = 'jtestlib = { git = "https://gitee.com/chuanghou/jtestlib.git" }'

if ($Mode -eq "dev") {
    if (-not (Test-Path $JtestlibPyproject)) {
        Write-Error @"
Local jtestlib not found. Clone beside milky:
  git clone https://gitee.com/chuanghou/jtestlib.git $JtestlibPath
"@
    }
    $content = Get-Content $Pyproject -Raw -Encoding UTF8
    $content = $content -replace 'jtestlib = \{ git = "https://gitee.com/chuanghou/jtestlib.git" \}', $DevSource
    $content = $content -replace 'jtestlib = \{ path = "\.\./\.\./jtestlib", editable = true \}', $DevSource
    Set-Content -Path $Pyproject -Value $content -Encoding UTF8 -NoNewline
    if (Test-Path (Join-Path $TestDir "uv.toml")) {
        Remove-Item (Join-Path $TestDir "uv.toml") -Force
    }
    Write-Host "Mode: dev (local editable, pyproject.toml [tool.uv.sources])"
    Write-Host "jtestlib: $(Resolve-Path $JtestlibPath)"
} else {
    $content = Get-Content $Pyproject -Raw -Encoding UTF8
    $content = $content -replace 'jtestlib = \{ path = "\.\./\.\./jtestlib", editable = true \}', $ReleaseSource
    $content = $content -replace 'jtestlib = \{ git = "https://gitee.com/chuanghou/jtestlib.git" \}', $ReleaseSource
    Set-Content -Path $Pyproject -Value $content -Encoding UTF8 -NoNewline
    if (Test-Path (Join-Path $TestDir "uv.toml")) {
        Remove-Item (Join-Path $TestDir "uv.toml") -Force
    }
    Write-Host "Mode: release (git source from pyproject.toml)"
}

Set-Location $TestDir
uv sync
uv run python -c "import jtestlib; print(jtestlib.__file__)"
