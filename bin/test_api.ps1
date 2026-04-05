# Disable strict TLS if needed
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$baseUrl = "http://localhost:8080/api"

function Log-Output {
    param([string]$message, [string]$color = "White")
    Write-Host $message -ForegroundColor $color
    $message
}

Log-Output "=== 1. Registering Admin ===" "Cyan"
$adminReq = @{
    name = "Admin User 2"
    email = "admin2@example.com"
    password = "password123"
    role = "ADMIN"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $adminReq -ContentType "application/json"
    Log-Output "Admin Registration: $($res.message)"
} catch {
    Log-Output "Admin Registration Failed: $_" "Red"
}

Log-Output "`n=== 2. Registering User ===" "Cyan"
$userReq = @{
    name = "Regular User 2"
    email = "user2@example.com"
    password = "password123"
    role = "USER"
} | ConvertTo-Json
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $userReq -ContentType "application/json"
    Log-Output "User Registration: $($res.message)"
} catch {
    Log-Output "User Registration Failed: $_" "Red"
}

Log-Output "`n=== 3. Login Admin ===" "Cyan"
$adminLogin = @{
    email = "admin2@example.com"
    password = "password123"
} | ConvertTo-Json
$adminToken = ""
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $adminLogin -ContentType "application/json"
    Log-Output "Admin Login: $($res.message)"
    $adminToken = $res.data.token
} catch {
    Log-Output "Admin Login Failed: $_" "Red"
}

Log-Output "`n=== 4. Login User ===" "Cyan"
$userLogin = @{
    email = "user2@example.com"
    password = "password123"
} | ConvertTo-Json
$userToken = ""
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $userLogin -ContentType "application/json"
    Log-Output "User Login: $($res.message)"
    $userToken = $res.data.token
} catch {
    Log-Output "User Login Failed: $_" "Red"
}

Log-Output "`n=== 5. Create Loan (User) ===" "Cyan"
$loanReq = @{
    principalAmount = 10000
    interestRate = 5.5
    durationMonths = 12
} | ConvertTo-Json
$loanId = ""
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/loans/apply" -Method Post -Body $loanReq -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
    Log-Output "Loan Creation: $($res.data.status) - EMI: $($res.data.emiAmount)"
    $loanId = $res.data.id
} catch {
    Log-Output "Loan Creation Failed: $_" "Red"
}

Log-Output "`n=== 6. Approve Loan (Admin) ===" "Cyan"
if ($loanId) {
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/loans/$loanId/status?status=APPROVED" -Method Put -Headers @{Authorization="Bearer $adminToken"}
        Log-Output "Loan Approval: $($res.data.status)"
    } catch {
        Log-Output "Loan Approval Failed: $_" "Red"
    }
}

Log-Output "`n=== 7. Make Payment (User) ===" "Cyan"
if ($loanId) {
    try {
        $paymentReq = @{
            amount = 1000
            paymentMethod = "CREDIT_CARD"
        } | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "$baseUrl/payments/loan/$loanId" -Method Post -Body $paymentReq -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
        Log-Output "Payment Success! Amount: $($res.data.amount) Paid"
    } catch {
        Log-Output "Payment Failed: $_" "Red"
    }
}

Log-Output "`n=== 8. Verify Updated Loan Balance ===" "Cyan"
if ($loanId) {
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/loans/$loanId" -Method Get -Headers @{Authorization="Bearer $userToken"}
        Log-Output "Loan status: $($res.data.status), Remaining Balance: $($res.data.remainingBalance)"
    } catch {
        Log-Output "Loan Fetch Failed: $_" "Red"
    }
}
