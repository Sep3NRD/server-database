package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.service.interfaces.AddressRepository;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sep3.server.*;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@GrpcService
public class CustomerServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final CustomerRepository repository;
    private final AddressRepository addressRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository repository,AddressRepository addressRepository){
        this.repository=repository;
        this.addressRepository = addressRepository;
    }


    /*
    This method creates a customer and stores this customer in the database
     */
    @Override
    @Transactional
    public void createCustomer(CustomerP request, StreamObserver<CustomerResponseP> responseObserver) {

    /*
    Address and customer creation
    */
        try {
            // Convert the gRPC request message to a domain model object (Customer)
            Customer customer = getCustomerFields(request);
            repository.save(customer);

            // Save the customer's address to the address repository
            Optional<Address> address = customer.getOtherAddresses().stream().findFirst();
            addressRepository.save(address.get());


            Set<Address> otherAddresses = customer.getOtherAddresses();
            if (otherAddresses != null && !otherAddresses.isEmpty()) {
                for (Address otherAddress : otherAddresses) {
                    otherAddress.setCustomer(customer);
                    addressRepository.save(otherAddress);
                }
            }

            // Build the gRPC response message with the received customer data
            CustomerResponseP customerResponseP = CustomerResponseP.newBuilder().setCustomer(request).build();

            // Save the customer to the main repository
            repository.save(customer);





            // Send the response to the client
            responseObserver.onNext(customerResponseP);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // If an exception occurs during customer creation, handle the error
            System.out.println(e.getMessage());

            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not add Customer to the database"));
        }
    }



    @Override
    public void getCustomerByUsername(GetCustomerByUsername request, StreamObserver<CustomerResponseP> responseObserver) {

        // Extract the username from the gRPC request
        String username = request.getUsername();
        try {
            // Attempt to retrieve a customer from the repository based on the provided username
            Optional<Customer> optionalCustomer = repository.findByUserName(username);
            // Check if the customer with the given username exists
            if (optionalCustomer.isPresent()) {
                // If the customer exists, convert the customer data to a proto message (CustomerP)
                Customer customer = optionalCustomer.get();

                Optional<Address> optionalAddress = customer.getOtherAddresses().stream().findFirst();

                    Address address = optionalAddress.get();



                // Convert the address information to a proto message (AddressP)
                AddressP addressP = AddressP.newBuilder().setId(address.getId())
                        .setDoorNumber(address.getDoorNumber())
                        .setCity(address.getCity())
                        .setPostalCode(address.getPostalCode())
                        .setCountry(address.getCountry())
                        .setStreet(address.getStreet())
                        .setState(address.getState())
                        .build();

                // Build the CustomerP message with the transformed customer and address data
                CustomerP customerP = CustomerP.newBuilder()
                        .setId(customer.getId())
                        .setFirstName(customer.getFirstName())
                        .setUsername(customer.getUserName())
                        .setPassword(customer.getPassword())
                        .setLastName(customer.getLastName())
                        .setAddress(addressP)
                        .setRole(customer.getRole())
                        .build();

                // Build the response message with the transformed customer data
                CustomerResponseP customerResponseP = CustomerResponseP.newBuilder().setCustomer(customerP).build();

                // Send the response to the client
                responseObserver.onNext(customerResponseP);
                responseObserver.onCompleted();
            } else {
                // If the customer with the given username is not found, send an error response
                String errorMessage = "Customer with username " + username + " not found";
                responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
            }

        } catch (Exception e) {
            // If an exception occurs during processing, send an error response
            responseObserver.onError(e);
        }
    }



    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<CustomerResponseP> responseObserver) {

        // Retrieve customer information based on the provided username from the repository
        Optional<Customer> optionalCustomer = repository.findByUserName(request.getUsername());

        // Check if the customer with the given username exists and if the provided password matches
        if (optionalCustomer.isPresent() && optionalCustomer.get().getPassword().equals(request.getPassword())) {
            // If authentication is successful, retrieve the customer
            Customer customer = optionalCustomer.get();

            // Transform the customer object into a proto message (CustomerP)
            CustomerP customerP = getCustomerPFields(customer);

            // Build the response message with the transformed customer data
            CustomerResponseP customerResponseP = CustomerResponseP.newBuilder().setCustomer(customerP).build();

            // Send the response to the client
            responseObserver.onNext(customerResponseP);
            responseObserver.onCompleted();
        } else {
            // If the customer authentication fails, send an error response
            String errorMessage = "Username or password invalid";

            // Use gRPC Status.NOT_FOUND to represent a client error (404 Not Found)
            responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
        }
    }

    // Override the updateCustomer method from the gRPC service definition
    @Override
    public void updateCustomer(CustomerP request, StreamObserver<CustomerResponseP> responseObserver) {
        // Attempt to find a customer in the repository based on the provided username
        Optional<Customer> optionalCustomer = repository.findByUserName(request.getUsername());

        if (optionalCustomer.isPresent()) {
            // If the customer is found, retrieve it and update its fields
            Customer customer = optionalCustomer.get();
            customer = getCustomerFields(request);
            

            // Extract the address from the updated customer and set its ID
            Optional<Address> optionalAddress = customer.getOtherAddresses().stream().findFirst();
            Address address = optionalAddress.get();

            // Update additional fields of the customer
            customer.setRole(request.getRole());
            customer.setId(request.getId());

            // Save the updated address and customer to their respective repositories
            addressRepository.save(address);
            repository.save(customer);

            // Convert the updated customer to a gRPC response message
            CustomerP customerP = getCustomerPFields(customer);
            CustomerResponseP customerResponseP = CustomerResponseP.newBuilder().setCustomer(customerP).build();

            // Send the updated customer as a response to the client
            responseObserver.onNext(customerResponseP);
            responseObserver.onCompleted();
        } else {
            // If the customer is not found, send an error response
            String errorMessage = "Could not update customer with id: " + request.getId();

            // Use gRPC Status.ABORTED to represent a client error (HTTP 409 Conflict)
            responseObserver.onError(Status.ABORTED.withDescription(errorMessage).asException());
        }
    }

    @Override
    public void addNewAddress(NewAddress request, StreamObserver<Empty> responseObserver) {

        Optional<Customer> optionalCustomer = repository.findByUserName(request.getUsername());
        if (optionalCustomer.isPresent()) {
            // If the customer is found, retrieve it and update its fields
            Customer customer = optionalCustomer.get();

            Address address = new Address(request.getDoorNumber(),
                    request.getStreet(),request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry());
            address.setCustomer(customer);

            Set<Address> newAddresses = customer.getOtherAddresses();
            newAddresses.add(address);
            customer.setOtherAddresses(newAddresses);

            addressRepository.save(address);
            repository.save(customer);

            Empty empty= Empty.newBuilder().build();
            responseObserver.onNext(empty);
            responseObserver.onCompleted();
        }

        else {
            // If the customer is not found, send an error response
            String errorMessage = "Could not add a new address ";

            // Use gRPC Status.ABORTED to represent a client error (HTTP 409 Conflict)
            responseObserver.onError(Status.ABORTED.withDescription(errorMessage).asException());
        }
    }





    /**
     * Converts a gRPC CustomerP request message to a domain model object (Customer).
     *
     * @param request The gRPC CustomerP request message to be converted.
     * @return A Customer object with fields populated from the gRPC request.
     */
    private static Customer getCustomerFields(CustomerP request) {
        // Extract address details from the request and create an Address object
        Address address = new Address(
                request.getAddress().getDoorNumber(),
                request.getAddress().getStreet(),
                request.getAddress().getCity(),
                request.getAddress().getState(),
                request.getAddress().getPostalCode(),
                request.getAddress().getCountry()
        );

        Set<Address> addresses = new HashSet<>();
        addresses.add(address);
        // Create a Customer object with fields from the gRPC request
        return new Customer(
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                request.getPassword(),
                request.getRole(),addresses
        );
    }

    /**
     * Converts a domain model object (Customer) to a gRPC CustomerP response message.
     *
     * @param request The Customer object to be converted.
     * @return A gRPC CustomerP response message with fields populated from the Customer object.
     */
    private static CustomerP getCustomerPFields(Customer request) {
        // Extract address details from the Customer object and create an AddressP message
        Optional<Address> optionalAddress = request.getOtherAddresses().stream().findFirst();
        Address address=null;
        if (optionalAddress.isPresent()) {
            address=optionalAddress.get();
        }

        AddressP addressP = AddressP.newBuilder()
                .setDoorNumber(address.getDoorNumber())
                .setStreet(address.getStreet())
                .setCity(address.getCity())
                .setState(address.getState())
                .setPostalCode(address.getPostalCode())
                .setCountry(address.getCountry())
                .build();

        // Create a gRPC CustomerP response message with fields from the Customer object
        return CustomerP.newBuilder()
                .setId(request.getId())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setUsername(request.getUserName())
                .setPassword(request.getPassword())
                .setAddress(addressP).setRole(request.getRole())
                .build();
    }



}
