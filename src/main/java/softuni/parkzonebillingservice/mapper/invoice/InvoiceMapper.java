package softuni.parkzonebillingservice.mapper.invoice;

import org.springframework.stereotype.Component;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.model.entity.Invoice;

@Component
public class InvoiceMapper {

    public InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .reservationId(invoice.getReservationId())
                .userId(invoice.getUserId())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .createdOn(invoice.getCreatedOn())
                .paidOn(invoice.getPaidOn())
                .cancelledOn(invoice.getCancelledOn())
                .build();
    }
}
