package com.rockeseat.planner.trip;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rockeseat.planner.activity.ActivityData;
import com.rockeseat.planner.activity.ActivityRequestPayLoad;
import com.rockeseat.planner.activity.ActivityResponse;
import com.rockeseat.planner.activity.ActivityService;
import com.rockeseat.planner.link.LinkData;
import com.rockeseat.planner.link.LinkRequestPayLoad;
import com.rockeseat.planner.link.LinkResponse;
import com.rockeseat.planner.link.LinkService;
import com.rockeseat.planner.participant.ParticipantCreateResponse;
import com.rockeseat.planner.participant.ParticipantData;
import com.rockeseat.planner.participant.ParticipantRequestPayLoad;
import com.rockeseat.planner.participant.ParticipantService;


@RestController
@RequestMapping("/trips")
public class TripController {
	
	 @Autowired
	 private ActivityService activityService;

	@Autowired
	private LinkService linkService;

	@Autowired
	private ParticipantService participantService;

	private TripRepository repository;

	@Autowired
	public TripController(TripRepository repository) {
		this.repository = repository;
	}

	@PostMapping
	public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayLoad payLoad) {
		Trip newTrip = new Trip(payLoad);
		this.repository.save(newTrip);

		this.participantService.registerParticipantsToEvent(payLoad.emails_to_invite(), newTrip);

		return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
		Optional<Trip> trip = this.repository.findById(id);
		return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

	}

	@PutMapping("/{id}")
	public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayLoad payLoad) {
		Optional<Trip> trip = this.repository.findById(id);

		if (trip.isPresent()) {
			Trip rawTrip = trip.get();
			rawTrip.setEnds_at(LocalDateTime.parse(payLoad.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
			rawTrip.setStartsAt(LocalDateTime.parse(payLoad.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
			rawTrip.setDestination(payLoad.destination());

			this.repository.save(rawTrip);
			return ResponseEntity.ok(rawTrip);
		}

		return ResponseEntity.notFound().build();
	}

	@GetMapping("/{id}/confirm")
	public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
		Optional<Trip> trip = this.repository.findById(id);

		if (trip.isPresent()) {
			Trip rawTrip = trip.get();
			rawTrip.setConfirmed(true);

			this.repository.save(rawTrip);
			this.participantService.triggerConfirmationEmailToParticipant(id);
			return ResponseEntity.ok(rawTrip);
		}

		return ResponseEntity.notFound().build();
	}
	
	 @PostMapping("/{id}/activities")
	    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayLoad payload) {

	        Optional<Trip> trip = this.repository.findById(id);

	        if(trip.isPresent()){
	            Trip rawTrip = trip.get();
	     
	            ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);

	            return ResponseEntity.ok(activityResponse);
	        }
	        return ResponseEntity.notFound().build();
	        
	    }


	    @GetMapping("/{id}/activities")
	    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id){
	        List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(id);

	        return ResponseEntity.ok(activityDataList);
	    }
	
	
	@PostMapping("/{id}/invite")
	public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayLoad payLoad) {
		Optional<Trip> trip = this.repository.findById(id);

		if (trip.isPresent()) {
			Trip rawTrip = trip.get();
			
 
			
			
			ParticipantCreateResponse participantResponse =  this.participantService.registerParticipantToEvent(payLoad.email(), rawTrip);
			
			if(rawTrip.isConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payLoad.email());
			return ResponseEntity.ok(participantResponse);
		}

		return ResponseEntity.notFound().build();
	}
	
	@GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);

        return ResponseEntity.ok(participantList);
    }
	
	 @PostMapping("/{id}/links")
	    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayLoad payload) {

	        Optional<Trip> trip = this.repository.findById(id);

	        if(trip.isPresent()){
	            Trip rawTrip = trip.get();
	 
	            
	            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);

	            
	            return ResponseEntity.ok(linkResponse);
	        }
	        return ResponseEntity.notFound().build();
	        
	    }

	    @GetMapping("/{id}/links")
	    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id){
	        List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

	        return ResponseEntity.ok(linkDataList);
	    }
	    
	    
	   
	
	

}
