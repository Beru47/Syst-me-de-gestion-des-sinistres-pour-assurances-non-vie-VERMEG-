package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.service.ExpertService;
import com.vermeg.sinistpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;
    private final UserService userService;

    @Autowired
    public ExpertController(ExpertService expertService, UserService userService) {
        this.expertService = expertService;
        this.userService = userService;
    }

    // Create a new expert with user account and ROLE_EXPERT
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Expert> createExpert(@RequestBody ExpertSignupRequest signupRequest) {
        // Create a user with ROLE_EXPERT
        User user = new User(signupRequest.getUsername(), signupRequest.getPassword(), signupRequest.getEmail());
        User savedUser = userService.signup(user, "ROLE_EXPERT", null); // Pass null since ExpertSignupRequest handles expert details

        // The signup method in UserService creates an Expert entity linked to the user
        Expert expert = expertService.findExpertById(savedUser.getId()); // Assumes expert ID matches user ID

        // Update expert details from the signup request
        expert.setNom(signupRequest.getNom());
        expert.setSpecialite(signupRequest.getSpecialite());
        expert.setContact(signupRequest.getTelephone());
        expert.setLocation(signupRequest.getAdresse());

        Expert updatedExpert = expertService.saveExpert(expert);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedExpert);
    }

    // Get expert by ID
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Expert> getExpertById(@PathVariable Long id) {
        Expert expert = expertService.findExpertById(id);
        return ResponseEntity.ok(expert);
    }

    // Get experts by specialization
    @GetMapping("/specialite/{specialite}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Expert>> getExpertsBySpecialite(@PathVariable String specialite) {
        List<Expert> experts = expertService.findExpertsBySpecialite(specialite);
        return ResponseEntity.ok(experts);
    }

    // Update an existing expert
    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Expert> updateExpert(@PathVariable Long id, @RequestBody Expert expert) {
        Expert existingExpert = expertService.findExpertById(id);
        existingExpert.setNom(expert.getNom());
        existingExpert.setSpecialite(expert.getSpecialite());
        existingExpert.setContact(expert.getContact());
        existingExpert.setLocation(expert.getLocation());
        Expert updatedExpert = expertService.saveExpert(existingExpert);
        return ResponseEntity.ok(updatedExpert);
    }

    // Get claims assigned to an expert
    @GetMapping("/{expertId}/claims")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<List<Sinistre>> getAssignedClaims(@PathVariable Long expertId) {
        List<Sinistre> claims = expertService.getAssignedClaims(expertId);
        return ResponseEntity.ok(claims);
    }

    // Get details of a specific claim
    @GetMapping("/{expertId}/claims/{sinistreId}")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Sinistre> getClaimDetails(@PathVariable Long expertId, @PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(expertId, sinistreId);
        return ResponseEntity.ok(sinistre);
    }

    // Submit a report for a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/report")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Report> submitReport(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                               @RequestBody String content) {
        Report report = expertService.writeReport(expertId, sinistreId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    // Schedule a meeting for a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/meeting")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Meeting> scheduleMeeting(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                                   @RequestBody MeetingRequest meetingRequest) {
        Meeting meeting = expertService.scheduleMeeting(expertId, sinistreId, meetingRequest.getMeetingType(),
                meetingRequest.getMeetingDate(), meetingRequest.getRequestedLocation());
        return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
    }

    // Approve a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/approve")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Void> approveClaim(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                             @RequestBody String approvalNotes) {
        expertService.approveClaim(expertId, sinistreId, approvalNotes);
        return ResponseEntity.ok().build();
    }

    // Trigger expert auto-assignment for a claim
    @PostMapping("/assign/{sinistreId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignExpertToSinistre(@PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(null, sinistreId);
        Map<String, Object> assignmentResult = expertService.assignExpertToSinistreWithDetails(sinistre);
        return ResponseEntity.ok(assignmentResult);
    }

    // Get expert workload calculation
    @GetMapping("/{expertId}/workload")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExpertWorkload(@PathVariable Long expertId) {
        Map<String, Object> workloadDetails = expertService.calculateExpertWorkloadWithDetails(expertId);
        return ResponseEntity.ok(workloadDetails);
    }

    // Get assignment model analytics - for admin/debugging
    @GetMapping("/analytics/assignments")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAssignmentAnalytics() {
        Map<String, Object> analytics = expertService.getAssignmentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Preview assignment scores for a claim without making assignment
    @GetMapping("/preview-assignment/{sinistreId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> previewAssignmentScores(@PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(null, sinistreId);
        List<Map<String, Object>> scoringResults = expertService.previewExpertScores(sinistre);
        return ResponseEntity.ok(scoringResults);
    }

    // DTO for meeting request
    static class MeetingRequest {
        private Meeting.MeetingType meetingType;
        private LocalDateTime meetingDate;
        private String requestedLocation;

        public Meeting.MeetingType getMeetingType() {
            return meetingType;
        }

        public void setMeetingType(Meeting.MeetingType meetingType) {
            this.meetingType = meetingType;
        }

        public LocalDateTime getMeetingDate() {
            return meetingDate;
        }

        public void setMeetingDate(LocalDateTime meetingDate) {
            this.meetingDate = meetingDate;
        }

        public String getRequestedLocation() {
            return requestedLocation;
        }

        public void setRequestedLocation(String requestedLocation) {
            this.requestedLocation = requestedLocation;
        }
    }
}

