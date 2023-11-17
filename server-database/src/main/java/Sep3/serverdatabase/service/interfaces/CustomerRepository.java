package Sep3.serverdatabase.service.interfaces;

import Sep3.serverdatabase.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    Optional<Customer> findByUserName(String userName);
}
