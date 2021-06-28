# Sport News App
Project for Mobile and Cloud Computing's Course 2020-2021 at Sapienza Università di Roma.


## Abstract
Sport News App is an Android mobile application for browsing and searching [The Guardian](https://www.theguardian.com/international)'s news about Formula One, tennis and cycling, saving articles in a list of favorites and sharing them with other users. A Google account is required to interact with the application (Android Lollipop 5.0 at least).


## Tools
* Project written in Kotlin on [Android Studio](https://developer.android.com/studio)
* [Google Sign-In](https://developers.google.com/identity/sign-in/android/sign-in) for authentication
* [Unsplash Image API](https://unsplash.com/developers) for a random wallpaper to show in the login screen
* [The Guardian Open Platform](https://open-platform.theguardian.com/) for retrieving the daily and past news
* My own RESTful Web Service with SQLite database running on [PythonAnywhere](https://jrtaloma.pythonanywhere.com/) for checking, inserting and delivering contents
* [Firebase Realtime Database](https://firebase.google.com/docs/database/android/start) for instantly sharing contents among users


## Some application screens

### Login screen
With a random portrait or landscape wallpaper (depending on the smartphone's orientation).

<img src="https://github.com/jrtaloma/GuardianNewsApp/blob/main/screens/login.jpg" width="200">


### Homepage
From left to right: Formula 1, Tennis, Cycling, Favorites, Share. Search bar for specific keyword and floating button to load more (and older) results.

The user can save an item from Formula 1, Tennis, Cycling and Share in Favorites by long clicking it, as well as for deleting it.

<img src="https://github.com/jrtaloma/GuardianNewsApp/blob/main/screens/homepage.jpg" width="200">


### Webpage
Click an item in the homepage, read the article (optionally in browser) and share it by inserting the email of a subscribed user. Automatic refresh every 5 minutes.

<img src="https://github.com/jrtaloma/GuardianNewsApp/blob/main/screens/webpage.jpg" width="200">


### Notification
The receiver will find a new item on top of the share list. Click on the notification to read the news in the browser.

<img src="https://github.com/jrtaloma/GuardianNewsApp/blob/main/screens/notification.jpg" width="200">
