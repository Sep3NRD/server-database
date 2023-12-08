package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.model.WishList;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import Sep3.serverdatabase.service.interfaces.ItemRepository;
import Sep3.serverdatabase.service.interfaces.WishListRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import sep3.server.SuccessResponse;
import sep3.server.WishListRequest;
import sep3.server.WishListServiceGrpc;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@GrpcService
public class WishListServiceImpl extends WishListServiceGrpc.WishListServiceImplBase {
    private final WishListRepository repository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public WishListServiceImpl(WishListRepository repository, CustomerRepository customerRepository, ItemRepository itemRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void addToWishList(WishListRequest request, StreamObserver<SuccessResponse> responseObserver) {


        try {
            // Step 1: Find the customer and the item
            Optional<Customer> customerFromDatabase = customerRepository.findByUserName(request.getUsername());
            Optional<Item> itemFromDatabase = itemRepository.findById(request.getItemId());


            if (customerFromDatabase.isPresent() && itemFromDatabase.isPresent()) {

                Customer finalCustomer = customerFromDatabase.get();
                Item finalItem = itemFromDatabase.get();


                // Step 2: Check if the customer already has a wish list
                Optional<WishList> existingWishList = repository.findByCustomer(finalCustomer);
                if (existingWishList.isPresent())
                {
                    WishList wishList = existingWishList.get();
                    boolean itemExists = wishList.getItems().stream()
                            .anyMatch(item -> item.getId() == request.getItemId());
                    if (itemExists){
                        responseObserver.onError(Status.ABORTED.withDescription("This item is already on you wishlist").asException());
                    }
                }


                if (existingWishList.isEmpty()) {
                    // Customer does not have a wish list, create a new one
                    Set<Item> finalItems = new HashSet<>();
                    finalItems.add(finalItem);
                    WishList newWishList = new WishList(finalCustomer, finalItems);
                    repository.save(newWishList);
                }
                else {
                    WishList wishList = existingWishList.get();
                    System.out.println("hello i am here");
                    wishList.getItems().add(finalItem);
                    repository.save(wishList);
                }

                SuccessResponse successResponse = SuccessResponse.newBuilder()
                        .setMessage("Item added to wishlist successfully")
                        .build();

                responseObserver.onNext(successResponse);
                responseObserver.onCompleted();
            } else {
                // Either customer or item not found
                responseObserver.onError(new Throwable("Could not find customer or item in the database"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not add an item to wishlist to the database"));
        }
    }

}
