package homerep.springy.controller.customer;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.exception.NonExistentAppointmentException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.exception.UnconfirmableAppointmentException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.ZoneId;
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
            @RequestParam(value = "week_ends", required = false) Boolean weekEnds,
            @RequestParam(value = "zone_id") ZoneId zoneId,
            @AuthenticationPrincipal User user) {
        if (weekEnds == null) {
            weekEnds = false;
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        return appointmentService.getAppointmentsByMonth(customer, yearMonth, weekEnds, zoneId);
    }

    @GetMapping("/appointments/{id}")
    public AppointmentModel getAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        return AppointmentModel.fromEntity(appointment);
    }

    @GetMapping("/appointments/{id}/conflicting")
    public List<AppointmentModel> getConflictingAppointments(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        return appointmentService.getConflictingCustomerAppointments(appointment);
    }

    @GetMapping("/appointments/unconfirmed")
    public List<AppointmentModel> listUnconfirmedAppointments(@AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        return appointmentService.getUnconfirmedAppointments(customer);
    }

    @GetMapping("/service_request/{service_request_id}/appointments")
    public List<AppointmentModel> getAppointmentsFor(
            @PathVariable("service_request_id") int serviceRequestId,
            @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(serviceRequestId, user.getUsername());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        return appointmentService.getAppointmentsFor(serviceRequest);
    }

    @PostMapping("/appointments/{id}/confirm")
    public void confirmAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) throws UnconfirmableAppointmentException, ConflictingAppointmentException {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        appointmentService.confirmAppointment(appointment);
    }

    @PostMapping("/appointments/{id}/cancel")
    public void cancelAppointment(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        Appointment appointment = appointmentRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (appointment == null) {
            throw new NonExistentAppointmentException();
        }
        appointmentService.cancelAppointment(appointment);
    }
}
