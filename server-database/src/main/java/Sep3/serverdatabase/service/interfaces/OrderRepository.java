package Sep3.serverdatabase.service.interfaces;

import Sep3.serverdatabase.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Integer> {
}
