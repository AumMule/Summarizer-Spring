
package com.aumm.support_summarizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotBlank(message = "Ticket text cannot be empty")
        @Size(max = 5000, message = "Ticket text must not exceed 5000 characters")
        String ticketText
) {
}
