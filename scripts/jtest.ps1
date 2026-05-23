param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$JtestArgs
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

if ($JtestArgs.Count -eq 0) {
    & uv run --project test python test/jtest.py
} else {
    & uv run --project test python test/jtest.py @JtestArgs
}
exit $LASTEXITCODE
