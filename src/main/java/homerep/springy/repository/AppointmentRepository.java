package homerep.springy.repository;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Appointment findByIdAndCustomerAccountEmail(long id, String email);

    Appointment findByIdAndServiceProviderAccountEmail(long id, String email);

    List<Appointment> findAllByCustomerAndDateBetween(Customer customer, LocalDate start, LocalDate end);

    List<Appointment> findAllByServiceProviderAndDateBetween(ServiceProvider serviceProvider, LocalDate start, LocalDate end);
}
