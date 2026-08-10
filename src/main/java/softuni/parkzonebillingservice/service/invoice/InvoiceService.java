package softuni.parkzonebillingservice.service.invoice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import softuni.parkzonebillingservice.exception.BillingRuleException;
import softuni.parkzonebillingservice.exception.InvoiceNotFoundException;
import softuni.parkzonebillingservice.mapper.invoice.InvoiceMapper;
import softuni.parkzonebillingservice.model.dto.invoice.CreateInvoiceRequest;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.model.dto.invoice.UpdateInvoiceRequest;
import softuni.parkzonebillingservice.model.entity.Invoice;
import softuni.parkzonebillingservice.model.entity.InvoiceStatus;
import softuni.parkzonebillingservice.repository.invoice.InvoiceRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {

        if (invoiceRepository.existsByReservationId(request.getReservationId())) {
            log.warn("Invoice creation rejected. Reservation [{}] already has invoice", request.getReservationId());
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

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice [{}] created for reservation [{}] and user [{}]",
                savedInvoice.getId(), savedInvoice.getReservationId(), savedInvoice.getUserId());

        return invoiceMapper.mapToResponse(savedInvoice);
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
            log.warn("Invoice [{}] payment rejected because status is [{}]", invoiceId, invoice.getStatus());
            throw new BillingRuleException("Only pending invoices can be paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidOn(LocalDateTime.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice [{}] paid", savedInvoice.getId());

        return invoiceMapper.mapToResponse(savedInvoice);
    }

    public InvoiceResponse cancelInvoiceByReservationId(UUID reservationId) {

        Invoice invoice = invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED
                || invoice.getStatus() == InvoiceStatus.REFUNDED) {
            log.warn("Invoice [{}] cancellation rejected because status is [{}]", invoice.getId(), invoice.getStatus());
            throw new BillingRuleException("Invoice is already closed");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            invoice.setStatus(InvoiceStatus.REFUNDED);
        } else {
            invoice.setStatus(InvoiceStatus.CANCELLED);
        }

        invoice.setCancelledOn(LocalDateTime.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice [{}] closed with status [{}] for reservation [{}]",
                savedInvoice.getId(), savedInvoice.getStatus(), reservationId);

        return invoiceMapper.mapToResponse(savedInvoice);
    }

    public InvoiceResponse updateInvoiceByReservationId(UUID reservationId, UpdateInvoiceRequest request) {

        Invoice invoice = invoiceRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            log.warn("Invoice [{}] update rejected because status is [{}]",
                    invoice.getId(), invoice.getStatus());

            throw new BillingRuleException("Only pending invoices can be updated");
        }

        invoice.setAmount(request.getAmount());
        invoice.setCurrency(request.getCurrency());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice [{}] updated for reservation [{}]. New amount [{}] [{}]",
                savedInvoice.getId(),
                reservationId,
                savedInvoice.getAmount(),
                savedInvoice.getCurrency());

        return invoiceMapper.mapToResponse(savedInvoice);
    }
}