/*package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.service.ExpertService;
import com.vermeg.sinistpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;
    private final UserService userService;  // Add UserService

    @Autowired
    public ExpertController(ExpertService expertService, UserService userService) {
        this.expertService = expertService;
        this.userService = userService;
    }

    // Get expert by ID
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Expert> getExpertById(@PathVariable Long id) {
        Expert expert = expertService.findExpertById(id);
        return ResponseEntity.ok(expert);
    }

    // Get experts by specialization
    @GetMapping("/specialite/{specialite}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Expert>> getExpertsBySpecialite(@PathVariable String specialite) {
        List<Expert> experts = expertService.findExpertsBySpecialite(specialite);
        return ResponseEntity.ok(experts);
    }

    // Create a new expert with user account and ROLE_EXPERT
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Expert> createExpert(@RequestBody ExpertRegistrationRequest request) {
        // First create a user with ROLE_EXPERT
        User user = new User(request.getUsername(), request.getPassword(), request.getEmail());
        User savedUser = userService.signup(user, "ROLE_EXPERT", null);

        // Then create the expert linked to this user
        Expert expert = new Expert();
        expert.setNom(request.getNom());
        expert.setSpecialite(request.getSpecialite());
        expert.setContact(request.getContact());
        expert.setLocation(request.getLocation());
        expert.setUser(savedUser);  // Link the expert to the newly created user

        Expert savedExpert = expertService.saveExpert(expert);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpert);
    }

    // Update an existing expert
    @PutMapping("/{id}")
    //  @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Expert> updateExpert(@PathVariable Long id, @RequestBody Expert expert) {
        Expert existingExpert = expertService.findExpertById(id);
        existingExpert.setNom(expert.getNom());
        existingExpert.setSpecialite(expert.getSpecialite());
        existingExpert.setContact(expert.getContact());
        existingExpert.setLocation(expert.getLocation());
        Expert updatedExpert = expertService.saveExpert(existingExpert);
        return ResponseEntity.ok(updatedExpert);
    }

    // Get claims assigned to an expert
    @GetMapping("/{expertId}/claims")
    //  @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<List<Sinistre>> getAssignedClaims(@PathVariable Long expertId) {
        List<Sinistre> claims = expertService.getAssignedClaims(expertId);
        return ResponseEntity.ok(claims);
    }

    // Get details of a specific claim
    @GetMapping("/{expertId}/claims/{sinistreId}")
    // @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Sinistre> getClaimDetails(@PathVariable Long expertId, @PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(expertId, sinistreId);
        return ResponseEntity.ok(sinistre);
    }

    // Submit a report for a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/report")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Report> submitReport(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                               @RequestBody String content) {
        Report report = expertService.writeReport(expertId, sinistreId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    // Schedule a meeting for a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/meeting")
    //  @PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Meeting> scheduleMeeting(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                                   @RequestBody MeetingRequest meetingRequest) {
        Meeting meeting = expertService.scheduleMeeting(expertId, sinistreId, meetingRequest.getMeetingType(),
                meetingRequest.getMeetingDate(), meetingRequest.getRequestedLocation());
        return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
    }

    // Approve a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/approve")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Void> approveClaim(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                             @RequestBody String approvalNotes) {
        expertService.approveClaim(expertId, sinistreId, approvalNotes);
        return ResponseEntity.ok().build();
    }

    // Trigger expert auto-assignment for a claim
    @PostMapping("/assign/{sinistreId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignExpertToSinistre(@PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(null, sinistreId); // Null expertId to bypass expert check
        Map<String, Object> assignmentResult = expertService.assignExpertToSinistreWithDetails(sinistre);
        return ResponseEntity.ok(assignmentResult);
    }

    // Get expert workload calculation
    @GetMapping("/{expertId}/workload")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExpertWorkload(@PathVariable Long expertId) {
        Map<String, Object> workloadDetails = expertService.calculateExpertWorkloadWithDetails(expertId);
        return ResponseEntity.ok(workloadDetails);
    }

    // Get assignment model analytics - for admin/debugging
    @GetMapping("/analytics/assignments")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAssignmentAnalytics() {
        Map<String, Object> analytics = expertService.getAssignmentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Preview assignment scores for a claim without making assignment
    @GetMapping("/preview-assignment/{sinistreId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> previewAssignmentScores(@PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(null, sinistreId);
        List<Map<String, Object>> scoringResults = expertService.previewExpertScores(sinistre);
        return ResponseEntity.ok(scoringResults);
    }

    // DTO for meeting request
    static class MeetingRequest {
        private Meeting.MeetingType meetingType;
        private LocalDateTime meetingDate;
        private String requestedLocation;

        public Meeting.MeetingType getMeetingType() {
            return meetingType;
        }

        public void setMeetingType(Meeting.MeetingType meetingType) {
            this.meetingType = meetingType;
        }

        public LocalDateTime getMeetingDate() {
            return meetingDate;
        }

        public void setMeetingDate(LocalDateTime meetingDate) {
            this.meetingDate = meetingDate;
        }

        public String getRequestedLocation() {
            return requestedLocation;
        }

        public void setRequestedLocation(String requestedLocation) {
            this.requestedLocation = requestedLocation;
        }
    }

    // DTO for expert registration
    public static class ExpertRegistrationRequest {
        private String username;
        private String password;
        private String email;
        private String nom;
        private String specialite;
        private String contact;
        private String location;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getSpecialite() {
            return specialite;
        }

        public void setSpecialite(String specialite) {
            this.specialite = specialite;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}*/


