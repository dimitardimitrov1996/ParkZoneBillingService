package softuni.parkzonebillingservice.controller.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import softuni.parkzonebillingservice.config.ApiKeyAuthenticationFilter;
import softuni.parkzonebillingservice.config.SecurityConfiguration;
import softuni.parkzonebillingservice.model.dto.invoice.CreateInvoiceRequest;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.model.entity.InvoiceStatus;
import softuni.parkzonebillingservice.service.invoice.InvoiceService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(InvoiceController.class)
@Import({SecurityConfiguration.class, ApiKeyAuthenticationFilter.class})
@TestPropertySource(properties = "billing.service.api-key=test-api-key")
class InvoiceControllerTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "test-api-key";
    private static final String INVALID_API_KEY = "wrong-api-key";

    @MockitoBean
    private InvoiceService invoiceService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID invoiceId;
    private UUID reservationId;
    private UUID userId;

    private CreateInvoiceRequest createInvoiceRequest;
    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        createInvoiceRequest = CreateInvoiceRequest.builder()
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .build();

        invoiceResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Test
    void createInvoice_whenApiKeyIsValidAndRequestIsValid_shouldReturnCreatedInvoice() throws Exception {
        when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                .thenReturn(invoiceResponse);

        mockMvc.perform(post("/api/v1/invoices")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createInvoiceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.reservationId").value(reservationId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.amount").value(240))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(invoiceService).createInvoice(any(CreateInvoiceRequest.class));
    }

    @Test
    void createInvoice_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {
        CreateInvoiceRequest invalidRequest = CreateInvoiceRequest.builder()
                .reservationId(null)
                .userId(null)
                .amount(BigDecimal.valueOf(-5))
                .currency("")
                .build();

        mockMvc.perform(post("/api/v1/invoices")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.reservationId").value("Reservation id is required"))
                .andExpect(jsonPath("$.fieldErrors.userId").value("User id is required"))
                .andExpect(jsonPath("$.fieldErrors.amount").value("Amount must be positive"))
                .andExpect(jsonPath("$.fieldErrors.currency").value("Currency is required"));
    }

    @Test
    void getInvoiceByReservationId_whenApiKeyIsValid_shouldReturnInvoice() throws Exception {
        when(invoiceService.getInvoiceByReservationId(reservationId))
                .thenReturn(invoiceResponse);

        mockMvc.perform(get("/api/v1/invoices/reservation/{reservationId}", reservationId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.reservationId").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void payInvoice_whenApiKeyIsValid_shouldReturnPaidInvoice() throws Exception {
        InvoiceResponse paidInvoice = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.PAID)
                .createdOn(LocalDateTime.now())
                .paidOn(LocalDateTime.now())
                .build();

        when(invoiceService.payInvoice(invoiceId))
                .thenReturn(paidInvoice);

        mockMvc.perform(put("/api/v1/invoices/{invoiceId}/pay", invoiceId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidOn").exists());
    }

    @Test
    void cancelInvoiceByReservationId_whenApiKeyIsValid_shouldReturnCancelledInvoice() throws Exception {
        InvoiceResponse cancelledInvoice = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.CANCELLED)
                .createdOn(LocalDateTime.now())
                .cancelledOn(LocalDateTime.now())
                .build();

        when(invoiceService.cancelInvoiceByReservationId(reservationId))
                .thenReturn(cancelledInvoice);

        mockMvc.perform(put("/api/v1/invoices/reservation/{reservationId}/cancel", reservationId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledOn").exists());
    }

    @Test
    void request_whenApiKeyIsMissing_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/reservation/{reservationId}", reservationId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Missing API Key header!"));
    }

    @Test
    void request_whenApiKeyIsInvalid_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/invoices/reservation/{reservationId}", reservationId)
                        .header(API_KEY_HEADER, INVALID_API_KEY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Invalid API Key!"));
    }

}
