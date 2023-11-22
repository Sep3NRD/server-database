package Sep3.serverdatabase.service.Implementations;


import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.service.interfaces.ItemRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sep3.server.ItemServiceGrpc;
import sep3.server.ItemP;


@GrpcService
public class ItemServiceImpl extends ItemServiceGrpc.ItemServiceImplBase {
    private final ItemRepository repository;

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

}
