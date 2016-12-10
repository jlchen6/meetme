# meetme
ECE 454 Capstone Design Project

Viswesh Periyasamy
Yefeng Yuan
Julia Chen


Project Modules


1)	Profile


Display the user’s personal information including photo, name, personal email, and interests.


functions: update each one of the personal information listed above
each function will be able to update and store the latest changes of the information to the server and update the local status as well
a group ID will be generated and loaded to the server


functions: retrieve each one of the personal information listed above
each function will be able to retrieve the the information from the server and store in local device





2)	Server


No UI component, just running code and functions to interact with each client


function: keep track of group details
Each group mapped with unique ID
Keep track of all members (mapped by phone number as unique ID)
When user joins group with ID, add them to list, send them back list of friends, and update existing group members list with new member
Use this list to send out refreshes when people set themselves available, or enter a request
Keep track of previous recommendations to avoid the same recommendation again, don’t blacklist but take into account how recent (MostRecentlyUsed eviction)
function: refresh user’s homepage
Whenever there's new information or information has changed, send update to all group members
Either periodically or on-information-changed callback
Set themselves available
User enters a request
A recommendation is made and needs to be sent out
User’s availability has run out, everyone needs to know
function: make a recommendation
Use locations of all available group members
If user did not have location services on, just don’t use it in algorithm
Map out ideal location for all users using algorithm to find optimal midpoint
Possible implementation: minimizing maximum distance for all
Take into account each user’s preference and iterate through places near optimal location
Requires an algorithm to handle importance of each preference
Rate each place based on how well they meet all expectations
Rank based on the ratings
Send back to participating users through refresh function





3)	Home
Your name & preferences displayed towards the top
Have option to change your food preferences if you feel like eating/not eating certain foods. 
IF TIME: Suggestions
Note that a user is eating a lot at one restaurant/one food and maybe suggest putting that food/restaurant on their list of “no’s”
Display list of friends (sorted by their distance to you if online)
Can be stored in-app and then retrieved locally to lower load times.
Have option to add/remove friends. 
For individual friends, have buttons to ask to meet up with them or send them a text message. (if we are not implementing a messenger function)
These buttons would be displayed next to their name in the list along with their distance from you if they are also online.


Show who’s available (Have online friends towards the top?)
Function: have some sort of “online” status boolean that is sent to server?
Server then retrieves list of friends that have an online status and displays them 
(could have the “online” status boolean have a larger weight when sorting in friends list such that online friends show up at the top)
Show list of groups
Show most recently active groups towards the top
	


	



4)	Request Details


UI can be a pop up or take you to a new activity to enter a request
	When request is done and sent


function: enter a new request
Cohesive list of preferences
Import user preferences to start list of preferences
Allow user to add or delete more preference based on how they are feeling
Maybe have a ‘-’ button by each to delete a preference
Have a ‘+’ button at bottom of list to add more
Each preference can be prefer, dislike, or blacklist (absolutely no)
Blacklist for allergies or deal breakers
Time frame
User specifies a time frame in which they are available
To ensure that people are available at the same time
Send to server
Information sent to server along with notifying everyone that this user is available
Server adds preferences to composite list of preferences used in recommendation algorithm
Request automatically takes user’s location and imports it into list for algorithm
If not specified (location services off) just ignore their location for the recommendation
Should be some kind of cancel or exit button if they don’t want to go through with the request


function: edit an existing request
Forgot a preference or changed mind
Change list of preferences associated with request and send to server
Time frame changed
Can no longer be available for as long, or have more free time now so update server
function: cancel existing request
Remove the user’s preferences and location from all calculation algorithms
Notify all users that this user is no longer currently available



5)	Recommendation


A new activity will show UI of a list of recommended places to the user.


function: click list
specify the place on the list where is clicked and pop up the activity specified for that place with its information such as location, ranking, phone number, etc


function: get information
after the specified activity is chosen, load the information of that place from server


function: get optimized location
design our algorithms to calculate the best place based on its distances to each member and its ranking from previous comments
generate a list with the best place to go at the top
