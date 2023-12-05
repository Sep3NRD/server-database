package Sep3.serverdatabase.model;

import jakarta.persistence.*;
import org.checkerframework.checker.units.qual.C;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table
public class Customer {

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
    private String firstName;
    private String lastName;
    private String userName;
    private String password;

//    private Address address;
    private String role;


    @OneToMany(mappedBy = "customer",fetch = FetchType.EAGER)
    @Column
    private Set<Address> otherAddresses;


    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch =  FetchType.EAGER )
    private List<Order> orders;


    public Customer() {
    }

    public Customer(String firstName, String lastName, String userName, String password,String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
//        this.address = address;
        this.role=role;
    }

    public Customer(String firstName, String lastName, String userName, String password,String role,Set<Address> otherAddresses) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
//        this.address = address;
        this.role=role;
        this.otherAddresses=otherAddresses;

    }




    public Set<Address> getOtherAddresses() {
        return otherAddresses;
    }

    public void setOtherAddresses(Set<Address> otherAddresses) {
        this.otherAddresses = otherAddresses;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public Address getAddress() {
//        return address;
//    }
//
//    public void setAddress(Address address) {
//        this.address = address;
//    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Costumer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", address=" +
                '}';
    }
}
