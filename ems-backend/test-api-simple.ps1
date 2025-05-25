# Simple API test script
Write-Host "====================================================="
Write-Host "Testing EMS Backend API"
Write-Host "====================================================="

# Test 1: Login as admin
Write-Host "`nTest 1: Login as admin"
$loginData = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method POST -Body $loginData -ContentType "application/json" -UseBasicParsing
    Write-Host "Login successful. Status: $($loginResponse.StatusCode)"
    $adminToken = ($loginResponse.Content | ConvertFrom-Json).token
    Write-Host "Admin token obtained."
} catch {
    Write-Host "Admin login failed: $_"
    exit
}

# Test 2: Get all employees as admin
Write-Host "`nTest 2: Get all employees as admin"
$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

try {
    $employeesResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Get employees successful. Status: $($employeesResponse.StatusCode)"
    $employees = $employeesResponse.Content | ConvertFrom-Json
    Write-Host "Employees retrieved: $($employees.Count)"
} catch {
    Write-Host "Get employees failed: $_"
}

# Test 3: Create a new employee
Write-Host "`nTest 3: Create a new employee"
$newEmployee = @{
    firstName = "Test"
    lastName = "Employee"
    email = "test.employee@example.com"
} | ConvertTo-Json

try {
    $createResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method POST -Body $newEmployee -UseBasicParsing
    Write-Host "Create employee successful. Status: $($createResponse.StatusCode)"
    $createdEmployee = $createResponse.Content | ConvertFrom-Json
    Write-Host "Employee created with ID: $($createdEmployee.id)"
} catch {
    Write-Host "Create employee failed: $_"
}

# Test 4: Register a new user
Write-Host "`nTest 4: Register a new user"
$newUser = @{
    email = "newuser$(Get-Random)@example.com"
    password = "newuser123"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-WebRequest -Uri "http://localhost:8081/register" -Method POST -Body $newUser -ContentType "application/json" -UseBasicParsing
    Write-Host "Register user successful. Status: $($registerResponse.StatusCode)"
    $registeredUser = $registerResponse.Content | ConvertFrom-Json
    Write-Host "User registered: $($registeredUser.email)"
} catch {
    Write-Host "Register user failed: $_"
}

# Test 5: Update an employee
Write-Host "`nTest 5: Update an employee"
if ($createdEmployee.id) {
    $updatedEmployee = @{
        firstName = "Updated"
        lastName = "Employee"
        email = "updated.employee@example.com"
    } | ConvertTo-Json

    try {
        $updateResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$($createdEmployee.id)" -Headers $headers -Method PUT -Body $updatedEmployee -UseBasicParsing
        Write-Host "Update employee successful. Status: $($updateResponse.StatusCode)"
        $updatedData = $updateResponse.Content | ConvertFrom-Json
        Write-Host "Employee updated: $($updatedData.firstName) $($updatedData.lastName)"
    } catch {
        Write-Host "Update employee failed: $_"
    }
} else {
    Write-Host "Skipping update test as no employee was created."
}

# Test 6: Delete an employee
Write-Host "`nTest 6: Delete an employee"
if ($createdEmployee.id) {
    try {
        $deleteResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$($createdEmployee.id)" -Headers $headers -Method DELETE -UseBasicParsing
        Write-Host "Delete employee successful. Status: $($deleteResponse.StatusCode)"
        Write-Host "Response: $($deleteResponse.Content)"
    } catch {
        Write-Host "Delete employee failed: $_"
    }
} else {
    Write-Host "Skipping delete test as no employee was created."
}

Write-Host "`n====================================================="
Write-Host "Testing completed!"
Write-Host "====================================================="
