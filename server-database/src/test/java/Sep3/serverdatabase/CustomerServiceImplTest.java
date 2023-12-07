package Sep3.serverdatabase;
import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.service.Implementations.CustomerServiceImpl;
import Sep3.serverdatabase.service.interfaces.AddressRepository;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import com.google.protobuf.Empty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.mockito.Mockito.*;
import io.grpc.stub.StreamObserver;
import sep3.server.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CustomerServiceImplTest {

    private CustomerServiceImpl customerService;
    private CustomerRepository mockCustomerRepository;
    private AddressRepository mockAddressRepository;
    private StreamObserver<CustomerResponseP> mockCustomerResponseObserver;
    private StreamObserver<Empty> mockEmptyResponseObserver;


    @BeforeEach
    void setUp() {
        mockCustomerRepository = mock(CustomerRepository.class);
        mockAddressRepository = mock(AddressRepository.class);
        customerService = new CustomerServiceImpl(mockCustomerRepository, mockAddressRepository);
        mockCustomerResponseObserver = mock(StreamObserver.class);
        mockEmptyResponseObserver=mock(StreamObserver.class);
    }

    @Test
    void testCreateCustomer() {
        // Mock gRPC request
        CustomerP request = CustomerP.newBuilder()
                .setFirstName("John")
                .setLastName("Doe")
                .setUsername("johndoe")
                .setPassword("password123")
                .setRole("user")
                .build();

        // Mock repository behavior
        when(mockCustomerRepository.save(any())).thenReturn(new Customer());
        when(mockAddressRepository.save(any())).thenReturn(new Address());

        // Execute the gRPC service method
        customerService.createCustomer(request, mockCustomerResponseObserver);

        // Verify that the repositories were called
        verify(mockCustomerRepository, times(2)).save(any());
        verify(mockAddressRepository, times(2)).save(any());

        // Verify that the responseObserver was notified
        verify(mockCustomerResponseObserver, times(1)).onNext(any());
        verify(mockCustomerResponseObserver, times(1)).onCompleted();
        verify(mockCustomerResponseObserver, never()).onError(any());
    }

    @Test
    void testGetCustomerByUsername() {
        // Mock gRPC request
        GetCustomerByUsername request = GetCustomerByUsername.newBuilder().setUsername("johndoe").build();

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("johndoe")).thenReturn(Optional.of(new Customer()));

        // Execute the gRPC service method
        customerService.getCustomerByUsername(request, mockCustomerResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("johndoe");

    }

    @Test
    void testGetCustomer_ValidCredentials() {
        // Mock gRPC request
        GetCustomerRequest request = GetCustomerRequest.newBuilder()
                .setUsername("ricardo")
                .setPassword("password123")
                .build();

        Set<Address> addresses = new HashSet<>();
        addresses.add(new Address(14,
                        "streettest",
                        "citytest",
                        "statetest",
                        14,
                        "regiontest"));

       Customer mockCustomer= new Customer("ricardo", "fernandes", "ricardo", "password123", "customer");
       mockCustomer.setOtherAddresses(addresses);

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("ricardo")).thenReturn(Optional.of(mockCustomer));

        // Execute the gRPC service method
        customerService.getCustomer(request, mockCustomerResponseObserver);


        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("ricardo");

        // Verify that the responseObserver was notified
        verify(mockCustomerResponseObserver, times(1)).onNext(any());
        verify(mockCustomerResponseObserver, times(1)).onCompleted();
        verify(mockCustomerResponseObserver, never()).onError(any());
    }

    @Test
    void testGetCustomer_UserNotFound() {
        // Mock gRPC request
        GetCustomerRequest request = GetCustomerRequest.newBuilder()
                .setUsername("nonexistentuser")
                .setPassword("password123")
                .build();

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("nonexistentuser")).thenReturn(Optional.empty());

        // Execute the gRPC service method
        customerService.getCustomer(request, mockCustomerResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("nonexistentuser");

        // Verify that the responseObserver was not notified and an error was sent
        verify(mockCustomerResponseObserver, never()).onNext(any());
        verify(mockCustomerResponseObserver, never()).onCompleted();
        verify(mockCustomerResponseObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    void testUpdateCustomer_CustomerFound() {
        // Mock gRPC request
        CustomerP request = CustomerP.newBuilder()
                .setId(1)
                .setFirstName("ricardo")
                .setLastName("fernandes")
                .setUsername("ricardo")
                .setPassword("password123")
                .setRole("customer")
                .build();

        AddressP addressP = AddressP.newBuilder()
                .setId(1)
                .setDoorNumber(123)
                .setStreet("Test Street")
                .setCity("Test City")
                .setState("Test State")
                .setPostalCode(12345)
                .setCountry("Test Country")
                .build();

        request = request.toBuilder().setAddress(addressP).build();

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("ricardo")).thenReturn(Optional.of(new Customer()));

        // Execute the gRPC service method
        customerService.updateCustomer(request, mockCustomerResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("ricardo");

        // Verify that the responseObserver was notified
        verify(mockCustomerResponseObserver, times(1)).onNext(any());
        verify(mockCustomerResponseObserver, times(1)).onCompleted();
        verify(mockCustomerResponseObserver, never()).onError(any());

        // Add more assertions based on the expected behavior of your service
    }

    @Test
    void testUpdateCustomer_CustomerNotFound() {
        // Mock gRPC request
        CustomerP request = CustomerP.newBuilder()
                .setId(1)
                .setFirstName("ricardo")
                .setLastName("fernandes")
                .setUsername("ricardo")
                .setPassword("password123")
                .setRole("customer")
                .build();

        AddressP addressP = AddressP.newBuilder()
                .setId(1)
                .setDoorNumber(123)
                .setStreet("Test Street")
                .setCity("Test City")
                .setState("Test State")
                .setPostalCode(12345)
                .setCountry("Test Country")
                .build();

        request = request.toBuilder().setAddress(addressP).build();

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("ricardo")).thenReturn(Optional.empty());

        // Execute the gRPC service method
        customerService.updateCustomer(request, mockCustomerResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("ricardo");

        // Verify that the responseObserver was not notified and an error was sent
        verify(mockCustomerResponseObserver, never()).onNext(any());
        verify(mockCustomerResponseObserver, never()).onCompleted();
        verify(mockCustomerResponseObserver, times(1)).onError(any(Throwable.class));

        // Add more assertions based on the expected behavior of your service
    }

    @Test
    void testAddNewAddress_CustomerFound() {
        // Mock gRPC request
        NewAddress request = NewAddress.newBuilder()
                .setUsername("ricardo")
                .setDoorNumber(123)
                .setStreet("Test Street")
                .setCity("Test City")
                .setState("Test State")
                .setPostalCode(12345)
                .setCountry("Test Country")
                .build();

        Set<Address> addresses = new HashSet<>();
        addresses.add(new Address(14,
                "streettest",
                "citytest",
                "statetest",
                14,
                "regiontest"));

        Customer mockCustomer= new Customer("ricardo", "fernandes", "ricardo", "password123", "customer");
        mockCustomer.setOtherAddresses(addresses);



        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("ricardo")).thenReturn(Optional.of(mockCustomer));

        // Execute the gRPC service method
        customerService.addNewAddress(request, mockEmptyResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("ricardo");
        verify(mockAddressRepository, times(1)).save(any(Address.class));
        verify(mockCustomerRepository, times(1)).save(any(Customer.class));

        // Verify that the responseObserver was notified
        verify(mockEmptyResponseObserver, times(1)).onNext(any());
        verify(mockEmptyResponseObserver, times(1)).onCompleted();
        verify(mockEmptyResponseObserver, never()).onError(any());

    }

    @Test
    void testAddNewAddress_CustomerNotFound() {
        // Mock gRPC request
        NewAddress request = NewAddress.newBuilder()
                .setUsername("ricardo")
                .setDoorNumber(123)
                .setStreet("Test Street")
                .setCity("Test City")
                .setState("Test State")
                .setPostalCode(12345)
                .setCountry("Test Country")
                .build();

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("ricardo")).thenReturn(Optional.empty());

        // Execute the gRPC service method
        customerService.addNewAddress(request, mockEmptyResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("ricardo");

        // Verify that the responseObserver was not notified and an error was sent
        verify(mockEmptyResponseObserver, never()).onNext(any());
        verify(mockEmptyResponseObserver, never()).onCompleted();
        verify(mockEmptyResponseObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    void testGetAddresses_CustomerFound() {
        // Mock gRPC request
        GetCustomerByUsername request = GetCustomerByUsername.newBuilder()
                .setUsername("ricardo")
                .build();

        // Mock repository behavior
        Set<Address> addresses = new HashSet<>();
        addresses.add(new Address(1, "Street1", "City1", "State1", 12345, "Country1"));
        addresses.add(new Address(2, "Street2", "City2", "State2", 67890, "Country2"));

        Customer mockCustomer = new Customer("ricardo", "fernandes", "ricardo", "password123", "customer");
        mockCustomer.setOtherAddresses(addresses);

        when(mockCustomerRepository.findByUserName("ricardo")).thenReturn(Optional.of(mockCustomer));

        // Mock responseObserver
        StreamObserver<GetAddressesByUsername> mockResponseObserver = mock(StreamObserver.class);

        // Execute the gRPC service method
        customerService.getAddresses(request, mockResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("ricardo");

        // Verify that the responseObserver was notified
        verify(mockResponseObserver, times(1)).onNext(any());
        verify(mockResponseObserver, times(1)).onCompleted();
        verify(mockResponseObserver, never()).onError(any());

        // Add more assertions based on the expected behavior of your service
    }

    @Test
    void testGetAddresses_CustomerNotFound() {
        // Mock gRPC request
        GetCustomerByUsername request = GetCustomerByUsername.newBuilder()
                .setUsername("nonexistentuser")
                .build();

        // Mock repository behavior
        when(mockCustomerRepository.findByUserName("nonexistentuser")).thenReturn(Optional.empty());

        // Mock responseObserver
        StreamObserver<GetAddressesByUsername> mockResponseObserver = mock(StreamObserver.class);

        // Execute the gRPC service method
        customerService.getAddresses(request, mockResponseObserver);

        // Verify that the repository was called
        verify(mockCustomerRepository, times(1)).findByUserName("nonexistentuser");

        // Verify that the responseObserver was not notified
        verify(mockResponseObserver, never()).onError(any());
        verify(mockResponseObserver, times(1)).onNext(any());
        verify(mockResponseObserver, times(1)).onCompleted();

        // Add more assertions based on the expected behavior of your service
    }
}



