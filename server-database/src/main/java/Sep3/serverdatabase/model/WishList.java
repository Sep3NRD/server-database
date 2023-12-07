package Sep3.serverdatabase.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table
public class WishList {
    @Id
    @SequenceGenerator(
            name = "wishList_sequence",
            sequenceName = "wishList_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "wishList_sequence"
    )

    private int id;
    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToMany
    @JoinTable(
            name = "wish_list_item",
            joinColumns = @JoinColumn(name = "wishList_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private Set<Item> items;

    public WishList(){}
    public WishList(Customer customer, Set<Item> items){
        this.customer = customer;
        this.items = items;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }


}