/*package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Expert;
import com.vermeg.sinistpro.model.Meeting;
import com.vermeg.sinistpro.model.Report;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.service.ExpertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experts")
public class ExpertController {

    private final ExpertService expertService;

    @Autowired
    public ExpertController(ExpertService expertService) {
        this.expertService = expertService;
    }

    // Get expert by ID
    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Expert> getExpertById(@PathVariable Long id) {
        Expert expert = expertService.findExpertById(id);
        return ResponseEntity.ok(expert);
    }

    // Get experts by specialization
    @GetMapping("/specialite/{specialite}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Expert>> getExpertsBySpecialite(@PathVariable String specialite) {
        List<Expert> experts = expertService.findExpertsBySpecialite(specialite);
        return ResponseEntity.ok(experts);
    }

    // Create a new expert
    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Expert> createExpert(@RequestBody Expert expert) {
        Expert savedExpert = expertService.saveExpert(expert);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpert);
    }

    // Update an existing expert
    @PutMapping("/{id}")
    //  @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Expert> updateExpert(@PathVariable Long id, @RequestBody Expert expert) {
        Expert existingExpert = expertService.findExpertById(id);
        existingExpert.setNom(expert.getNom());
        existingExpert.setSpecialite(expert.getSpecialite());
        existingExpert.setContact(expert.getContact());
        existingExpert.setLocation(expert.getLocation());
        Expert updatedExpert = expertService.saveExpert(existingExpert);
        return ResponseEntity.ok(updatedExpert);
    }

    // Get claims assigned to an expert
    @GetMapping("/{expertId}/claims")
    //  @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<List<Sinistre>> getAssignedClaims(@PathVariable Long expertId) {
        List<Sinistre> claims = expertService.getAssignedClaims(expertId);
        return ResponseEntity.ok(claims);
    }

    // Get details of a specific claim
    @GetMapping("/{expertId}/claims/{sinistreId}")
    // @PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Sinistre> getClaimDetails(@PathVariable Long expertId, @PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(expertId, sinistreId);
        return ResponseEntity.ok(sinistre);
    }

    // Submit a report for a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/report")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Report> submitReport(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                               @RequestBody String content) {
        Report report = expertService.writeReport(expertId, sinistreId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    // Schedule a meeting for a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/meeting")
    //  @PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Meeting> scheduleMeeting(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                                   @RequestBody MeetingRequest meetingRequest) {
        Meeting meeting = expertService.scheduleMeeting(expertId, sinistreId, meetingRequest.getMeetingType(),
                meetingRequest.getMeetingDate(), meetingRequest.getRequestedLocation());
        return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
    }

    // Approve a claim
    @PostMapping("/{expertId}/claims/{sinistreId}/approve")
    //@PreAuthorize("hasRole('EXPERT')")
    public ResponseEntity<Void> approveClaim(@PathVariable Long expertId, @PathVariable Long sinistreId,
                                             @RequestBody String approvalNotes) {
        expertService.approveClaim(expertId, sinistreId, approvalNotes);
        return ResponseEntity.ok().build();
    }

    // Trigger expert auto-assignment for a claim
    @PostMapping("/assign/{sinistreId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignExpertToSinistre(@PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(null, sinistreId); // Null expertId to bypass expert check
        Map<String, Object> assignmentResult = expertService.assignExpertToSinistreWithDetails(sinistre);
        return ResponseEntity.ok(assignmentResult);
    }

    // Get expert workload calculation
    @GetMapping("/{expertId}/workload")
    //@PreAuthorize("hasRole('EXPERT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExpertWorkload(@PathVariable Long expertId) {
        Map<String, Object> workloadDetails = expertService.calculateExpertWorkloadWithDetails(expertId);
        return ResponseEntity.ok(workloadDetails);
    }

    // Get assignment model analytics - for admin/debugging
    @GetMapping("/analytics/assignments")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAssignmentAnalytics() {
        Map<String, Object> analytics = expertService.getAssignmentAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // Preview assignment scores for a claim without making assignment
    @GetMapping("/preview-assignment/{sinistreId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> previewAssignmentScores(@PathVariable Long sinistreId) {
        Sinistre sinistre = expertService.getClaimDetails(null, sinistreId);
        List<Map<String, Object>> scoringResults = expertService.previewExpertScores(sinistre);
        return ResponseEntity.ok(scoringResults);
    }

    // DTO for meeting request
    static class MeetingRequest {
        private Meeting.MeetingType meetingType;
        private LocalDateTime meetingDate;
        private String requestedLocation;

        public Meeting.MeetingType getMeetingType() {
            return meetingType;
        }

        public void setMeetingType(Meeting.MeetingType meetingType) {
            this.meetingType = meetingType;
        }

        public LocalDateTime getMeetingDate() {
            return meetingDate;
        }

        public void setMeetingDate(LocalDateTime meetingDate) {
            this.meetingDate = meetingDate;
        }

        public String getRequestedLocation() {
            return requestedLocation;
        }

        public void setRequestedLocation(String requestedLocation) {
            this.requestedLocation = requestedLocation;
        }
    }
}*/