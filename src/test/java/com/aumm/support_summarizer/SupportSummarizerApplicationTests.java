
package com.aumm.support_summarizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@WebMvcTest(ChatController.class)
@Import({
		GlobalExceptionHandler.class,
		SupportSummarizerApplicationTests.MockConfig.class
})
class SupportSummarizerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ChatClient chatClient;

	private ChatClient.ChatClientRequestSpec requestSpec;
	private ChatClient.CallResponseSpec responseSpec;

	@TestConfiguration
	static class MockConfig {

		@Bean
		ChatClient chatClient() {
			return mock(ChatClient.class);
		}

		@Bean
		ChatClient.Builder chatClientBuilder(ChatClient chatClient) {
			ChatClient.Builder builder = mock(ChatClient.Builder.class);
			when(builder.build()).thenReturn(chatClient);
			return builder;
		}
	}

	@BeforeEach
	void setUp() {
		clearInvocations(chatClient);
		requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
		responseSpec = mock(ChatClient.CallResponseSpec.class);

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.system(anyString())).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
	}

	@Test
	void blankTicketShouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/tickets/summarize")
						.contentType("application/json")
						.content("""
                        {"ticketText": "   "}
                        """))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Ticket text cannot be empty"));

		verify(chatClient, never()).prompt();
	}

	@Test
	void oversizedTicketShouldReturnBadRequest() throws Exception {
		String ticket = "a".repeat(5001);
		String json = """
            {"ticketText": "%s"}
            """.formatted(ticket);

		mockMvc.perform(post("/api/tickets/summarize")
						.contentType("application/json")
						.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Ticket text must not exceed 5000 characters"));

		verify(chatClient, never()).prompt();
	}

	@Test
	void validTicketShouldReturnStructuredSummary() throws Exception {
		TicketSummary summary = new TicketSummary(
				"Customer cannot log in.",
				"HIGH",
				"AUTHENTICATION"
		);

		when(responseSpec.entity(TicketSummary.class))
				.thenReturn(summary);

		mockMvc.perform(post("/api/tickets/summarize")
						.contentType("application/json")
						.content("""
                        {"ticketText": "I cannot log in and need urgent help."}
                        """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.summary")
						.value("Customer cannot log in."))
				.andExpect(jsonPath("$.priority").value("HIGH"))
				.andExpect(jsonPath("$.category").value("AUTHENTICATION"));

		verify(chatClient).prompt();
	}
}
