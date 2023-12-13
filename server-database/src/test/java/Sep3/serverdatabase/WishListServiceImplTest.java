//package Sep3.serverdatabase;
//
//import Sep3.serverdatabase.model.Customer;
//import Sep3.serverdatabase.model.Item;
//import Sep3.serverdatabase.model.WishList;
//import Sep3.serverdatabase.service.Implementations.WishListServiceImpl;
//import Sep3.serverdatabase.service.interfaces.CustomerRepository;
//import Sep3.serverdatabase.service.interfaces.ItemRepository;
//import Sep3.serverdatabase.service.interfaces.WishListRepository;
//import io.grpc.stub.StreamObserver;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import sep3.server.*;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class WishListServiceImplTest {
//    @Mock
//    private WishListRepository wishListRepository;
//
//    @Mock
//    private CustomerRepository customerRepository;
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @Mock
//    private StreamObserver<SuccessResponse> successResponseObserver;
//
//    @Mock
//    private StreamObserver<GetWishListResponse> getWishListResponseObserver;
//
//    @InjectMocks
//    private WishListServiceImpl wishListService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testAddToWishList() {
//        // Create a test WishListRequest
//        WishListRequest wishListRequest = WishListRequest.newBuilder()
//                .setItemId(1)
//                .setUsername("testUser")
//                .build();
//
//        // Create a test Customer and Item
//        Customer testCustomer = new Customer();
//        Item testItem = new Item();
//
//        // Mock the behavior of repositories
//        when(customerRepository.findByUserName(any())).thenReturn(Optional.of(testCustomer));
//        when(itemRepository.findById(any())).thenReturn(Optional.of(testItem));
//        when(wishListRepository.findByCustomer(any())).thenReturn(Optional.empty());
//
//        // Call the method to be tested
//        wishListService.addToWishList(wishListRequest, successResponseObserver);
//
//        // Verify that repository save method is called once
//        verify(wishListRepository, times(1)).save(any());
//
//        // Verify that the onNext and onCompleted methods are called on the responseObserver
//        verify(successResponseObserver, times(1)).onNext(any());
//        verify(successResponseObserver, times(1)).onCompleted();
//    }
//
//    @Test
//    public void testGetWishList() {
//        // Arrange
//        String username = "testUser";
//        Customer mockCustomer = new Customer();
//        mockCustomer.setUserName(username);
//
//        GetWishListRequest request = GetWishListRequest.newBuilder().setUsername(username).build();
//        StreamObserver<GetWishListResponse> responseObserverMock = mock(StreamObserver.class);
//
//        // Mock the behavior of repositories
//        when(customerRepository.findByUserName(username)).thenReturn(Optional.of(mockCustomer));
//        when(wishListRepository.findByCustomer(mockCustomer)).thenReturn(Optional.ofNullable(null)); // Simulate null WishList
//
//        // Act
//        wishListService.getWishList(request, responseObserverMock);
//
//        // Assert
//        ArgumentCaptor<WishList> wishListCaptor = ArgumentCaptor.forClass(WishList.class);
//        verify(wishListRepository).save(wishListCaptor.capture());
//
//        WishList savedWishList = wishListCaptor.getValue();
//        assertNotNull(savedWishList);
//        assertEquals(mockCustomer, savedWishList.getCustomer());
//        assertEquals(1, savedWishList.getItems().size());
//
//        // Additional assertions based on your specific logic
//    }
//
//
//
//}
