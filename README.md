#Web Scraper v0.1 (WS v0.1) 
Back-End Skills Test

# Running the Application

1. Clone the source code
2. Open project on IDE (I used IntelliJ)
3. Make sure all libraries are imported (imported HTMLUnit in my project) and maven imports
4. Make sure you have google service key and google api authentication available on your local and path (THIS IS A MUST)
5. Make sure that Google Cloud Vision API is properly setup on your end and all pre-requisites are enabled for Google vision to work (THIS IS A MUST)
6. Build Yelp Scraper Application
7. Run Application

# Accessing my application

Note: Created 2 Approaches in accessing my Application

* Approach 1 (GET method)

  1. Run Application
  2. Open Browser
  3. enter "http://localhost:8080/" to access GET method of Application
  4. The parameter used here is hard coded
  
* Approach 2 (POST method) (@Param restaurantName)

  1. Run Application
  2. Open Browser
  3. Open RESTClient plugin or any REST Web Client in browser (Used Mozilla RESTClient)
  4. Set Request Method to POST
  5. Set headers user:user and Content-Type: application/json
  6. Set URL to http://localhost:8080/api/cws/yelp/get
  7. Enter Request Body copy JSON below:
  
  {
  
  "restaurantName" : "lazy bastard makati"
  
  {
  
  8. Hit Send
