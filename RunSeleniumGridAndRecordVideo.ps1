$podman = "C:\Users\ad\Documents\podman-5.4.1\usr\bin\podman.exe"

$mvn = "mvn clean test -Dsuite=local"
# Ensure Podman ,
if (-Not (Test-Path $podman)) { Write-Host "Podman not found!"; exit 1 }

# Ensure videos directory exists
if (-not (Test-Path ./videos)) {
    mkdir ./videos | Out-Null
    Write-Host "Created videos directory"
}

# Check if Podman machine is initialized
if (-Not (& $podman machine list --format json | ConvertFrom-Json)) {
    Write-Host "Initializing Podman..."
    & $podman machine init
}

# Ensure Podman is running
if (-Not (& $podman machine list --format json | ConvertFrom-Json | Where-Object { $_.Running })) {
    Write-Host "Starting Podman..."
    & $podman machine start
}

Write-Host "Podman is running!"
# Start Selenium and Video Containers
& $podman --version
& $podman network create grid
& $podman run -d -p 4444:4444 -p 6900:5900 --net grid --name selenium --shm-size=2g selenium/standalone-chrome:4.30.0-20250323
& $podman run -d --net grid --name video -v ./videos:/videos selenium/video:ffmpeg-7.1.1.1-20250323

# Run Tests
Invoke-Expression $mvn

Write-Host "Clean!"
& $podman stop video selenium
& $podman rm video selenium
& $podman network rm grid
# Optional
#  machine stop podman-machine-default
#  machine rm podman-machine-default
# powershell -ep Bypass -f RunSeleniumGridAndRecordVideo.ps1
