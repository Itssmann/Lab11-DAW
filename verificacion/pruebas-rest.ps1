param([Parameter(Mandatory=$true)][string]$Salida)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$client.Timeout = [TimeSpan]::FromSeconds(20)
$records = New-Object 'System.Collections.Generic.List[object]'
$owned = New-Object 'System.Collections.Generic.List[object]'
$run = Get-Date -Format 'yyyyMMdd-HHmmss'
function Call-Api([string]$Caso,[string]$Metodo,[string]$Url,$Body,[int]$Esperado) {
    $request = New-Object System.Net.Http.HttpRequestMessage
    $request.Method = New-Object System.Net.Http.HttpMethod($Metodo)
    $request.RequestUri = [Uri]$Url
    $json = $null
    if ($null -ne $Body) {
        $json = ConvertTo-Json -InputObject $Body -Depth 20 -Compress
        $request.Content = New-Object System.Net.Http.StringContent($json,[Text.Encoding]::UTF8,'application/json')
    }
    $watch = [Diagnostics.Stopwatch]::StartNew()
    try {
        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        $text = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $status = [int]$response.StatusCode
        $parsed = if($text){try {ConvertFrom-Json $text} catch {$text}} else {$null}
        # Congelar el cuerpo: los casos siguientes modifican sus propios datos de prueba.
        $bodySnapshot=if($json){ConvertFrom-Json $json}else{$null}
        $evidenceResponse=if($Caso.EndsWith(': listar')){@{cantidad=@($parsed).Count;nota='Se omiten registros ajenos para no copiar datos existentes a la evidencia.'}}else{$parsed}
        $records.Add([pscustomobject]@{caso=$Caso;fecha=(Get-Date).ToString('o');metodo=$Metodo;url=$Url;solicitud=$bodySnapshot;esperado=$Esperado;estado=$status;correcto=($status -eq $Esperado);duracionMs=$watch.ElapsedMilliseconds;respuesta=$evidenceResponse})
        if($status -ne $Esperado){throw "$Caso : esperado $Esperado; recibido $status"}
        return $parsed
    } finally {$request.Dispose()}
}
try {
    $specs=@(
        @{name='categoria';port=8082;plural='categorias';body=@{nombre="Lab11-$run";estado='ACTIVO'}},
        @{name='cliente';port=8081;plural='clientes';body=@{nombre="Lab11-$run";apellidos='Prueba controlada';direccion='Direccion de prueba';telefono='987654321';nroDni='12345678';estado='ACTIVO'}},
        @{name='producto';port=8083;plural='productos';body=@{nombre="Lab11-$run";precio=89.90;stock=15;categoriaId=0}},
        @{name='pedido';port=8084;plural='pedidos';body=@{fecha=(Get-Date -Format 'yyyy-MM-dd');idCliente=0;total=179.80;detalles=@(@{idProducto=0;cantidad=2;precio=89.90})}}
    )
    $ids=@{}
    foreach($s in $specs){
        $base="http://localhost:$($s.port)/api/$($s.plural)"
        if($s.name -eq 'producto'){$s.body.categoriaId=$ids.categoria}
        if($s.name -eq 'pedido'){$s.body.idCliente=$ids.cliente;$s.body.detalles[0].idProducto=$ids.producto}
        $created=Call-Api "$($s.name): crear" 'POST' $base $s.body 201
        $id=if($s.name -eq 'pedido'){$created.idPedido}else{$created.id}
        if(-not $id){throw 'La respuesta no contiene el ID creado.'}
        $ids[$s.name]=$id
        $owned.Add(@{name=$s.name;url="$base/$id"})
        $null=Call-Api "$($s.name): listar" 'GET' $base $null 200
        $null=Call-Api "$($s.name): obtener" 'GET' "$base/$id" $null 200
        if($s.name -eq 'pedido'){$s.body.total=269.70;$s.body.detalles[0].cantidad=3}else{$s.body.nombre+=' actualizado'}
        $updated=Call-Api "$($s.name): actualizar" 'PUT' "$base/$id" $s.body 200
        $read=Call-Api "$($s.name): persistencia de actualización" 'GET' "$base/$id" $null 200
        if($s.name -eq 'pedido'){
            if([decimal]$read.total -ne [decimal]269.70 -or $read.detalles[0].cantidad -ne 3){throw 'No se persistió la actualización del pedido.'}
            $invalid=@{fecha=$s.body.fecha;idCliente=$ids.cliente;total=1;detalles=$s.body.detalles}
            $null=Call-Api 'pedido: total incoherente' 'POST' $base $invalid 400
            $s.body.total=89.90;$s.body.detalles=@(@{idProducto=$ids.producto;cantidad=1;precio=89.90})
            $null=Call-Api 'pedido: reemplazar detalles' 'PUT' "$base/$id" $s.body 200
            $read=Call-Api 'pedido: consultar detalles reemplazados' 'GET' "$base/$id" $null 200
            if(@($read.detalles).Count -ne 1 -or [decimal]$read.total -ne [decimal]89.90){throw 'No se persistió el reemplazo de detalles.'}
        } elseif($read.nombre -ne $s.body.nombre){throw 'No se persistió la actualización.'}
        $null=Call-Api "$($s.name): campos obligatorios" 'POST' $base @{} 400
        $null=Call-Api "$($s.name): ID inválido" 'GET' "$base/abc" $null 400
        $null=Call-Api "$($s.name): no existe" 'GET' "$base/9223372036854775807" $null 404
        $null=Call-Api "$($s.name): actualizar inexistente" 'PUT' "$base/9223372036854775807" $s.body 404
        $null=Call-Api "$($s.name): eliminar inexistente" 'DELETE' "$base/9223372036854775807" $null 404
    }
} finally {
    # Solo se eliminan IDs devueltos por POST satisfactorios en ESTA ejecución.
    for($i=$owned.Count-1;$i -ge 0;$i--){
        try {
            $null=Call-Api "$($owned[$i].name): eliminar registro de prueba" 'DELETE' $owned[$i].url $null 204
            $null=Call-Api "$($owned[$i].name): confirmar eliminación" 'GET' $owned[$i].url $null 404
        } catch {Write-Warning $_.Exception.Message}
    }
    $client.Dispose()
    $records | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $Salida -Encoding UTF8
}
$failed=@($records | Where-Object {-not $_.correcto})
if($failed.Count){throw "$($failed.Count) respuestas inesperadas. Consultar la evidencia."}
Write-Host "$($records.Count) solicitudes REST verificadas correctamente."
