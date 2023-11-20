package homerep.springy.repository;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.model.appointment.AppointmentStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Appointment findByIdAndCustomerAccountEmail(long id, String email);

    Appointment findByIdAndServiceProviderAccountEmail(long id, String email);

    List<Appointment> findAllByCustomerAndEndTimeAfterAndStartTimeBefore(Customer customer, Instant start, Instant end);

    List<Appointment> findAllByServiceProviderAndEndTimeAfterAndStartTimeBefore(ServiceProvider serviceProvider, Instant start, Instant end);

    List<Appointment> findAllByCustomerAndStatus(Customer customer, AppointmentStatus status, Sort sort);

    List<Appointment> findAllByServiceProviderAndStatusIn(ServiceProvider serviceProvider, List<AppointmentStatus> statuses, Sort sort);

    List<Appointment> findAllByStatusInAndEndTimeBefore(List<AppointmentStatus> statuses, Instant endTime);

    // This is getting out of hand...
    List<Appointment> findAllByCustomerAndStatusAndEndTimeAfterAndStartTimeBefore(Customer customer, AppointmentStatus status, Instant start, Instant end);

    List<Appointment> findAllByServiceProviderAndStatusInAndEndTimeAfterAndStartTimeBefore(ServiceProvider serviceProvider, List<AppointmentStatus> status, Instant start, Instant end);

    // Aliases that better describe the queries
    default List<Appointment> findAllByCustomerAndPeriodIn(Customer customer, Instant start, Instant end) {
        return findAllByCustomerAndEndTimeAfterAndStartTimeBefore(customer, start, end);
    }

    default List<Appointment> findAllByServiceProviderAndPeriodIn(ServiceProvider serviceProvider, Instant start, Instant end) {
        return findAllByServiceProviderAndEndTimeAfterAndStartTimeBefore(serviceProvider, start, end);
    }

    default List<Appointment> findAllByCustomerAndStatusAndPeriodIn(Customer customer, AppointmentStatus status, Instant start, Instant end) {
        return findAllByCustomerAndStatusAndEndTimeAfterAndStartTimeBefore(customer, status, start, end);
    }

    default List<Appointment> findAllByServiceProviderAndStatusInAndPeriodIn(ServiceProvider serviceProvider, List<AppointmentStatus> status, Instant start, Instant end) {
        return findAllByServiceProviderAndStatusInAndEndTimeAfterAndStartTimeBefore(serviceProvider, status, start, end);
    }
}
