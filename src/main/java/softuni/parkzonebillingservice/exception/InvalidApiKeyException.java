package softuni.parkzonebillingservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidApiKeyException extends ApiException {

    private final HttpStatus httpStatus;

    public InvalidApiKeyException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}