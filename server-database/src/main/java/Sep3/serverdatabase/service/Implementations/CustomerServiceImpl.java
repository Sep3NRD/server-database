package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.service.interfaces.AddressRepository;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.transaction.Transaction;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sep3.server.*;

import java.util.Optional;

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
    public void createCustomer(CustomerP request, StreamObserver<CustomerResponseP> responseObserver){

        /*
        Address and costumer creation
        */
        try {
            Customer customer = getCustomerFields(request);

            CustomerResponseP customerResponseP= CustomerResponseP.newBuilder().setCustomer(request).build();
            addressRepository.save(customer.getAddress());
            repository.save(customer);
            responseObserver.onNext(customerResponseP);
            responseObserver.onCompleted();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            responseObserver.onError(new Throwable("Could not add Customer to the database"));
        }
    }


    @Override
    public void getCustomerByUsername(GetCustomerByUsername request, StreamObserver<CustomerResponseP> responseObserver){

        String username = request.getUsername();

        try {
            Optional<Customer> optionalCustomer = repository.findByUserName(username);
            if (optionalCustomer.isPresent()) {
                // Convert the retrieved customer to your response object
                Customer customer = optionalCustomer.get();
                AddressP addressP =AddressP.newBuilder().setDoorNumber(customer.getAddress().getDoorNumber())
                        .setCity(customer.getAddress().getCity())
                        .setPostalCode(customer.getAddress().getPostalCode())
                        .setCountry(customer.getAddress().getCountry())
                        .setStreet(customer.getAddress().getStreet())
                        .setState(customer.getAddress().getState()).build();
                CustomerP customerP = CustomerP.newBuilder().setId(customer.getId())
                        .setFirstName(customer.getFirstName())
                                .setUsername(customer.getUserName())
                                        .setPassword(customer.getPassword())
                                                .setLastName(customer.getLastName())
                                                        .setAddress(addressP).build();


                CustomerResponseP customerResponseP = CustomerResponseP.newBuilder().setCustomer(customerP).build();
                // Send the response to the client
                responseObserver.onNext(customerResponseP);
                responseObserver.onCompleted();
            } else {
                String errorMessage = "Customer with username " + username + " not found";
               responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
            }

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }


    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<CustomerResponseP> responseObserver) {

        Optional<Customer> optionalCustomer = repository.findByUserName(request.getUsername());
        if (optionalCustomer.isPresent()&& optionalCustomer.get().getPassword().equals(request.getPassword())){
            Customer customer = optionalCustomer.get();
            CustomerP customerP = getCustomerPFields(customer);
            CustomerResponseP customerResponseP = CustomerResponseP.newBuilder().setCustomer(customerP).build();
           responseObserver.onNext(customerResponseP);
            responseObserver.onCompleted();
        }
        else {
            String errorMessage = "Username or password invalid";
            responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
        }
    }




    private static Customer getCustomerFields(CustomerP request) {
        Address address = new Address(
                request.getAddress().getDoorNumber(),
                request.getAddress().getStreet(),
                request.getAddress().getCity(),
                request.getAddress().getState(),
                request.getAddress().getPostalCode(),
                request.getAddress().getCountry()
        );
        return new Customer(
                request.getFirstName(),
                request.getLastName(),
                request.getUsername(),
                request.getPassword(),
                address
        );
    }

    private static CustomerP getCustomerPFields(Customer request) {
        AddressP addressP = AddressP.newBuilder()
                .setDoorNumber(request.getAddress().getDoorNumber())
                .setStreet(request.getAddress().getStreet())
                .setCity(request.getAddress().getCity())
                .setState(request.getAddress().getState())
                .setPostalCode(request.getAddress().getPostalCode())
                .setCountry(request.getAddress().getCountry())
                .build();

        return CustomerP.newBuilder()
                .setId(request.getId())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setUsername(request.getUserName())
                .setPassword(request.getPassword())
                .setAddress(addressP)
                .build();
    }


}
