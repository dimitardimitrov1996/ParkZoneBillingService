package softuni.parkzonebillingservice.mapper.invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.model.entity.Invoice;
import softuni.parkzonebillingservice.model.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceMapperTest {

    private InvoiceMapper invoiceMapper;

    @BeforeEach
    void setUp() {
        invoiceMapper = new InvoiceMapper();
    }

    @Test
    void mapToResponse_shouldMapInvoiceToInvoiceResponse() {
        UUID invoiceId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime paidOn = LocalDateTime.now().plusMinutes(5);
        LocalDateTime cancelledOn = LocalDateTime.now().plusMinutes(10);

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.PAID)
                .createdOn(createdOn)
                .paidOn(paidOn)
                .cancelledOn(cancelledOn)
                .build();

        InvoiceResponse result = invoiceMapper.mapToResponse(invoice);

        assertEquals(invoiceId, result.getId());
        assertEquals(reservationId, result.getReservationId());
        assertEquals(userId, result.getUserId());
        assertEquals(BigDecimal.valueOf(240), result.getAmount());
        assertEquals("EUR", result.getCurrency());
        assertEquals(InvoiceStatus.PAID, result.getStatus());
        assertEquals(createdOn, result.getCreatedOn());
        assertEquals(paidOn, result.getPaidOn());
        assertEquals(cancelledOn, result.getCancelledOn());
    }
}