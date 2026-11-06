$pgCtl = "C:\Program Files\PostgreSQL\18\bin\pg_ctl.exe"
$createdb = "C:\Program Files\PostgreSQL\18\bin\createdb.exe"
$pgData = "C:\Users\admin\Downloads\Hospital\pgdata"

Write-Host "Starting PostgreSQL on port 54321..."
& $pgCtl -D $pgData -l "$pgData\logfile.log" -o "-p 54321 -h 127.0.0.1" start -w

Write-Host "Creating database hospital_db if not exists..."
& $createdb -U postgres -h 127.0.0.1 -p 54321 hospital_db 2>$null

try {
    Write-Host "Running tests..."
    cd C:\Users\admin\Downloads\Hospital
    & "C:\Program Files\Java\jdk-21\bin\java.exe" "@Hospital\target\run_tests_args.txt"
} finally {
    Write-Host "Stopping PostgreSQL..."
    & $pgCtl -D $pgData stop
}
