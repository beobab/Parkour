# Parkour

```
/race start [name]
```

This creates a new race (with a name of your choice). Not entering the name will create
one with a name like race23. Step on pressure plates, push and pull levers and buttons
to create waypoints.

```
/race end
```

Call this when you're done creating a race. It will save the most recently stepped on plate/button/levers
as the finish line.

```
/race
```

If you are not yet in a race, this will point your compass to the nearest race.  
If you are in a race, this will tell you information about the current race, including 
how long you've been racing, and the time since the last waypoint. Your compass will 
already be pointing to the next waypoint.

```
/race list
```

If you can't find a race, this will likely help you. Mind you, if people have just called
their races "race1, race2, race3, .. race3829", it might not help at all. It's paged, with
5 races on each page. 

Pull request to add that to the config.

```
/race info
```

This just outputs the race as a string. It's not pretty. It's just "This race is X steps
long, over N waypoints (doesn't include start and finish as waypoints) and between 2 worlds.


[Download just the plugin](out/artifacts/Parkour_jar/Parkour.jar)
