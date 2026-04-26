$ErrorActionPreference = "Stop"

Write-Host "Creating database..."
appwrite databases create --database-id "artisansx_db" --name "ArtisansX"

Write-Host "Creating user_profiles collection..."
appwrite databases create-collection --database-id "artisansx_db" --collection-id "user_profiles" --name "User Profiles" --document-security true --permissions 'read("users")' 'create("users")' 'update("users")'

Write-Host "Creating userId string attribute..."
appwrite databases create-string-attribute --database-id "artisansx_db" --collection-id "user_profiles" --key "userId" --size 255 --required true

Write-Host "Creating email string attribute..."
appwrite databases create-string-attribute --database-id "artisansx_db" --collection-id "user_profiles" --key "email" --size 255 --required true

Write-Host "Done"
