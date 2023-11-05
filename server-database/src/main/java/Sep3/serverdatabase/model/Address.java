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
        private String doorNumber;
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public Address(String doorNumber, String street, String city, String state, String postalCode, String country) {
            this.doorNumber = doorNumber;
            this.street = street;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

    public Address() {

    }

    public String getDoorNumber() {
            return doorNumber;
        }

        public void setDoorNumber(String doorNumber) {
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

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
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

