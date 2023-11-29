package Sep3.serverdatabase.model;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table
public class Order {
    @Id
    @SequenceGenerator(
            name = "customer_sequence",
            sequenceName = "customer_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "customer_sequence"
    )

    private int id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Item> items;
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address adress;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private boolean isConfirmed;
    private double totalPrice;

}
