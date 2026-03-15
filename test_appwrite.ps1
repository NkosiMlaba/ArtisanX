$ErrorActionPreference = "Stop"

Write-Host "Creating database..."
appwrite databases create --databaseId "artisansx_db" --name "ArtisansX"

Write-Host "Creating user_profiles collection..."
appwrite databases createCollection --databaseId "artisansx_db" --collectionId "user_profiles" --name "User Profiles" --documentSecurity true --permissions 'read("users")' 'create("users")' 'update("users")'

Write-Host "Creating userId string attribute..."
appwrite databases createStringAttribute --databaseId "artisansx_db" --collectionId "user_profiles" --key "userId" --size 255 --required true

Write-Host "Creating email string attribute..."
appwrite databases createStringAttribute --databaseId "artisansx_db" --collectionId "user_profiles" --key "email" --size 255 --required true

Write-Host "Done"
