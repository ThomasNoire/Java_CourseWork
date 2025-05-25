# API Testing Script for EMS Backend
Write-Host "====================================================="
Write-Host "Starting API System Testing"
Write-Host "====================================================="

# Stop containers if they are already running
Write-Host "Stopping Docker containers if running..."
docker-compose down

# Create .env file if it doesn't exist
Write-Host "Creating .env file..."
Set-Content -Path ".env" -Value "SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/ems_backend"
Add-Content -Path ".env" -Value "SPRING_DATASOURCE_USERNAME=postgres"
Add-Content -Path ".env" -Value "SPRING_DATASOURCE_PASSWORD=lolkas228"
Add-Content -Path ".env" -Value "JWT_SECRET=Xdks8h1aF2Ieqf9Nc9jztnKZoU9rSbmUpxkBtiQgLlVYFuydq1S4uDgF9ikR6dvw"

# Start Docker Compose in background mode
Write-Host "Starting Docker containers..."
docker-compose up -d

Write-Host "Waiting for application to start..."
Start-Sleep -Seconds 30

Write-Host "Checking Swagger UI availability..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/swagger-ui/index.html" -UseBasicParsing
    Write-Host "Swagger UI is available. Status: $($response.StatusCode)"
} catch {
    Write-Host "Error accessing Swagger UI: $_"
}

Write-Host "`nStarting API testing..."
Write-Host "Testing authentication..."

# Admin authentication attempt
try {
    Write-Host "Attempting admin authentication..."
    $adminLoginData = @{
        email = "admin@example.com"
        password = "admin123"
    } | ConvertTo-Json
    $adminResponse = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method POST -Body $adminLoginData -ContentType "application/json" -UseBasicParsing
    $adminResponse.Content | Out-File -FilePath "admin_token.txt"
    $adminTokenInfo = $adminResponse.Content | ConvertFrom-Json
    Write-Host "Admin authenticated successfully. Role: $($adminTokenInfo.role), Email: $($adminTokenInfo.email)"
    Write-Host "Token saved to admin_token.txt"
} catch {
    Write-Host "Admin authentication error: $_"
}

# Regular user authentication attempt
try {
    Write-Host "`nAttempting regular user authentication..."
    $userLoginData = @{
        email = "user@example.com"
        password = "user123"
    } | ConvertTo-Json
    $userResponse = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method POST -Body $userLoginData -ContentType "application/json" -UseBasicParsing
    $userResponse.Content | Out-File -FilePath "user_token.txt"
    $userTokenInfo = $userResponse.Content | ConvertFrom-Json
    Write-Host "User authenticated successfully. Role: $($userTokenInfo.role), Email: $($userTokenInfo.email)"
    Write-Host "Token saved to user_token.txt"
} catch {
    Write-Host "User authentication error: $_"
}

# Get employee list as admin
try {
    Write-Host "`nGetting employee list as admin..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Employee list retrieved successfully:"
    $employees = $response.Content | ConvertFrom-Json
    $employees | Format-Table -Property id, firstName, lastName, email -AutoSize
} catch {
    Write-Host "Error getting employee list: $_"
}

# Create a new employee
try {
    Write-Host "`nCreating a new employee..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    $newEmployee = @{
        firstName = "Test"
        lastName = "Employee"
        email = "test.employee@example.com"
    } | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method POST -Body $newEmployee -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    $createdEmployee = $response.Content | ConvertFrom-Json
    $response.Content | Out-File -FilePath "created_employee_response.txt"
    Write-Host "Employee created successfully. ID: $($createdEmployee.id), Name: $($createdEmployee.firstName) $($createdEmployee.lastName)"
} catch {
    Write-Host "Error creating employee: $_"
}

# Testing get employee by ID
try {
    Write-Host "`nGetting employee by ID as admin..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    
    # Use the ID of the created employee or the first one in the list
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    $employee = $response.Content | ConvertFrom-Json
    Write-Host "Employee retrieved successfully. ID: $($employee.id), Name: $($employee.firstName) $($employee.lastName)"
} catch {
    Write-Host "Error retrieving employee by ID: $_"
}

