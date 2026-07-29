package softuni.parkzonebillingservice.exception;

import org.springframework.http.HttpStatus;

public class InvoiceNotFoundException extends ApiException {

    public InvoiceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}