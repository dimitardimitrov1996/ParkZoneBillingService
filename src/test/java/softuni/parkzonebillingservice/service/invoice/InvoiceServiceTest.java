package softuni.parkzonebillingservice.service.invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import softuni.parkzonebillingservice.exception.BillingRuleException;
import softuni.parkzonebillingservice.exception.InvoiceNotFoundException;
import softuni.parkzonebillingservice.mapper.invoice.InvoiceMapper;
import softuni.parkzonebillingservice.model.dto.invoice.CreateInvoiceRequest;
import softuni.parkzonebillingservice.model.dto.invoice.InvoiceResponse;
import softuni.parkzonebillingservice.model.entity.Invoice;
import softuni.parkzonebillingservice.model.entity.InvoiceStatus;
import softuni.parkzonebillingservice.repository.invoice.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    private UUID invoiceId;
    private UUID reservationId;
    private UUID userId;

    private Invoice invoice;
    private InvoiceResponse invoiceResponse;
    private CreateInvoiceRequest createInvoiceRequest;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        invoice = Invoice.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        invoiceResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.PENDING)
                .createdOn(invoice.getCreatedOn())
                .build();

        createInvoiceRequest = CreateInvoiceRequest.builder()
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .build();
    }

    @Test
    void createInvoice_whenReservationDoesNotHaveInvoice_shouldCreateInvoice() {
        when(invoiceRepository.existsByReservationId(reservationId))
                .thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> {
                    Invoice savedInvoice = invocation.getArgument(0);
                    savedInvoice.setId(invoiceId);
                    return savedInvoice;
                });
        when(invoiceMapper.mapToResponse(any(Invoice.class)))
                .thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.createInvoice(createInvoiceRequest);

        assertEquals(invoiceResponse, result);

        verify(invoiceRepository).save(argThat(savedInvoice ->
                savedInvoice.getReservationId().equals(reservationId)
                        && savedInvoice.getUserId().equals(userId)
                        && savedInvoice.getAmount().equals(BigDecimal.valueOf(240))
                        && savedInvoice.getCurrency().equals("EUR")
                        && savedInvoice.getStatus() == InvoiceStatus.PENDING
                        && savedInvoice.getCreatedOn() != null
        ));
    }

    @Test
    void createInvoice_whenReservationAlreadyHasInvoice_shouldThrowException() {
        when(invoiceRepository.existsByReservationId(reservationId))
                .thenReturn(true);

        assertThrows(BillingRuleException.class,
                () -> invoiceService.createInvoice(createInvoiceRequest));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void getInvoiceByReservationId_whenInvoiceExists_shouldReturnInvoiceResponse() {
        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.of(invoice));
        when(invoiceMapper.mapToResponse(invoice))
                .thenReturn(invoiceResponse);

        InvoiceResponse result = invoiceService.getInvoiceByReservationId(reservationId);

        assertEquals(invoiceResponse, result);
    }

    @Test
    void getInvoiceByReservationId_whenInvoiceDoesNotExist_shouldThrowException() {
        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class,
                () -> invoiceService.getInvoiceByReservationId(reservationId));
    }

    @Test
    void payInvoice_whenInvoiceIsPending_shouldMarkInvoiceAsPaid() {
        InvoiceResponse paidResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.PAID)
                .createdOn(invoice.getCreatedOn())
                .paidOn(LocalDateTime.now())
                .build();

        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice))
                .thenReturn(invoice);
        when(invoiceMapper.mapToResponse(invoice))
                .thenReturn(paidResponse);

        InvoiceResponse result = invoiceService.payInvoice(invoiceId);

        assertEquals(paidResponse, result);
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertNotNull(invoice.getPaidOn());

        verify(invoiceRepository).save(invoice);
    }

    @Test
    void payInvoice_whenInvoiceDoesNotExist_shouldThrowException() {
        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class,
                () -> invoiceService.payInvoice(invoiceId));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void payInvoice_whenInvoiceIsNotPending_shouldThrowException() {
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));

        assertThrows(BillingRuleException.class,
                () -> invoiceService.payInvoice(invoiceId));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void cancelInvoiceByReservationId_whenInvoiceIsPending_shouldCancelInvoice() {
        InvoiceResponse cancelledResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.CANCELLED)
                .createdOn(invoice.getCreatedOn())
                .cancelledOn(LocalDateTime.now())
                .build();

        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice))
                .thenReturn(invoice);
        when(invoiceMapper.mapToResponse(invoice))
                .thenReturn(cancelledResponse);

        InvoiceResponse result = invoiceService.cancelInvoiceByReservationId(reservationId);

        assertEquals(cancelledResponse, result);
        assertEquals(InvoiceStatus.CANCELLED, invoice.getStatus());
        assertNotNull(invoice.getCancelledOn());

        verify(invoiceRepository).save(invoice);
    }

    @Test
    void cancelInvoiceByReservationId_whenInvoiceIsPaid_shouldRefundInvoice() {
        invoice.setStatus(InvoiceStatus.PAID);

        InvoiceResponse refundedResponse = InvoiceResponse.builder()
                .id(invoiceId)
                .reservationId(reservationId)
                .userId(userId)
                .amount(BigDecimal.valueOf(240))
                .currency("EUR")
                .status(InvoiceStatus.REFUNDED)
                .createdOn(invoice.getCreatedOn())
                .paidOn(invoice.getPaidOn())
                .cancelledOn(LocalDateTime.now())
                .build();

        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice))
                .thenReturn(invoice);
        when(invoiceMapper.mapToResponse(invoice))
                .thenReturn(refundedResponse);

        InvoiceResponse result = invoiceService.cancelInvoiceByReservationId(reservationId);

        assertEquals(refundedResponse, result);
        assertEquals(InvoiceStatus.REFUNDED, invoice.getStatus());
        assertNotNull(invoice.getCancelledOn());

        verify(invoiceRepository).save(invoice);
    }

    @Test
    void cancelInvoiceByReservationId_whenInvoiceDoesNotExist_shouldThrowException() {
        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class,
                () -> invoiceService.cancelInvoiceByReservationId(reservationId));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void cancelInvoiceByReservationId_whenInvoiceIsCancelled_shouldThrowException() {
        invoice.setStatus(InvoiceStatus.CANCELLED);

        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.of(invoice));

        assertThrows(BillingRuleException.class,
                () -> invoiceService.cancelInvoiceByReservationId(reservationId));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void cancelInvoiceByReservationId_whenInvoiceIsRefunded_shouldThrowException() {
        invoice.setStatus(InvoiceStatus.REFUNDED);

        when(invoiceRepository.findByReservationId(reservationId))
                .thenReturn(Optional.of(invoice));

        assertThrows(BillingRuleException.class,
                () -> invoiceService.cancelInvoiceByReservationId(reservationId));

        verify(invoiceRepository, never()).save(any());
    }
}
