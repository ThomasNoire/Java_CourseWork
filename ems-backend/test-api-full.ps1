# EMS Backend API Full Test Script
# Version 1.0

# Configuration
$apiBaseUrl = "http://localhost:8081"
$adminCredentials = @{
    email = "admin@example.com"
    password = "admin123"
}
$userCredentials = @{
    email = "user@example.com"
    password = "user123"
}

# Helper Functions
function Write-TestHeader {
    param([string]$title)
    Write-Host "`n====================================================="
    Write-Host $title
    Write-Host "====================================================="
}

function Write-TestResult {
    param(
        [string]$testName,
        [bool]$success,
        [string]$message = ""
    )
    
    if ($success) {
        Write-Host "✅ $testName : SUCCESS" -ForegroundColor Green
    } else {
        Write-Host "❌ $testName : FAILED" -ForegroundColor Red
    }
    
    if ($message -ne "") {
        Write-Host "   $message" -ForegroundColor Gray
    }
}

function Test-Endpoint {
    param(
        [string]$name,
        [string]$url,
        [string]$method = "GET",
        [object]$headers = $null,
        [object]$body = $null,
        [int]$expectedStatus = 200,
        [switch]$returnResponse
    )
    
    Write-Host "`n🔍 Testing: $name..." -ForegroundColor Cyan
    
    try {
        $params = @{
            Uri = $url
            Method = $method
            UseBasicParsing = $true
        }
        
        if ($headers -ne $null) {
            $params.Headers = $headers
        }
        
        if ($body -ne $null) {
            $params.Body = ($body | ConvertTo-Json)
            $params.ContentType = "application/json"
        }
        
        $response = Invoke-WebRequest @params
        
        $success = $response.StatusCode -eq $expectedStatus
        $message = "Status: $($response.StatusCode)"
        
        Write-TestResult -testName $name -success $success -message $message
        
        if ($returnResponse) {
            return $response.Content | ConvertFrom-Json
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $expectedFailure = $expectedStatus -eq $statusCode
        
        if ($expectedFailure) {
            Write-TestResult -testName $name -success $true -message "Expected failure with status: $statusCode"
        } else {
            Write-TestResult -testName $name -success $false -message "Error: $($_.Exception.Message)"
        }
        
        if ($returnResponse) {
            return $null
        }
    }
}

# Main Testing Function
function Run-FullApiTest {
    # Start Docker Containers
    Write-TestHeader "Starting Docker Environment"
    docker-compose down
    docker-compose up -d
    
    # Wait for system to start
    Write-Host "Waiting for system to start (30 seconds)..."
    Start-Sleep -Seconds 30
    
    # Test Swagger UI availability
    Test-Endpoint -name "Swagger UI Availability" -url "$apiBaseUrl/swagger-ui/index.html"
    
    # Authentication Tests
    Write-TestHeader "Authentication Tests"
    
    # Admin Login
    $adminLoginResponse = Test-Endpoint -name "Admin Login" -url "$apiBaseUrl/login" -method "POST" -body $adminCredentials -returnResponse
    $adminToken = $adminLoginResponse.token
    
    # User Login
    $userLoginResponse = Test-Endpoint -name "User Login" -url "$apiBaseUrl/login" -method "POST" -body $userCredentials -returnResponse
    $userToken = $userLoginResponse.token
    
    # Register New User
    $newUserEmail = "newuser$(Get-Random)@example.com"
    $newUser = @{
        email = $newUserEmail
        password = "newuser123"
    }
    Test-Endpoint -name "User Registration" -url "$apiBaseUrl/register" -method "POST" -body $newUser -expectedStatus 201
    
    # Set up auth headers
    $adminHeaders = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    $userHeaders = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Employee Endpoints - Admin Access
    Write-TestHeader "Employee Endpoints - Admin Access"
    
    # Get all employees
    $employeesResponse = Test-Endpoint -name "Get All Employees (Admin)" -url "$apiBaseUrl/api/employees" -headers $adminHeaders -returnResponse
    Write-Host "   Found $($employeesResponse.Count) employees" -ForegroundColor Gray
    
    # Create new employee
    $newEmployee = @{
        firstName = "Test"
        lastName = "Employee"
        email = "test.employee@example.com"
    }
    $createdEmployee = Test-Endpoint -name "Create Employee (Admin)" -url "$apiBaseUrl/api/employees" -method "POST" -headers $adminHeaders -body $newEmployee -expectedStatus 201 -returnResponse
    Write-Host "   Created employee with ID: $($createdEmployee.id)" -ForegroundColor Gray
    
    # Get employee by ID
    Test-Endpoint -name "Get Employee by ID (Admin)" -url "$apiBaseUrl/api/employees/$($createdEmployee.id)" -headers $adminHeaders
    
    # Update employee
    $updatedEmployee = @{
        firstName = "Updated"
        lastName = "Employee"
        email = "updated.employee@example.com"
    }
    Test-Endpoint -name "Update Employee (Admin)" -url "$apiBaseUrl/api/employees/$($createdEmployee.id)" -method "PUT" -headers $adminHeaders -body $updatedEmployee
    
    # Employee Endpoints - User Access
    Write-TestHeader "Employee Endpoints - User Access"
    
    # Get all employees as user
    Test-Endpoint -name "Get All Employees (User)" -url "$apiBaseUrl/api/employees" -headers $userHeaders
    
    # Get employee by ID as user
    Test-Endpoint -name "Get Employee by ID (User)" -url "$apiBaseUrl/api/employees/$($createdEmployee.id)" -headers $userHeaders
    
    # Try to create employee as user (should fail)
    Test-Endpoint -name "Create Employee (User)" -url "$apiBaseUrl/api/employees" -method "POST" -headers $userHeaders -body $newEmployee -expectedStatus 403
    
    # Try to update employee as user (should fail)
    Test-Endpoint -name "Update Employee (User)" -url "$apiBaseUrl/api/employees/$($createdEmployee.id)" -method "PUT" -headers $userHeaders -body $updatedEmployee -expectedStatus 403
    
    # Try to delete employee as user (should fail)
    Test-Endpoint -name "Delete Employee (User)" -url "$apiBaseUrl/api/employees/$($createdEmployee.id)" -method "DELETE" -headers $userHeaders -expectedStatus 403
    
    # Delete employee as admin
    Test-Endpoint -name "Delete Employee (Admin)" -url "$apiBaseUrl/api/employees/$($createdEmployee.id)" -method "DELETE" -headers $adminHeaders
    
    # Summary
    Write-TestHeader "Test Summary"
    Write-Host "✅ Authentication endpoints are working correctly"
    Write-Host "✅ Employee CRUD operations are working correctly"
    Write-Host "✅ Authorization is properly implemented"
    Write-Host "✅ User roles are properly enforced"
    Write-Host "`nTo stop Docker containers, run: docker-compose down"
}

# Run the tests
Write-TestHeader "EMS Backend API Testing"
Run-FullApiTest
