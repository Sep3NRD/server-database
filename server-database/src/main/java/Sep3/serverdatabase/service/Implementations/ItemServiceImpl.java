package Sep3.serverdatabase.service.Implementations;


import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.service.interfaces.ItemRepository;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sep3.server.*;

import java.util.List;
import java.util.Optional;


@GrpcService
public class ItemServiceImpl extends ItemServiceGrpc.ItemServiceImplBase {
    private final ItemRepository repository;
    private final static Logger LOG =
            LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    public ItemServiceImpl(ItemRepository repository){
        this.repository=repository;
    }


    //This method creates an Item and stores it in the database
    @Override
    public void postItem(ItemP request, StreamObserver<ItemP> responseObserver ){

        try {
            // Create a new Item instance to be saved in the repository
            Item item = new Item();

            // Set properties of the Item from the request
            item.setCategory(request.getCategory());
            item.setDescription(request.getDescription());
            item.setStock(request.getStock());
            item.setPrice(request.getPrice());
            item.setName(request.getName());

            // Save the Item to the repository
            repository.save(item);

            // Notify the client that the item has been successfully added
            responseObserver.onNext(request);
            responseObserver.onCompleted();
        }
        catch (Exception e){
            // Handle exceptions that may occur during item addition
            e.getMessage();

            // Notify the client about the error and provide a generic error message
            responseObserver.onError(new Throwable("Could not add item to the database"));
        }

    }

    //This method gets All the Items from the database
    @Override
    public void getItems(Empty request, StreamObserver<ItemP> responseObserver){

       try{
           // Log the start of the method execution
           LOG.info(">>> executing getProduct()..");
           // Retrieve all items from the repository
           List<Item> items = repository.findAll();

           // Iterate through each item and convert it to the gRPC response type
           for (Item item : items) {
               ItemP itemP = ItemP.newBuilder()
                       .setCategory(item.getCategory())
                       .setDescription(item.getDescription())
                       .setStock(item.getStock())
                       .setPrice(item.getPrice())
                       .setName(item.getName())
                       .setItemId(item.getId())
                       .build();
               // Log the items retrieved from the database
               responseObserver.onNext(itemP);
           }
           // Log the items retrieved from the database
           System.out.println("Items from database>>>>>>>"+items.toString());
           // Notify the client that the operation is completed
           responseObserver.onCompleted();
       }
       catch (Exception e){
           // Log the error message and stack trace
           LOG.error("Error while fetching items from the repository", e);
           // Notify the client about the error and provide a generic error message
           responseObserver.onError (new StatusRuntimeException(Status.INTERNAL.withDescription("Could not get items from the repository")));
       }
    }

    //This method gets a specific Item through its id
    @Override
    public void getItemById(GetItemById request, StreamObserver<ItemResponseP> responseObserver){
        // Extract the item ID from the gRPC request
        int id = request.getItemId();

        try{
            // Attempt to find the item in the repository by ID
            Optional<Item> optionalItem = repository.findById(id);

            if (optionalItem.isPresent()){
                // If the item is found, convert it to the gRPC response type
                Item item = optionalItem.get();

                ItemP itemP = ItemP.newBuilder()
                        .setItemId(item.getId())
                        .setName(item.getName())
                        .setDescription(item.getDescription())
                        .setPrice(item.getPrice())
                        .setCategory(item.getCategory())
                        .setStock(item.getStock())
                        .build();

                // Build the response containing the converted item and send it to the client
                ItemResponseP itemResponseP = ItemResponseP.newBuilder().setItem(itemP).build();
                responseObserver.onNext(itemResponseP);
                responseObserver.onCompleted();
            }
            else {
                // If the item is not found, notify the client with a NOT_FOUND status and a descriptive error message
                String errorMessage = "Item with id " + id + " not found";
                responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
            }
        }

        catch (Exception e){
            // Handle exceptions that may occur during item retrieval
            responseObserver.onError(e);
        }
    }

    //This method updates the parameters "Price" and "Stock" of an Item
    @Override
    public void updateItem(UpdateItemRequest request, StreamObserver<StringValue> responseObserver ){
        // Extract the item ID from the gRPC request
        int id = request.getItemId();

        try {
            // Attempt to find the item in the repository by ID
            Optional<Item> optionalItem = repository.findById(id);
            if (optionalItem.isPresent()) {
                // If the item is found, update its properties with the values from the gRPC request
                Item item = optionalItem.get();
                        item.setPrice(request.getPrice());
                        item.setStock(request.getStock());

                // Save the updated item to the repository
                repository.save(item);

                // Notify the client that the item has been successfully updated
                responseObserver.onNext(StringValue.newBuilder().setValue("Item Updated").build());
                responseObserver.onCompleted();
            }
            else {
                // If the item is not found, notify the client with a NOT_FOUND status and a descriptive error message
                String errorMessage = "Item with id "+ id +" was not found";
                responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
            }
        }
        catch (Exception e){
            // Handle exceptions that may occur during item update
            responseObserver.onError(e);
        }
    }

    //This method deletes an item from the database
    @Override
    public void deleteItemById(DeleteItemRequest request, StreamObserver<Empty> responseObserver) {
        try {
            // Extract the item ID from the gRPC request
            int itemId = request.getItemId();
            // Log the item ID (optional, for debugging purposes)
            System.out.println(itemId);

            // Attempt to find the item in the repository by ID
            Optional<Item> optionalItem = repository.findById(itemId);

            if (optionalItem.isPresent()) {
                // If the item is found, delete it from the repository
                repository.deleteById(itemId);

                // Notify the client that the item has been successfully deleted
                responseObserver.onNext(Empty.getDefaultInstance());
                responseObserver.onCompleted();
            } else {
                // If the item is not found, notify the client with a NOT_FOUND status and a descriptive error message
                responseObserver.onError(new StatusRuntimeException(
                        Status.NOT_FOUND.withDescription("Item with ID " + itemId + " not found")));
            }
        } catch (Exception e) {
            // Notify the client about the error and provide a generic error message
            e.printStackTrace();
            responseObserver.onError(new Throwable("Could not delete item from the database"));
        }
    }




}

