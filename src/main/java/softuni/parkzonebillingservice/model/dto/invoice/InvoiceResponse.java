package softuni.parkzonebillingservice.model.dto.invoice;

import lombok.Builder;
import lombok.Data;
import softuni.parkzonebillingservice.model.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvoiceResponse {

    private UUID id;

    private UUID reservationId;

    private UUID userId;

    private BigDecimal amount;

    private String currency;

    private InvoiceStatus status;

    private LocalDateTime createdOn;

    private LocalDateTime paidOn;

    private LocalDateTime cancelledOn;
}
