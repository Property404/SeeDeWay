# SeeDeWay
An app to locate your buddy using augmented reality


User Story: 

Client I opens app: 

System
Client I phone number is retrieved from the system (if possible), or is input into a text form and submitted.  On retrieval/ submission [unique_id (most likely phone number), location parameters (tbd), online status, ...(tbd)] are sent to the server and is stored/updated in a database.  

UI
Client I sees a submission form with a camera backdrop. On submission, client see camera view and a phone number search bar at the top of canvas. 

Client J opens app: see above

Client I requests to see a Client J: a request is sent to the server to get the location of the phone in order to visualize: If Client J is not online, [return not_online_message]. If Client J is online, return location of Client J to Client I (also once basic functionality is reached, consider asking first before sending), vice versa, and visualize for both.

## Objectives

### Natalie ( UI ) 
- Create form (a text box and a button) for the user to input their phone number when the app starts. 
- Have the form save the data to a variable that can be manipulat

### Dagan ( Client Side: Integrating Positioning Data )
- Determine accuracy of positioning
- Integrate position data in a format that can be rendered as an AR visuluation 

### Tare'
- Create AR camera view. 
- Learn API documentation to render visualization
- Work with Dagan to to determine how to render visualization from available position data

### Wade
- Create Database
- Create Dataabse Tables ( Work with Dagan to figure out what tables need to exist) 
      - So far: phone_number, location_parameters, online
- Create function(s) to fufill POST requests that update database when they are sent from Clients. 
- Create funciton(s) of GET requests that will be sent to clients.  

### Ben
- Learn to create visualization with ARcore
- Create function to render visualization

### Joe
- Create POST, GET requests to be sent to the server on actions: [phone_number_retrieved, search_for_friend,  ] 
-  Create continous (RESTful?) data stream from client to server/Create Listeners to recieve response from server: [friend_accessed_location]

 


