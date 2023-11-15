package Sep3.serverdatabase.service.interfaces;

import Sep3.serverdatabase.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item,Integer> {

}
