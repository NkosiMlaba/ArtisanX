@echo off
set db=tablesdb

echo Creating collections and attributes...

appwrite databases create-collection --database-id %db% --collection-id "user_profiles" --name "user_profiles" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "userId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "fullName" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "email" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "phone" --size 50 --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "addresses" --size 1000 --required false --array true
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "profileImageId" --size 255 --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "user_profiles" --key "role" --size 50 --required true
appwrite databases create-datetime-attribute --database-id %db% --collection-id "user_profiles" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "artisan_profiles" --name "artisan_profiles" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "userId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "fullName" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "email" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "phone" --size 50 --required true
appwrite databases create-boolean-attribute --database-id %db% --collection-id "artisan_profiles" --key "isStudent" --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "institutionName" --size 255 --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "studentNumber" --size 255 --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "studentCardFileId" --size 255 --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "courseField" --size 255 --required false
appwrite databases create-integer-attribute --database-id %db% --collection-id "artisan_profiles" --key "gradYear" --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "idFileId" --size 255 --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "tradeCategory" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "skills" --size 1000 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "serviceArea" --size 255 --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "artisan_profiles" --key "serviceRadiusKm" --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "artisan_profiles" --key "latitude" --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "artisan_profiles" --key "longitude" --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "workPhotoIds" --size 255 --required false --array true
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "certifications" --size 1000 --required false
appwrite databases create-integer-attribute --database-id %db% --collection-id "artisan_profiles" --key "yearsExperience" --required false
appwrite databases create-boolean-attribute --database-id %db% --collection-id "artisan_profiles" --key "verified" --required true --default false
appwrite databases create-string-attribute --database-id %db% --collection-id "artisan_profiles" --key "badge" --size 255 --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "artisan_profiles" --key "avgRating" --required true --default 0
appwrite databases create-integer-attribute --database-id %db% --collection-id "artisan_profiles" --key "reviewCount" --required true --default 0
appwrite databases create-datetime-attribute --database-id %db% --collection-id "artisan_profiles" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "jobs" --name "jobs" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "customerId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "title" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "category" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "description" --size 5000 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "photoIds" --size 255 --required false --array true
appwrite databases create-float-attribute --database-id %db% --collection-id "jobs" --key "latitude" --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "jobs" --key "longitude" --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "address" --size 1000 --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "jobs" --key "budget" --required false
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "urgency" --size 50 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "jobType" --size 50 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "status" --size 50 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "jobs" --key "assignedArtisanId" --size 255 --required false
appwrite databases create-datetime-attribute --database-id %db% --collection-id "jobs" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "bids" --name "bids" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "bids" --key "jobId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "bids" --key "artisanId" --size 255 --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "bids" --key "priceOffer" --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "bids" --key "message" --size 1000 --required true
appwrite databases create-float-attribute --database-id %db% --collection-id "bids" --key "estimatedHours" --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "bids" --key "status" --size 50 --required true
appwrite databases create-datetime-attribute --database-id %db% --collection-id "bids" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "bookings" --name "bookings" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "bookings" --key "jobId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "bookings" --key "customerId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "bookings" --key "artisanId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "bookings" --key "status" --size 50 --required true
appwrite databases create-datetime-attribute --database-id %db% --collection-id "bookings" --key "startedAt" --required false
appwrite databases create-datetime-attribute --database-id %db% --collection-id "bookings" --key "completedAt" --required false
appwrite databases create-boolean-attribute --database-id %db% --collection-id "bookings" --key "isPaid" --required true --default false
appwrite databases create-datetime-attribute --database-id %db% --collection-id "bookings" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "chat_messages" --name "chat_messages" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "chat_messages" --key "bookingId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "chat_messages" --key "senderId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "chat_messages" --key "message" --size 4000 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "chat_messages" --key "imageFileId" --size 255 --required false
appwrite databases create-datetime-attribute --database-id %db% --collection-id "chat_messages" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "reviews" --name "reviews" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "reviews" --key "bookingId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "reviews" --key "customerId" --size 255 --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "reviews" --key "artisanId" --size 255 --required true
appwrite databases create-integer-attribute --database-id %db% --collection-id "reviews" --key "rating" --required true
appwrite databases create-string-attribute --database-id %db% --collection-id "reviews" --key "comment" --size 4000 --required true
appwrite databases create-datetime-attribute --database-id %db% --collection-id "reviews" --key "createdAt" --required true

appwrite databases create-collection --database-id %db% --collection-id "credits" --name "credits" --document-security true --permissions "read(\"users\")" "create(\"users\")" "update(\"users\")"
appwrite databases create-string-attribute --database-id %db% --collection-id "credits" --key "artisanId" --size 255 --required true
appwrite databases create-integer-attribute --database-id %db% --collection-id "credits" --key "balance" --required true --default 5
appwrite databases create-datetime-attribute --database-id %db% --collection-id "credits" --key "lastUpdated" --required true

echo Waiting 10 seconds for attributes...
timeout /t 10 /nobreak > nul

echo Creating indexes...
appwrite databases create-index --database-id %db% --collection-id "jobs" --key "status" --type "key" --attributes "status" 
appwrite databases create-index --database-id %db% --collection-id "jobs" --key "category" --type "key" --attributes "category"
appwrite databases create-index --database-id %db% --collection-id "jobs" --key "customerId" --type "key" --attributes "customerId"
appwrite databases create-index --database-id %db% --collection-id "jobs" --key "status_category" --type "key" --attributes "status" "category"

appwrite databases create-index --database-id %db% --collection-id "bids" --key "jobId" --type "key" --attributes "jobId"
appwrite databases create-index --database-id %db% --collection-id "bids" --key "artisanId" --type "key" --attributes "artisanId"
appwrite databases create-index --database-id %db% --collection-id "bids" --key "job_artisan" --type "unique" --attributes "jobId" "artisanId"

appwrite databases create-index --database-id %db% --collection-id "bookings" --key "customerId" --type "key" --attributes "customerId"
appwrite databases create-index --database-id %db% --collection-id "bookings" --key "artisanId" --type "key" --attributes "artisanId"
appwrite databases create-index --database-id %db% --collection-id "bookings" --key "jobId" --type "key" --attributes "jobId"

appwrite databases create-index --database-id %db% --collection-id "chat_messages" --key "bookingId" --type "key" --attributes "bookingId"
appwrite databases create-index --database-id %db% --collection-id "chat_messages" --key "booking_created" --type "key" --attributes "bookingId" "createdAt"

appwrite databases create-index --database-id %db% --collection-id "reviews" --key "artisanId" --type "key" --attributes "artisanId"
appwrite databases create-index --database-id %db% --collection-id "reviews" --key "bookingId" --type "unique" --attributes "bookingId"

appwrite databases create-index --database-id %db% --collection-id "artisan_profiles" --key "tradeCategory" --type "key" --attributes "tradeCategory"
appwrite databases create-index --database-id %db% --collection-id "artisan_profiles" --key "userId" --type "unique" --attributes "userId"

appwrite databases create-index --database-id %db% --collection-id "user_profiles" --key "userId" --type "unique" --attributes "userId"

appwrite databases create-index --database-id %db% --collection-id "credits" --key "artisanId" --type "unique" --attributes "artisanId"

echo Created everything!
