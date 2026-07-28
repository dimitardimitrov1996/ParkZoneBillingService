package softuni.parkzonebillingservice.repository.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import softuni.parkzonebillingservice.model.entity.Invoice;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByReservationId(UUID reservationId);

    Optional<Invoice> findByReservationId(UUID reservationId);
}
