Remove-Item "server/plugins/btv-*.jar" -Force

Get-ChildItem "target" -Filter *.jar |
Sort-Object LastWriteTime -Descending |
Select-Object -First 1 |
Copy-Item -Destination "server/plugins" -Force

Push-Location
Set-Location "server"
java -Xmx2G -jar "paper-1.21.10-129.jar" nogui
Pop-Location