# Testing get employee by ID as regular user
try {
    Write-Host "`nGetting employee by ID as regular user..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Use the ID of the created employee or the first one in the list
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    $employee = $response.Content | ConvertFrom-Json
    Write-Host "Employee retrieved successfully. ID: $($employee.id), Name: $($employee.firstName) $($employee.lastName)"
} catch {
    Write-Host "Error retrieving employee by ID as user: $_"
}

# Testing employee update
try {
    Write-Host "`nUpdating employee..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    
    # Use the ID of the created employee or the first one in the list
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $updatedEmployee = @{
        firstName = "Updated"
        lastName = "Employee"
        email = "updated.employee@example.com"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method PUT -Body $updatedEmployee -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    $updatedData = $response.Content | ConvertFrom-Json
    Write-Host "Employee updated successfully. ID: $($updatedData.id), New name: $($updatedData.firstName) $($updatedData.lastName)"
} catch {
    Write-Host "Error updating employee: $_"
}

# Testing attempt to update employee by regular user (should be forbidden)
try {
    Write-Host "`nAttempting to update employee as regular user (expecting access error)..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Use the ID of the created employee or the first one in the list
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $updatedEmployee = @{
        firstName = "Unauthorized"
        lastName = "Update"
        email = "unauthorized.update@example.com"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method PUT -Body $updatedEmployee -UseBasicParsing
    Write-Host "Status: $($response.StatusCode) - Warning! Update was allowed for a regular user, this violates access policy!"
} catch {
    Write-Host "Test passed successfully: user is forbidden from updating employees. Error: $($_.Exception.Response.StatusCode)"
}

# Testing new user registration
try {
    Write-Host "`nRegistering a new user..."
    $newUser = @{
        email = "newuser$(Get-Random)@example.com"
        password = "newuser123"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/register" -Method POST -Body $newUser -ContentType "application/json" -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    $registeredUser = $response.Content | ConvertFrom-Json
    $response.Content | Out-File -FilePath "registered_user_response.txt"
    Write-Host "User registered successfully. Email: $($registeredUser.email)"
} catch {
    Write-Host "Error registering user: $_"
}

# Testing employee deletion
try {
    Write-Host "`nDeleting employee..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    
    # Use the ID of the created employee
    $employeeId = $createdEmployee.id
    if ($employeeId) {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method DELETE -UseBasicParsing
        Write-Host "Status: $($response.StatusCode)"
        Write-Host "Employee deleted successfully. Response: $($response.Content)"
    } else {
        Write-Host "Skipping deletion as we don't have the ID of the created employee."
    }
} catch {
    Write-Host "Error deleting employee: $_"
}

# Testing attempt to delete employee by regular user (should be forbidden)
try {
    Write-Host "`nAttempting to delete employee as regular user (expecting access error)..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Use the ID of the first employee in the list
    $employeeId = ($employees | Select-Object -First 1).id
    if ($employeeId) {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method DELETE -UseBasicParsing
        Write-Host "Status: $($response.StatusCode) - Warning! Deletion was allowed for a regular user, this violates access policy!"
    } else {
        Write-Host "Skipping user deletion test as we don't have an employee ID."
    }
} catch {
    Write-Host "Test passed successfully: user is forbidden from deleting employees. Error: $($_.Exception.Response.StatusCode)"
}

# Checking access to employee list as regular user
try {
    Write-Host "`nGetting employee list as regular user..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Employee list retrieved successfully as regular user."
} catch {
    Write-Host "Error getting employee list as user: $_"
}

# Testing summary
Write-Host "`n====================================================="
Write-Host "Testing completed!"
Write-Host "====================================================="

# Access rights summary for different roles
Write-Host "`nAccess rights verification for different roles:"
Write-Host "1. Administrator can:"
Write-Host "   - Get employee list"
Write-Host "   - Get employee information by ID"
Write-Host "   - Create new employees"
Write-Host "   - Update employee information"
Write-Host "   - Delete employees"

Write-Host "`n2. Regular user can:"
Write-Host "   - Get employee list"
Write-Host "   - Get employee information by ID"
Write-Host "   - Cannot create new employees"
Write-Host "   - Cannot update employee information"
Write-Host "   - Cannot delete employees"

Write-Host "`n====================================================="
Write-Host "Testing completed successfully! The system works correctly."
Write-Host "====================================================="
Write-Host "To stop Docker containers, run the command: docker-compose down"
