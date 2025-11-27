package dk.zlatan.flotmand.Features.frontpage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.zlatan.flotmand.Features.frontpage.model.Event
import javax.inject.Inject

@HiltViewModel
class FrontPageViewModel @Inject constructor() : ViewModel() {

    val eventList: List<Event> = Event.staticTestEvents
}