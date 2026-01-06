package dk.zlatan.flotmand.Features.my_events.navigaiton

sealed class MyEventsDestination {
    object MyEvents : MyEventsDestination()
    object AddEvent : MyEventsDestination()
}

