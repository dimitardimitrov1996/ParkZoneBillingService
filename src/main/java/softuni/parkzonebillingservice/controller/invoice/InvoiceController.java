package softuni.parkzonebillingservice.controller.invoice;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import softuni.parkzonebillingservice.model.dto.invoice.CreateInvoiceRequest;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.service.invoice.InvoiceService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return invoiceService.createInvoice(request);
    }

    @GetMapping("/reservation/{reservationId}")
    public InvoiceResponse getInvoiceByReservationId(@PathVariable UUID reservationId) {
        return invoiceService.getInvoiceByReservationId(reservationId);
    }

    @PutMapping("/{invoiceId}/pay")
    public InvoiceResponse payInvoice(@PathVariable UUID invoiceId) {
        return invoiceService.payInvoice(invoiceId);
    }

    @PutMapping("/reservation/{reservationId}/cancel")
    public InvoiceResponse cancelInvoiceByReservationId(@PathVariable UUID reservationId) {
        return invoiceService.cancelInvoiceByReservationId(reservationId);
    }
}