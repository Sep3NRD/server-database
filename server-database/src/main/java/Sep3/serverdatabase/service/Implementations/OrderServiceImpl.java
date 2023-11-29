package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.model.Order;
import Sep3.serverdatabase.service.interfaces.OrderRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sep3.server.*;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    private final OrderRepository repository;


    @Autowired
    public OrderServiceImpl(OrderRepository repository){
        this.repository = repository;
    }

    @Override
    public void createOrder(OrderPRequest request, StreamObserver<OrderResponseP> responseObserver){
        try {
           Order order = getOrderFields(request);
            repository.save(order);
            OrderP orderP = getOrderPFields(order);
            OrderResponseP orderResponseP = OrderResponseP.newBuilder()
                   .setOrder(orderP).build();


           responseObserver.onNext(orderResponseP);
           responseObserver.onCompleted();

        }
        catch (Exception e) {
            // If an exception occurs during customer creation, handle the error
            System.out.println(e.getMessage());

            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not add an Order to the database"));
        }
    }
    private static Order getOrderFields(OrderPRequest request) {
        Address address = new Address(
                request.getCustomer().getAddress().getDoorNumber(),
                request.getCustomer().getAddress().getStreet(),
                request.getCustomer().getAddress().getCity(),
                request.getCustomer().getAddress().getState(),
                request.getCustomer().getAddress().getPostalCode(),
                request.getCustomer().getAddress().getCountry()
        );


        Customer customer = new Customer(
                request.getCustomer().getFirstName(),
                request.getCustomer().getLastName(),
                request.getCustomer().getUsername(),
                request.getCustomer().getPassword(),
                address,
                request.getCustomer().getRole()
        );
        List<Item> items = new ArrayList<>();
        for (ItemP itemP : request.getItemsList()) {
            Item item = new Item(
                    itemP.getName(),
                    itemP.getPrice(),
                    itemP.getCategory(),
                    itemP.getStock(),
                    itemP.getDescription()
            );
            items.add(item);

        }

        return new Order(
                customer,
                items,
                address,
                request.getOrder().getOrderDate(),
                request.getOrder().getDeliveryDate()
        );
    }
    private static OrderP getOrderPFields(Order request) {

        AddressP addressP = AddressP.newBuilder()
                .setDoorNumber(request.getCustomer().getAddress().getDoorNumber())
                .setStreet(request.getCustomer().getAddress().getStreet())
                .setCity(request.getCustomer().getAddress().getCity())
                .setState(request.getCustomer().getAddress().getState())
                .setPostalCode(request.getCustomer().getAddress().getPostalCode())
                .setCountry(request.getCustomer().getAddress().getCountry())
                .build();

        CustomerP customerP = CustomerP.newBuilder()
                .setId(request.getCustomer().getId())
                .setFirstName(request.getCustomer().getFirstName())
                .setLastName(request.getCustomer().getLastName())
                .setUsername(request.getCustomer().getUserName())
                .setPassword(request.getCustomer().getPassword())
                .setAddress(addressP)
                .build();

        List<ItemP> itemsP = new ArrayList<>();
        for (Item item: request.getItems()){
            ItemP itemP = ItemP.newBuilder()
                    .setItemId(item.getId())
                    .setName(item.getName())
                    .setPrice(item.getPrice())
                    .setCategory(item.getCategory())
                    .setStock(item.getStock())
                    .setDescription(item.getDescription())
                    .build();
            itemsP.add(itemP);
        }

        return OrderP.newBuilder()
                .setId(request.getId())
                .setCustomer(customerP)
                .addAllItems(itemsP)
                .setAddress(addressP)
                .setOrderDate(request.getOrderDate())
                .setDeliveryDate(request.getDeliveryDate())
                .setTotalPrice(request.getTotalPrice())
                .build();
    }

}
