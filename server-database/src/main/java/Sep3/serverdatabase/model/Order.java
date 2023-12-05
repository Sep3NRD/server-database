package Sep3.serverdatabase.model;


import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    @SequenceGenerator(
            name = "order_sequence",
            sequenceName = "order_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "order_sequence"
    )

    private int id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Column(name = "item_id")
    private List<Item> items;
    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address adress;
    @Column(name = "order_date")
    private String orderDate;

    @Column(name = "delivery_date")
    private String deliveryDate;
    private boolean isConfirmed;
    private double totalPrice;
    public Order(){}

    public Order (Customer customer, List<Item> items,Address address, String orderDate, String deliveryDate){
        this.customer = customer;
        this.items = items;
        this.adress = address;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.isConfirmed = false;
        this.totalPrice = getTotalPrice();
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Address getAdress() {
        return adress;
    }

    public void setAdress(Address adress) {
        this.adress = adress;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Item item: items) {
            total += item.getPrice();
        }
        return total;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + customer +
                ", items=" + items +
                ", adress=" + adress +
                ", orderDate=" + orderDate +
                ", deliveryDate=" + deliveryDate +
                ", isConfirmed=" + isConfirmed +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
