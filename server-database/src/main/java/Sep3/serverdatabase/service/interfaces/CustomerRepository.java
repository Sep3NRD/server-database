package Sep3.serverdatabase.service.interfaces;

import Sep3.serverdatabase.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Integer> {
}
