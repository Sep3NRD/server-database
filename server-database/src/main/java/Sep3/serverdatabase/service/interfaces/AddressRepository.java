package Sep3.serverdatabase.service.interfaces;

import Sep3.serverdatabase.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address,Integer> {
}
