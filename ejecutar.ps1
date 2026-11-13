param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('categoria','cliente','producto','pedido')]
    [string]$Servicio,
    [ValidateSet('dev','prod')]
    [string]$Perfil = 'dev'
)
$ErrorActionPreference = 'Stop'
$jdk = $env:JAVA_HOME
if ([string]::IsNullOrWhiteSpace($jdk)) { throw 'Configura JAVA_HOME con la ruta de tu JDK 25.' }
if (-not (Test-Path (Join-Path $jdk 'bin\java.exe'))) { throw 'JAVA_HOME no contiene un JDK valido.' }
if ($Perfil -eq 'prod' -and [string]::IsNullOrWhiteSpace($env:DB_URL)) { throw 'Configura DB_URL localmente para el perfil prod.' }
$previousJava = $env:JAVA_HOME
$previousMaven = $env:MAVEN_USER_HOME
$previousUser = $env:DB_USERNAME
$previousPassword = $env:DB_PASSWORD
try {
    $env:JAVA_HOME = (Resolve-Path $jdk).Path

    if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) { $env:DB_USERNAME = Read-Host 'Usuario MariaDB' }
    if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) { throw 'El usuario es obligatorio.' }
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $secure = Read-Host 'Contraseña MariaDB (no se muestra ni se guarda en archivos)' -AsSecureString
        $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
        try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
        finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr); $secure.Dispose() }
    }
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) { throw 'No se permiten contraseñas vacías.' }
    $project = Join-Path $PSScriptRoot "$Servicio-service"
    Push-Location $project
    try {
        & '.\mvnw.cmd' spring-boot:run "-Dspring-boot.run.profiles=$Perfil"
        if ($LASTEXITCODE -ne 0) { throw "El servicio terminó con código $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:MAVEN_USER_HOME = $previousMaven
    $env:DB_USERNAME = $previousUser
    $env:DB_PASSWORD = $previousPassword
}
