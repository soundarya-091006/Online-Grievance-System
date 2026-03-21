package com.safereport.controller;

import com.safereport.dto.request.SupportTicketRequest;
import com.safereport.dto.response.ApiResponse;
import com.safereport.entity.SupportTicket;
import com.safereport.entity.User;
import com.safereport.service.impl.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<SupportTicket>> create(
            @Valid @RequestBody SupportTicketRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.success("Ticket created", ticketService.createTicket(req, user)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<SupportTicket>>> myTickets(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(ticketService.getMyTickets(user, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupportTicket>> getTicket(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketById(id, user)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<SupportTicket>>> allTickets(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(ticketService.getAllTickets(pageable)));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupportTicket>> respond(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                ApiResponse.success("Response sent",
                        ticketService.respondToTicket(id, body.get("response"))));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SupportTicket>> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                ApiResponse.success("Status updated",
                        ticketService.updateTicketStatus(id, body.get("status"))));
    }
}
