package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.model.Order;
import Sep3.serverdatabase.model.WishList;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import Sep3.serverdatabase.service.interfaces.ItemRepository;
import Sep3.serverdatabase.service.interfaces.WishListRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import sep3.server.*;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
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


    /**
     * Retrieves the wish list for a specific customer based on the provided username.
     *
     * @param request          The request containing the customer's username.
     * @param responseObserver The response observer for sending the wish list response to the client.
     */
    @Override
    public void getWishList(GetWishListRequest request,  StreamObserver<GetWishListResponse> responseObserver){
        try{
            // Retrieve the customer from the database based on the provided username
            Optional<Customer> optionalCustomer = customerRepository.findByUserName(request.getUsername());

            if (optionalCustomer.isPresent()){
                Customer customer = optionalCustomer.get();
                // Retrieve the wish list associated with the customer
                WishList wishLists = customer.getWishLists();
                // Build the response containing items from the wish list
                GetWishListResponse.Builder responseBuilder = GetWishListResponse.newBuilder();
                for (Item item : wishLists.getItems()) {
                    ItemP itemP = ItemP.newBuilder()
                            .setItemId(item.getId())
                            .setPrice(item.getPrice())
                            .setName(item.getName())
                            .setStock(item.getStock())
                            .setCategory(item.getCategory())
                            .setDescription(item.getDescription())
                            .build();
                    responseBuilder.addItems(itemP);
                }
                // Send the wish list response to the client
                GetWishListResponse response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            // Send an error response to the client with a descriptive message
            responseObserver.onError(new
                    Throwable("Could not get the wish list " +
                    "from database"));
        }
    }
}
