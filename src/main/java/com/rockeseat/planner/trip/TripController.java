package com.rockeseat.planner.trip;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rockeseat.planner.participant.ParticipantService;

@RestController
@RequestMapping("/trips")
public class TripController {

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

		this.participantService.registerParticipantsToEvent(payLoad.emails_to_invite(), newTrip.getId());

		return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
	       Optional <Trip> trip = this.repository.findById(id);
	       return trip.map(ResponseEntity::ok).orElseGet(()-> ResponseEntity.notFound().build());
	       
	}

}
