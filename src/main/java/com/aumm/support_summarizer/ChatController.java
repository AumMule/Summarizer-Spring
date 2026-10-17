
package com.aumm.support_summarizer;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/tickets")
public class ChatController {

    private final ChatClient chatClient;


    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }




    @PostMapping("/summarize")
    public TicketSummary summarize(@Valid @RequestBody TicketRequest request) {

        return chatClient.prompt()
                .system("""
                You are a support ticket analyst.
                Analyze the ticket and return:
                - summary: a concise summary of the customer's issue
                - priority: HIGH, MEDIUM, or LOW
                - category: AUTHENTICATION, BILLING, TECHNICAL, or OTHER
                        
                                                                      Set priority using the following rules:
                        
                                                                      - HIGH: The customer cannot use a critical service,
                                                                        has an immediate deadline (within 24 hours),
                                                                        or describes a severe business impact.
                        
                                                                      - MEDIUM: The issue blocks an important task, but
                                                                        the deadline is more than 24 hours away, or the
                                                                        issue persists and needs support.
                        
                                                                      - LOW: Minor inconvenience, general question, or
                                                                        issue with little impact.
                        
                                                                      Important:
                                                                      - Do not classify a ticket as HIGH just because it
                                                                        contains the words "urgent" or "deadline".
                                                                      - Consider the actual time remaining before the deadline.
                                                                      - A deadline several weeks away is not an immediate
                                                                        deadline.
                                                                      - If the deadline is unclear, do not assume it is
                                                                        immediate.
                        

                Return the result in the requested structured format.
                """)
                .user(request.ticketText())
                .call()
                .entity(TicketSummary.class);
    }

}
