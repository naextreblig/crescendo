# Crescendo Web Scraper v0.1 (CWS v0.1) 
Back-End Skills Test

# Running the Application

1. Clone the source code
2. Open project on IDE (I used IntelliJ)
3. Make sure all libraries are imported (imported HTMLUnit in my project) and maven imports
4. Make sure that Google Cloud Vision API is properly set up on your end and is enabled for Google vision to work (THIS IS A MUST)
  * make sure you have service key avaiable on your local and path
5. Build Yelp Scraper Application
6. Run Application

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
