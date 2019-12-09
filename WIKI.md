# Facebook
Backend REST API for Facebook app.

In our application you have many possibilities. The application consists of several parts.
    
#### Basic parts:

    - mail  
    - auth  
    - posts  
    - events  
    - news  
    - chat  
    
In these parts you have a lot of interesting features.
If you are registered and have an active email, then you have access to all tools.    
    
### Auth       
    To get started, you need to register a user, after that you will recieve email for activation your email address.
    For any actions you need to be logged in and send Authorization header (with token) in every request.  
    User can add/delete another user to/from friends.  
    User can display another user profile.  

### Posts
    You can add, delete, modify posts with and without photo (your photo will save on aws s3).
    You can comment on posts and make likes, unlike.
    During these operation posts are cached.
    
### Events
    You can add, delete events with and without photo (your photo will save on aws s3).
    You can join or invite friends to any existing event.
    
### Chat
    You can join to any existing group or create new one for You and your friends.  
    
### News
     Administrator can add, delete, edit news or get information about that.  
     User can only display last news.
    
     

