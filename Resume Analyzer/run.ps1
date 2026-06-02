param(
    [ValidateSet("local", "mysql")]
    [string]$Database = "local",
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = "",
    [string]$ServerPort = "8080",
    [string]$MysqlUrl = "jdbc:mysql://localhost:3306/resume_analyzer1?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
    [string]$LocalDatabaseUrl = "jdbc:h2:file:./data/resume_analyzer;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
)

$env:SERVER_PORT = $ServerPort

if ($Database -eq "mysql") {
    $env:SPRING_PROFILES_ACTIVE = "mysql"
    $env:MYSQL_URL = $MysqlUrl
    $env:MYSQL_USER = $MysqlUser
    $env:MYSQL_PASSWORD = $MysqlPassword
    Write-Host "Using MySQL database on localhost:3306"
} else {
    $env:SPRING_PROFILES_ACTIVE = ""
    $env:DATABASE_URL = $LocalDatabaseUrl
    $env:DATABASE_USER = "sa"
    $env:DATABASE_PASSWORD = ""
    $env:DATABASE_DRIVER = "org.h2.Driver"
    Write-Host "Using local database at .\data\resume_analyzer.mv.db"
}

$portProcess = Get-NetTCPConnection -LocalPort ([int]$ServerPort) -ErrorAction SilentlyContinue |
    Where-Object { $_.State -eq "Listen" } |
    Select-Object -First 1

if ($portProcess) {
    $owner = Get-CimInstance Win32_Process -Filter "ProcessId = $($portProcess.OwningProcess)"
    if ($owner.CommandLine -like "*com.resumeanalyzer.ResumeAnalyzerApplication*") {
        Write-Host "Resume Analyzer is already running on http://localhost:$ServerPort"
        exit 0
    }

    Write-Host "Port $ServerPort is already used by process $($portProcess.OwningProcess): $($owner.Name)"
    Write-Host "Run with another port, for example: .\run.ps1 -ServerPort 8081"
    exit 1
}

mvn spring-boot:run
