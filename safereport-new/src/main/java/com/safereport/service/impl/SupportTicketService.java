package com.safereport.service.impl;

import com.safereport.dto.request.SupportTicketRequest;
import com.safereport.entity.SupportTicket;
import com.safereport.entity.User;
import com.safereport.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public SupportTicket createTicket(SupportTicketRequest req, User user) {
        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .subject(req.getSubject())
                .description(req.getDescription())
                .priority(req.getPriority())
                .status("OPEN")
                .build();
        return ticketRepository.save(ticket);
    }

    public Page<SupportTicket> getMyTickets(User user, Pageable pageable) {
        return ticketRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public SupportTicket getTicketById(Long ticketId, User user) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        // Users can only see their own tickets; admins see all
        if (user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !ticket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return ticket;
    }

    public Page<SupportTicket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public SupportTicket respondToTicket(Long ticketId, String response) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setResponse(response);
        ticket.setStatus("RESOLVED");
        ticket.setRespondedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public SupportTicket updateTicketStatus(Long ticketId, String status) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }
}
