package Sep3.serverdatabase.service.interfaces;

import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.WishList;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WishListRepository extends JpaRepository<WishList,Integer> {
    @Query("SELECT w FROM WishList w WHERE w.customer = :customer")
    Optional<WishList> findByCustomer(Customer customer);
}
