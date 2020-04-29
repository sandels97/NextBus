#NextBus or NextDösä 

This project is the end-project for the "Ohjelmistokehityksen teknogioita" course.

My idea for the project is to develop an Android mobile app that shows the user when the next bus (HSL only) comes to bus stop based on the user's location. Android application is going to use Kotlin as the programming language.

Application is going to use HSL's open [API](https://www.hsl.fi/avoindata) to get bus stop's coordinates and timetables.
Users' bus stop preferences are going to stored to local database with Android Room Persistence library or SQLite.

For now, the scale of the project is only to allow users to search for bus routes or bus stops, and to show nearest or selected bus stop's timetable information. I'm not planning to support searching for the fastest route to destination at least yet.