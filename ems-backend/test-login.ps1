# Simple test script for login
$loginData = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json

Write-Host "Testing login endpoint..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method POST -Body $loginData -ContentType "application/json" -UseBasicParsing
    Write-Host "Login successful. Status: $($response.StatusCode)"
    Write-Host "Response: $($response.Content)"
} catch {
    Write-Host "Login failed: $_"
}
