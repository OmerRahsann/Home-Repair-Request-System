package homerep.springy.controller.customer;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentAppointmentException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.AppointmentService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerAppointmentController {
    @Autowired
    private CustomerRepository customerRepository;

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
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        List<Appointment> appointments = appointmentService.listAppointments(customer, yearMonth);
        return toModels(appointments);
    }

    @GetMapping("/appointments/{id}")
    public AppointmentModel getAppointment(@PathParam("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        return toModel(appointment);
    }

    @GetMapping("/service_request/{service_request_id}/appointments")
    public List<AppointmentModel> getAppointmentsFor(
            @PathVariable("service_request_id") int serviceRequestId,
            @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(serviceRequestId, user.getUsername());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        List<Appointment> appointments = serviceRequest.getAppointments();
        return toModels(appointments);
    }

    @PostMapping("/appointments/{id}/confirm")
    public void confirmAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        if (!appointmentService.confirmAppointment(appointment)) {
            throw new ApiException("unconfirmed_appointment", "Unable to confirm appointment.");
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public void cancelAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        appointmentService.cancelAppointment(appointment);
    }

    private AppointmentModel toModel(Appointment appointment) {
        return new AppointmentModel(
                appointment.getId(),
                null, // Don't need to send the customer their own information
                ServiceProviderInfoModel.fromEntity(appointment.getServiceProvider()),
                appointment.getServiceRequest().getId(),
                appointment.getDate(),
                appointment.getStatus(),
                appointment.getMessage()
        );
    }

    private List<AppointmentModel> toModels(List<Appointment> appointments) {
        List<AppointmentModel> appointmentModels = new ArrayList<>(appointments.size());
        for (Appointment appointment : appointments) {
            appointmentModels.add(toModel(appointment));
        }
        return appointmentModels;
    }
}
