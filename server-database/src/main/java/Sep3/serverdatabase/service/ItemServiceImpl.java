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


          try {
              Item item = new Item(request.getName(),request.getPrice());
              repository.save(item);
              responseObserver.onNext(request);
              responseObserver.onCompleted();
          }
          catch (Exception e){
              e.getMessage();
              responseObserver.onError(new Throwable("Could not add item to the database"));
          }

    }
}
