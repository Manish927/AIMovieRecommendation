# Add Ratings for User mannu927@yahoo.com

## Steps to Add Ratings and Test Recommendations

### Step 1: Find User ID

After logging in with `mannu927@yahoo.com`, check the browser console (F12) and look for:
- "Parsed user data:" - this will show the user object structure
- "Extracted user ID:" - this will show the user ID

Or check localStorage:
```javascript
// In browser console:
JSON.parse(localStorage.getItem('user'))
```

### Step 2: Add Ratings via API

Once you have the user ID (let's say it's `USER_ID`), add ratings:

```bash
# Rate Movie 1 (The Matrix) - 5 stars
curl -X POST http://localhost:8081/ratings \
  -H "Content-Type: application/json" \
  -d '{"userId": USER_ID, "movieId": 1, "rating": 5}'

# Rate Movie 2 (Inception) - 5 stars
curl -X POST http://localhost:8081/ratings \
  -H "Content-Type: application/json" \
  -d '{"userId": USER_ID, "movieId": 2, "rating": 5}'

# Rate Movie 3 (The Dark Knight) - 4 stars
curl -X POST http://localhost:8081/ratings \
  -H "Content-Type: application/json" \
  -d '{"userId": USER_ID, "movieId": 3, "rating": 4}'

# Rate Movie 4 (Pulp Fiction) - 5 stars
curl -X POST http://localhost:8081/ratings \
  -H "Content-Type: application/json" \
  -d '{"userId": USER_ID, "movieId": 4, "rating": 5}'

# Rate Movie 5 (Interstellar) - 4 stars
curl -X POST http://localhost:8081/ratings \
  -H "Content-Type: application/json" \
  -d '{"userId": USER_ID, "movieId": 5, "rating": 4}'
```

### Step 3: Verify Ratings

```bash
# Check ratings for the user
curl http://localhost:8081/ratings/user/USER_ID
```

### Step 4: Test Recommendations

```bash
# Test recommendation API
curl http://localhost:8083/recommendations/user/USER_ID/hybrid?limit=10
```

### Step 5: Refresh Frontend

1. Refresh the homepage (`http://localhost:4200`)
2. Check browser console for debugging messages
3. Recommendations should appear if:
   - User is logged in
   - User has at least 3 ratings
   - There are other users with ratings (or MovieLens data imported)

## Quick PowerShell Script

Replace `USER_ID` with your actual user ID:

```powershell
$userId = USER_ID  # Replace with your user ID
$baseUrl = "http://localhost:8081"

# Add ratings
$ratings = @(
    @{userId=$userId; movieId=1; rating=5},
    @{userId=$userId; movieId=2; rating=5},
    @{userId=$userId; movieId=3; rating=4},
    @{userId=$userId; movieId=4; rating=5},
    @{userId=$userId; movieId=5; rating=4}
)

foreach ($rating in $ratings) {
    $body = $rating | ConvertTo-Json
    Write-Host "Adding rating: $body"
    Invoke-RestMethod -Uri "$baseUrl/ratings" -Method POST -Body $body -ContentType "application/json"
}

Write-Host "`nRatings added! Check recommendations at:"
Write-Host "http://localhost:8083/recommendations/user/$userId/hybrid?limit=10"
```


