package softuni.parkzonebillingservice.service.invoice;

import org.springframework.stereotype.Service;
import softuni.parkzonebillingservice.exception.BillingRuleException;
import softuni.parkzonebillingservice.exception.InvoiceNotFoundException;
import softuni.parkzonebillingservice.mapper.invoice.InvoiceMapper;
import softuni.parkzonebillingservice.model.dto.invoice.CreateInvoiceRequest;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.model.entity.Invoice;
import softuni.parkzonebillingservice.model.entity.InvoiceStatus;
import softuni.parkzonebillingservice.repository.invoice.InvoiceRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {

        if (invoiceRepository.existsByReservationId(request.getReservationId())) {
            throw new BillingRuleException("Invoice for this reservation already exists");
        }

        Invoice invoice = Invoice.builder()
                .reservationId(request.getReservationId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(InvoiceStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        return invoiceMapper.mapToResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse getInvoiceByReservationId(UUID reservationId) {

        Invoice invoice = invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        return invoiceMapper.mapToResponse(invoice);
    }

    public InvoiceResponse payInvoice(UUID invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new BillingRuleException("Only pending invoices can be paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidOn(LocalDateTime.now());

        return invoiceMapper.mapToResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse cancelInvoiceByReservationId(UUID reservationId) {

        Invoice invoice = invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED
                || invoice.getStatus() == InvoiceStatus.REFUNDED) {
            throw new BillingRuleException("Invoice is already closed");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            invoice.setStatus(InvoiceStatus.REFUNDED);
        } else {
            invoice.setStatus(InvoiceStatus.CANCELLED);
        }

        invoice.setCancelledOn(LocalDateTime.now());

        return invoiceMapper.mapToResponse(invoiceRepository.save(invoice));
    }





}
