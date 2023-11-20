package homerep.springy.controller;

import homerep.springy.entity.Appointment;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.exception.UnconfirmableAppointmentException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.error.*;
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

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;
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

    @ExceptionHandler
    public ResponseEntity<ApiErrorModel> handleZoneRulesException(ZoneRulesException ex) {
        ApiErrorModel errorModel = new ApiErrorModel("invalid_time_zone", "An invalid timezone was specified.");
        return ResponseEntity.badRequest().body(errorModel);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorModel> handleDateTimeException(DateTimeException ex) {
        ApiErrorModel errorModel = new ApiErrorModel("invalid_date_time", "An invalid date/time was specified.");
        return ResponseEntity.badRequest().body(errorModel);
    }

    @ExceptionHandler
    public ResponseEntity<ConflictingAppointmentErrorModel> handleConflictingAppointmentException(ConflictingAppointmentException ex) {
        List<AppointmentModel> models = new ArrayList<>(ex.getConflictingAppointments().size());
        for (Appointment appointment : ex.getConflictingAppointments()) {
            models.add(AppointmentModel.fromEntity(appointment));
        }
        ConflictingAppointmentErrorModel errorModel = new ConflictingAppointmentErrorModel(models);
        return ResponseEntity.badRequest().body(errorModel);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorModel> handleUnconfirmableAppointmentException(UnconfirmableAppointmentException ex) {
        ApiErrorModel errorModel = new ApiErrorModel("unconfirmable_appointment", ex.getMessage());
        return ResponseEntity.badRequest().body(errorModel);
    }
}
