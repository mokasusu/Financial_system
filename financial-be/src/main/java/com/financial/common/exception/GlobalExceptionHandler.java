package com.financial.common.exception;

import com.financial.common.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler({
                        UserNotFoundException.class,
                        CompanyNotFoundException.class,
                        StatementNotFoundException.class,
                        IndicatorNotFoundException.class,
                        RatioNotFoundException.class,
                        MappingNotFoundException.class,
                        CriteriaNotFoundException.class,
                        CalculationNotFoundException.class,
                        ValidationNotFoundException.class,
                        ValidationRuleNotFoundException.class,
                        AssessmentNotFoundException.class,
                        RiskAnalysisNotFoundException.class,
                        RiskRuleNotFoundException.class,
                        AIAnalysisNotFoundException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleNotFoundException(RuntimeException ex) {
                String errorCode = null;
                if (ex instanceof StatementNotFoundException se) {
                        errorCode = se.getErrorCode();
                } else if (ex instanceof IndicatorNotFoundException ie) {
                        errorCode = ie.getErrorCode();
                } else if (ex instanceof RatioNotFoundException re) {
                        errorCode = re.getErrorCode();
                } else if (ex instanceof MappingNotFoundException me) {
                        errorCode = me.getErrorCode();
                } else if (ex instanceof CriteriaNotFoundException ce) {
                        errorCode = ce.getErrorCode();
                } else if (ex instanceof CalculationNotFoundException cale) {
                        errorCode = cale.getErrorCode();
                } else if (ex instanceof ValidationNotFoundException ve) {
                        errorCode = ve.getErrorCode();
                } else if (ex instanceof ValidationRuleNotFoundException vre) {
                        errorCode = vre.getErrorCode();
                } else if (ex instanceof AssessmentNotFoundException ae) {
                        errorCode = ae.getErrorCode();
                } else if (ex instanceof RiskAnalysisNotFoundException rae) {
                        errorCode = rae.getErrorCode();
                } else if (ex instanceof RiskRuleNotFoundException rre) {
                        errorCode = rre.getErrorCode();
                } else if (ex instanceof AIAnalysisNotFoundException aae) {
                        errorCode = aae.getErrorCode();
                }
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage(), errorCode));
        }

        @ExceptionHandler({
                        InvalidValidationRuleException.class,
                        DuplicateValidationRuleException.class,
                        InvalidAssessmentStateException.class,
                        InvalidRiskRuleException.class,
                        DuplicateRiskRuleException.class,
                        InvalidRiskAnalysisStateException.class,
                        UpstreamDataUnavailableException.class,
                        InvalidAIAnalysisStateException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleValidationRuleException(RuntimeException ex) {
                String errorCode = null;
                if (ex instanceof InvalidValidationRuleException ie) {
                        errorCode = ie.getErrorCode();
                } else if (ex instanceof DuplicateValidationRuleException de) {
                        errorCode = de.getErrorCode();
                } else if (ex instanceof InvalidAssessmentStateException iase) {
                        errorCode = iase.getErrorCode();
                } else if (ex instanceof InvalidRiskRuleException irfe) {
                        errorCode = irfe.getErrorCode();
                } else if (ex instanceof DuplicateRiskRuleException dre) {
                        errorCode = dre.getErrorCode();
                } else if (ex instanceof InvalidRiskAnalysisStateException irase) {
                        errorCode = irase.getErrorCode();
                } else if (ex instanceof UpstreamDataUnavailableException udue) {
                        errorCode = udue.getErrorCode();
                } else if (ex instanceof InvalidAIAnalysisStateException iase) {
                        errorCode = iase.getErrorCode();
                }
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), errorCode));
        }

        @ExceptionHandler(ConcurrentAIAnalysisException.class)
        public ResponseEntity<ApiResponse<Void>> handleConcurrentAIAnalysisException(ConcurrentAIAnalysisException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(OpenRouterTimeoutException.class)
        public ResponseEntity<ApiResponse<Void>> handleOpenRouterTimeoutException(OpenRouterTimeoutException ex) {
                return ResponseEntity
                                .status(HttpStatus.GATEWAY_TIMEOUT)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(OpenRouterApiException.class)
        public ResponseEntity<ApiResponse<Void>> handleOpenRouterApiException(OpenRouterApiException ex) {
                HttpStatus status = HttpStatus.resolve(ex.getStatusCode()) != null
                                ? HttpStatus.valueOf(ex.getStatusCode())
                                : HttpStatus.BAD_GATEWAY;
                return ResponseEntity
                                .status(status)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(UnconfirmedStatementException.class)
        public ResponseEntity<ApiResponse<Void>> handleUnconfirmedStatementException(UnconfirmedStatementException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(InvalidCriteriaException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidCriteriaException(InvalidCriteriaException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(UnsupportedAccountingStandardException.class)
        public ResponseEntity<ApiResponse<Void>> handleUnsupportedAccountingStandard(
                        UnsupportedAccountingStandardException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler({
                        InvalidFileException.class,
                        FileTooLargeException.class,
                        UnsupportedFileTypeException.class,
                        InvalidStatementStatusException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleFileAndStatementValidationException(RuntimeException ex) {
                String errorCode = null;
                if (ex instanceof InvalidFileException e)
                        errorCode = e.getErrorCode();
                else if (ex instanceof FileTooLargeException e)
                        errorCode = e.getErrorCode();
                else if (ex instanceof UnsupportedFileTypeException e)
                        errorCode = e.getErrorCode();
                else if (ex instanceof InvalidStatementStatusException e)
                        errorCode = e.getErrorCode();

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), errorCode));
        }

        @ExceptionHandler(OcrApiException.class)
        public ResponseEntity<ApiResponse<Void>> handleOcrApiException(OcrApiException ex) {
                HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
                if (status == null)
                        status = HttpStatus.BAD_GATEWAY;

                return ResponseEntity
                                .status(status)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(IndicatorMappingException.class)
        public ResponseEntity<ApiResponse<Void>> handleIndicatorMappingException(IndicatorMappingException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("File size exceeds the maximum allowed limit of 25 MB",
                                                "FILE_TOO_LARGE"));
        }

        @ExceptionHandler({
                        EmailAlreadyExistsException.class,
                        CompanyAlreadyExistsException.class,
                        TaxCodeAlreadyExistsException.class,
                        DuplicateCriteriaException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleConflictException(RuntimeException ex) {
                String errorCode = ex instanceof DuplicateCriteriaException dce ? dce.getErrorCode() : null;
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage(), errorCode));
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("Database constraint violation: "
                                                + ex.getMostSpecificCause().getMessage()));
        }

        @ExceptionHandler({
                        InvalidCredentialsException.class,
                        BadCredentialsException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleInvalidCredentialsException(Exception ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler({
                        InvalidTokenException.class,
                        TokenExpiredException.class,
                        TokenRevokedException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleTokenExceptions(RuntimeException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler({
                        AccountLockedException.class,
                        AccountInactiveException.class
        })
        public ResponseEntity<ApiResponse<Void>> handleAccountStatusExceptions(RuntimeException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Authentication failed: " + ex.getMessage()));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error(
                                                "Access denied: You do not have permission to access this resource"));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Validation failed", errors));
        }

        @ExceptionHandler(OcrTimeoutException.class)
        public ResponseEntity<ApiResponse<Void>> handleOcrTimeoutException(OcrTimeoutException ex) {
                return ResponseEntity
                                .status(HttpStatus.GATEWAY_TIMEOUT)
                                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
                        org.springframework.http.converter.HttpMessageNotReadableException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Request body is missing or invalid: " + ex.getMostSpecificCause().getMessage(),
                                                "INVALID_REQUEST_BODY"));
        }

        @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
                        org.springframework.web.bind.MissingServletRequestParameterException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Missing required parameter: " + ex.getParameterName(),
                                                "MISSING_PARAMETER"));
        }

        @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
                        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("Invalid value for parameter: " + ex.getName(), "TYPE_MISMATCH"));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
        }
}
