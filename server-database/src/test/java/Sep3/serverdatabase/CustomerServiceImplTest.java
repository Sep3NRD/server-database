package Sep3.serverdatabase;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.service.Implementations.CustomerServiceImpl;
import Sep3.serverdatabase.service.interfaces.AddressRepository;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import sep3.server.AddressP;
import sep3.server.CustomerP;
import sep3.server.CustomerResponseP;
import sep3.server.GetCustomerByUsername;
import io.grpc.stub.StreamObserver;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    public void testCreateCustomer() {
        // create a mock CustomerP request
        CustomerP customerP = CustomerP.newBuilder()
                .setFirstName("John")
                .setLastName("Doe")
                .setUsername("john.doe")
                .setPassword("password")
                .setAddress(AddressP.newBuilder()
                        .setDoorNumber(123)
                        .setStreet("Main St")
                        .setCity("City")
                        .setState("State")
                        .setPostalCode(12345)
                        .setCountry("Country")
                        .build())
                .build();

        // create a mock StreamObserver for the response
        StreamObserver<CustomerResponseP> responseObserver = Mockito.mock(StreamObserver.class);

        // mock repository behavior
        when(customerRepository.findByUserName("john.doe")).thenReturn(Optional.empty());

        // invoke the method to be tested
        customerService.createCustomer(customerP, responseObserver);

        // verify that the customer and address were saved
        verify(addressRepository).save(Mockito.any());
        verify(customerRepository).save(Mockito.any());

        // verify that the response was sent to the client
        verify(responseObserver).onNext(Mockito.any());
        verify(responseObserver).onCompleted();
    }

    @Test
    public void testGetCustomerByUsername() {
        // create a mock GetCustomerByUsername request
        GetCustomerByUsername request = GetCustomerByUsername.newBuilder()
                .setUsername("john.doe")
                .build();

        // create a mock StreamObserver for the response
        StreamObserver<CustomerResponseP> responseObserver = Mockito.mock(StreamObserver.class);

        // create a mock Customer object
        Customer customer = new Customer("John", "Doe", "john.doe", "password",
                new Address(123, "Main St", "City", "State", 12345, "Country"));

        // mock repository behavior
        when(customerRepository.findByUserName("john.doe")).thenReturn(Optional.of(customer));

        // invoke the method to be tested
        customerService.getCustomerByUsername(request, responseObserver);

        // verify that the response was sent to the client
        verify(responseObserver).onNext(Mockito.any());
        verify(responseObserver).onCompleted();
    }

    @Test
    public void testGetCustomer() {
        // create a mock GetCustomerRequest request
        sep3.server.GetCustomerRequest request = sep3.server.GetCustomerRequest.newBuilder()
                .setUsername("john.doe")
                .setPassword("password")
                .build();

        // create a mock StreamObserver for the response
        StreamObserver<CustomerResponseP> responseObserver = Mockito.mock(StreamObserver.class);

        // create a mock Customer object
        Customer customer = new Customer("John", "Doe", "john.doe", "password",
                new Address(123, "Main St", "City", "State", 12345, "Country"));

        // mock repository behavior
        when(customerRepository.findByUserName("john.doe")).thenReturn(Optional.of(customer));

        // invoke the method to be tested
        customerService.getCustomer(request, responseObserver);

        // verify that the response was sent to the client
        verify(responseObserver).onNext(Mockito.any());
        verify(responseObserver).onCompleted();
    }
}





