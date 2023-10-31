package Sep3.serverdatabase.service;


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
    public void postItem(ItemP request,StreamObserver<ItemP> responseObserver ){
        Item item = new Item();

        item.setName(request.getName());
        item.setPrice(item.getPrice());

        repository.save(item);
        responseObserver.onCompleted();
    }
}
