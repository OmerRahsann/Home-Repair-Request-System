package homerep.springy.controller.provider;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.NonExistentAppointmentException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.ArrayList;
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

    @GetMapping("/appointments")
    public List<AppointmentModel> getAppointments(
            @RequestParam(value = "year") int year,
            @RequestParam(value = "month") int month,
            @AuthenticationPrincipal User user) {
        YearMonth yearMonth = YearMonth.of(year, month);
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        List<Appointment> appointments = appointmentService.listAppointments(serviceProvider, yearMonth);
        List<AppointmentModel> appointmentModels = new ArrayList<>(appointments.size());
        for (Appointment appointment : appointments) {
            appointmentModels.add(toModel(appointment));
        }
        return appointmentModels;
    }

    @GetMapping("/appointments/{id}")
    public AppointmentModel getAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndServiceProviderAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        return toModel(appointment);
    }

    @PostMapping("/service_requests/{service_request_id}/appointments/create")
    public long createAppointment(
            @PathVariable("service_request_id") int serviceRequestId,
            @RequestBody @Validated CreateAppointmentModel createAppointmentModel,
            @AuthenticationPrincipal User user) {
        // TODO some authorization before making an appointment? Must request email frist?
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId).orElse(null);
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
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

    private AppointmentModel toModel(Appointment appointment) {
        return new AppointmentModel(
                appointment.getId(),
                new CustomerInfoModel(
                        appointment.getCustomer().getFirstName(),
                        appointment.getCustomer().getMiddleName(),
                        appointment.getCustomer().getLastName(),
                        null, // Use the address in the service request
                        appointment.getCustomer().getPhoneNumber()
                ),
                null, // Don't need to send service providers their own information
                appointment.getServiceRequest().getId(),
                appointment.getDate(),
                appointment.getStatus(),
                appointment.getMessage()
        );
    }
}
