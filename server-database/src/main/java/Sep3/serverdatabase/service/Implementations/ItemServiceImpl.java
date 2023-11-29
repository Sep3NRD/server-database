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

    @Override
    public void getItems(Empty request, StreamObserver<ItemP> responseObserver){

       try{
           LOG.info(">>> executing getProduct()..");
           List<Item> items = repository.findAll();

           for (Item item : items) {
               ItemP itemP = ItemP.newBuilder()
                       .setCategory(item.getCategory())
                       .setDescription(item.getDescription())
                       .setStock(item.getStock())
                       .setPrice(item.getPrice())
                       .setName(item.getName())
                       .setItemId(item.getId())
                       .build();

               responseObserver.onNext(itemP);
           }

           System.out.println("Items from database>>>>>>>"+items.toString());
           responseObserver.onCompleted();
       }
       catch (Exception e){
           LOG.error("Error while fetching items from the repository", e);
           responseObserver.onError (new StatusRuntimeException(Status.INTERNAL.withDescription("Could not get items from the repository")));
       }
    }

    @Override
    public void getItemById(GetItemById request, StreamObserver<ItemResponseP> responseObserver){
        int id = request.getItemId();

        try{
            Optional<Item> optionalItem = repository.findById(id);
            if (optionalItem.isPresent()){
                Item item = optionalItem.get();

                ItemP itemP = ItemP.newBuilder()
                        .setItemId(item.getId())
                        .setName(item.getName())
                        .setDescription(item.getDescription())
                        .setPrice(item.getPrice())
                        .setCategory(item.getCategory())
                        .setStock(item.getStock())
                        .build();

                ItemResponseP itemResponseP = ItemResponseP.newBuilder().setItem(itemP).build();
                responseObserver.onNext(itemResponseP);
                responseObserver.onCompleted();
            }
            else {
                String errorMessage = "Item with id " + id + " not found";
                responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
            }
        }

        catch (Exception e){
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateItem(UpdateItemRequest request, StreamObserver<StringValue> responseObserver ){
        int id = request.getItemId();

        try {
            Optional<Item> optionalItem = repository.findById(id);
            if (optionalItem.isPresent()) {
                Item item = optionalItem.get();

                        item.setPrice(request.getPrice());
                        item.setStock(request.getStock());

                repository.save(item);


                responseObserver.onCompleted();
            }
            else {
                String errorMessage = "Item was not found";
                responseObserver.onError(Status.NOT_FOUND.withDescription(errorMessage).asException());
            }
        }
        catch (Exception e){
            responseObserver.onError(e);
        }
    }
}

