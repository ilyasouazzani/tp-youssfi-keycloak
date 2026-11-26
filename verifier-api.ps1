$ErrorActionPreference = 'Stop'
$api = 'http://127.0.0.1:8083'
$public = Invoke-WebRequest "$api/public" -SkipHttpErrorCheck -TimeoutSec 5
$anonymous = Invoke-WebRequest "$api/me" -SkipHttpErrorCheck -TimeoutSec 5
if ($public.StatusCode -ne 200 -or $anonymous.StatusCode -ne 401) {
  throw "Codes inattendus : public=$($public.StatusCode), anonyme=$($anonymous.StatusCode)"
}
Write-Output 'Route publique (200) et accès anonyme refusé (401) vérifiés.'
