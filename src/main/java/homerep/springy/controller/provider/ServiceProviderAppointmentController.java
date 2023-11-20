package homerep.springy.controller.provider;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.exception.NonExistentAppointmentException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.AppointmentService;
import homerep.springy.service.impl.EmailRequestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/provider")
public class ServiceProviderAppointmentController {
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private EmailRequestServiceImpl emailRequestService;

    @GetMapping("/appointments")
    public List<AppointmentModel> getAppointments(
            @RequestParam(value = "year") int year,
            @RequestParam(value = "month") int month,
            @RequestParam(value = "zone_id") ZoneId zoneId,
            @AuthenticationPrincipal User user) {
        YearMonth yearMonth = YearMonth.of(year, month);
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        return appointmentService.getAppointmentsByMonth(serviceProvider, yearMonth, zoneId);
    }

    @GetMapping("/appointments/{id}")
    public AppointmentModel getAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndServiceProviderAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        return AppointmentModel.fromEntity(appointment);
    }

    @GetMapping("/appointments/updated")
    public List<AppointmentModel> getUpdatedAppointments(@AuthenticationPrincipal User user) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        return appointmentService.getUpdatedAppointments(serviceProvider);
    }

    @PostMapping("/service_requests/{service_request_id}/appointments/create")
    public long createAppointment(
            @PathVariable("service_request_id") int serviceRequestId,
            @RequestBody @Validated CreateAppointmentModel createAppointmentModel,
            @AuthenticationPrincipal User user) throws ConflictingAppointmentException {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId).orElse(null);
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        if (!emailRequestService.canAccessEmail(serviceProvider, serviceRequest)) {
            throw new ApiException("missing_accepted_email_request", "Can't create appointment for service request without an accepted email request.");
        }
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, createAppointmentModel);
        return appointment.getId();
    }

    @PostMapping("/appointments/{id}/cancel")
    public void cancelAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndServiceProviderAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        appointmentService.cancelAppointment(appointment);
    }
}
