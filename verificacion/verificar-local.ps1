param([string]$Usuario='lab11',[switch]$EntradaGrafica)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$out=Join-Path $root ('evidencias\'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $out -Force | Out-Null
Set-Content -LiteralPath (Join-Path $root 'evidencias\ultima-ejecucion.txt') -Value $out -Encoding UTF8
function Status([string]$Estado,[string]$Mensaje){
    @{estado=$Estado;mensaje=$Mensaje;fecha=(Get-Date).ToString('o')} | ConvertTo-Json | Set-Content (Join-Path $out 'estado.json') -Encoding UTF8
}
$oldUser=$env:DB_USERNAME;$oldPassword=$env:DB_PASSWORD
if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) { throw 'Configura JAVA_HOME con la ruta de tu JDK 25.' }
$java=Join-Path $env:JAVA_HOME 'bin\java.exe'
$driver=Get-ChildItem -Path (Join-Path $env:USERPROFILE '.m2\repository\org\mariadb\jdbc\mariadb-java-client') -Filter '*.jar' -Recurse | Select-Object -First 1 -ExpandProperty FullName
try {
    $env:DB_USERNAME=$Usuario
    Status 'esperando-credenciales' 'Introduce la cuenta local en esta ventana; no la compartas por chat.'
    Write-Host 'LAB11 - Verificacion real de MariaDB y REST' -ForegroundColor Cyan
    Write-Host 'No se cambian contraseñas ni se borran bases. Se crean y eliminan solo registros nuevos de prueba.'
    if([string]::IsNullOrWhiteSpace($env:DB_USERNAME)){
        $entered=Read-Host 'Usuario MariaDB [root]'
        $env:DB_USERNAME=if([string]::IsNullOrWhiteSpace($entered)){'root'}else{$entered}
    }
    if([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)){
        if($EntradaGrafica){
            Add-Type -AssemblyName System.Windows.Forms
            Add-Type -AssemblyName System.Drawing
            $form=New-Object System.Windows.Forms.Form
            $form.Text='LAB11 - Contrasena local de MariaDB'
            $form.ClientSize=New-Object System.Drawing.Size(440,165)
            $form.StartPosition='CenterScreen';$form.TopMost=$true
            $label=New-Object System.Windows.Forms.Label
            $label.Text="Usuario: $Usuario. Introduce la contrasena existente."
            $label.SetBounds(15,15,410,25)
            $box=New-Object System.Windows.Forms.TextBox
            $box.UseSystemPasswordChar=$true;$box.SetBounds(15,50,410,26)
            $ok=New-Object System.Windows.Forms.Button
            $ok.Text='Continuar';$ok.SetBounds(230,105,95,30);$ok.DialogResult='OK'
            $cancel=New-Object System.Windows.Forms.Button
            $cancel.Text='Cancelar';$cancel.SetBounds(330,105,95,30);$cancel.DialogResult='Cancel'
            $form.Controls.AddRange(@($label,$box,$ok,$cancel));$form.AcceptButton=$ok;$form.CancelButton=$cancel
            $form.Add_Shown({$box.Focus()})
            try {
                if($form.ShowDialog() -ne 'OK'){throw 'Entrada local cancelada. No se intento autenticar.'}
                $env:DB_PASSWORD=$box.Text
                $box.Clear()
            } finally {$form.Dispose()}
        }else{
            $secure=Read-Host 'Contraseña MariaDB (oculta, no se guarda)' -AsSecureString
            $ptr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
            try{$env:DB_PASSWORD=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)}finally{[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr);$secure.Dispose()}
        }
    }
    if([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)){throw 'No se intentó conexión: la contraseña introducida está vacía.'}
    $env:DB_HOST='localhost';$env:DB_PORT='3306'
    Status 'comprobando-mariadb' 'Consulta autenticada de las cuatro bases existentes.'
    & $java --class-path $driver (Join-Path $PSScriptRoot 'DatabaseProbe.java') (Join-Path $out 'mariadb-antes.json')
    if($LASTEXITCODE -ne 0){throw "Falló la autenticación o verificación de bases (código $LASTEXITCODE). No se iniciaron los servicios."}
    foreach($port in 8081,8082,8083,8084){
        if(Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue){throw "El puerto $port está ocupado. No se reemplazará su proceso."}
    }
    Status 'iniciando-servicios' 'Ejecutando ejecutar.ps1 para cada servicio.'
    $started=@()
    foreach($name in 'cliente','categoria','producto','pedido'){
        $args=@('-NoProfile','-ExecutionPolicy','RemoteSigned','-File',('"'+(Join-Path $root 'ejecutar.ps1')+'"'),'-Servicio',$name)
        $p=Start-Process -FilePath 'powershell.exe' -ArgumentList $args -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $out "$name.stdout.log") -RedirectStandardError (Join-Path $out "$name.stderr.log")
        $started+=@{servicio=$name;launcherPid=$p.Id}
    }
    $started | ConvertTo-Json | Set-Content (Join-Path $out 'procesos-iniciados.json') -Encoding UTF8
    $targets=@(@{name='cliente';port=8081;api='clientes'},@{name='categoria';port=8082;api='categorias'},@{name='producto';port=8083;api='productos'},@{name='pedido';port=8084;api='pedidos'})
    $deadline=(Get-Date).AddMinutes(4)
    $ready=@{}
    do {
        foreach($t in $targets){
            if(-not $ready.ContainsKey($t.name)){
                try{$response=Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:$($t.port)/api/$($t.api)" -TimeoutSec 3;if($response.StatusCode -eq 200){$ready[$t.name]=$true;Write-Host "$($t.name): HTTP 200 en $($t.port)"}}catch{}
            }
        }
        if($ready.Count -eq 4){break}
        Start-Sleep -Seconds 2
    }while((Get-Date) -lt $deadline)
    $targets | ForEach-Object {[pscustomobject]@{servicio=$_.name;puerto=$_.port;http200=$ready.ContainsKey($_.name);base=('bd_'+$_.name)}} | ConvertTo-Json | Set-Content (Join-Path $out 'servicios.json') -Encoding UTF8
    if($ready.Count -ne 4){throw "Solo $($ready.Count) de 4 servicios respondieron. Revisar registros en $out"}
    Status 'probando-rest' 'Pruebas contra los servicios reales; limpieza limitada a los IDs creados en esta ejecución.'
    & (Join-Path $PSScriptRoot 'pruebas-rest.ps1') -Salida (Join-Path $out 'respuestas-rest.json')
    & $java --class-path $driver (Join-Path $PSScriptRoot 'DatabaseProbe.java') (Join-Path $out 'mariadb-despues.json')
    if($LASTEXITCODE -ne 0){throw 'No se pudo obtener la comprobación final de bases.'}
    $before=Get-Content (Join-Path $out 'mariadb-antes.json') -Raw | ConvertFrom-Json
    $after=Get-Content (Join-Path $out 'mariadb-despues.json') -Raw | ConvertFrom-Json
    $changes=@()
    foreach($db in $before.databases){
        $current=$after.databases | Where-Object database -eq $db.database
        foreach($table in $db.tables.PSObject.Properties){
            if($current.tables.($table.Name) -ne $table.Value){$changes+="$($db.database).$($table.Name)"}
        }
    }
    if($changes.Count){throw ('Cambió el número de filas de tablas preexistentes: '+($changes -join ', '))}
    $tests=Get-Content (Join-Path $out 'respuestas-rest.json') -Raw | ConvertFrom-Json
    Status 'completado' "$($tests.Count) solicitudes REST correctas. Cuatro servicios activos. Filas preexistentes conservadas."
    Write-Host "Verificacion completada. Evidencias: $out" -ForegroundColor Green
} catch {
    Status 'error' $_.Exception.Message
    Write-Host $_.Exception.Message -ForegroundColor Red
} finally {
    $env:DB_USERNAME=$oldUser;$env:DB_PASSWORD=$oldPassword
    Write-Host 'Los servicios iniciados permanecen abiertos; las credenciales no se han escrito en archivos.'
    if(-not $EntradaGrafica){Read-Host 'Pulsa Enter para cerrar esta ventana' | Out-Null}
}
