package homerep.springy.controller;

import homerep.springy.exception.ApiException;
import homerep.springy.model.error.ApiErrorModel;
import homerep.springy.model.error.ValidationErrorModel;
import homerep.springy.model.error.FieldErrorModel;
import homerep.springy.model.error.ObjectErrorModel;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldErrorModel> fieldErrors = new ArrayList<>(ex.getFieldErrorCount());
        for (FieldError fieldError : ex.getFieldErrors()) {
            fieldErrors.add(new FieldErrorModel(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        List<ObjectErrorModel> objectErrors = new ArrayList<>(ex.getGlobalErrorCount());
        for (ObjectError objectError : ex.getGlobalErrors()) {
            objectErrors.add(new ObjectErrorModel(objectError.getDefaultMessage()));
        }

        ValidationErrorModel errorModel = new ValidationErrorModel(fieldErrors, objectErrors);
        return ResponseEntity.badRequest().body(errorModel);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorModel> handleApiException(ApiException ex) {
        ApiErrorModel errorModel = new ApiErrorModel(ex.getType(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorModel);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorModel> handleFileSizeLimitExceededException(FileSizeLimitExceededException ex) {
        ApiErrorModel errorModel = new ApiErrorModel("file_size_limit_exceeded", ex.getMessage());
        return ResponseEntity.badRequest().body(errorModel);
    }
}
