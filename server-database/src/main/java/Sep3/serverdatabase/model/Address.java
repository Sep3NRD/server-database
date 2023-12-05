package Sep3.serverdatabase.model;

import jakarta.persistence.*;

@Entity
@Table
public class Address {

    @Id
    @SequenceGenerator(
            name = "address_sequence",
            sequenceName = "address_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "address_sequence"
    )
        private int id;
        private int doorNumber;
        private String street;
        private String city;
        private String state;
        private int postalCode;
        private String country;

        @ManyToOne
        @JoinColumn(name = "customer_id")
        private Customer customer;

        public Address(int doorNumber, String street, String city, String state, int postalCode, String country) {
            this.doorNumber = doorNumber;
            this.street = street;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

    public Address() {

    }


    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getDoorNumber() {
            return doorNumber;
        }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDoorNumber(int doorNumber) {
            this.doorNumber = doorNumber;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public int getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(int postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        @Override
        public String toString() {
            return doorNumber + " " + street + ", " + city + ", " + state + " " + postalCode + ", " + country;
        }
    }

