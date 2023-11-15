package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sep3.server.CustomerP;
import sep3.server.CustomerResponseP;
import sep3.server.CustomerServiceGrpc;

@GrpcService
public class CustomerServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    private final CustomerRepository repository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository repository){
        this.repository=repository;
    }

    @Override
    public void createCustomer(CustomerP request, StreamObserver<CustomerResponseP> responseObserver){



        try {
            Address address = new Address(
                    request.getAddress().getDoorNumber(),
                                request.getAddress().getStreet(),
                                request.getAddress().getCity(),
                                request.getAddress().getState(),
                                request.getAddress().getPostalCode(),
                    request.getAddress().getCountry()
                        );
            Customer customer = new Customer(
                 request.getFirstName(),
                 request.getLastName(),
                 request.getUsername(),
                 request.getPassword(),
                 address
            );

            System.out.println(customer.toString());

            repository.save(customer);
            responseObserver.onCompleted();
        }
        catch (Exception e){
            e.getMessage();
            responseObserver.onError(new Throwable("Could not add Customer to the database"));
        }

    }
}
