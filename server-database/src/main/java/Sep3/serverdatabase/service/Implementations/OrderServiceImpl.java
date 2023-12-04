package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.model.Order;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import Sep3.serverdatabase.service.interfaces.OrderRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import sep3.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    private final OrderRepository repository;
    private  final CustomerRepository customerRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository repository, CustomerRepository customerRepository){
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void createOrder(OrderPRequest request, StreamObserver<OrderResponseP> responseObserver){
        try {

           Order order = getOrderFields(request);

            // Fetch the existing customer or merge the detached customer back into the persistence context
            Customer existingCustomer = customerRepository.findByUserName(order.getCustomer().getUserName()).orElse(null);

            if (existingCustomer != null) {
                // Merge the detached customer into the persistence context
                order.setCustomer(existingCustomer);
            } else {
                // If the customer doesn't exist, create a new customer
                order.getCustomer().getOrders().add(order); // Ensure bidirectional relationship
                customerRepository.save(order.getCustomer()); // Save the customer
            }


            System.out.println(">>>>>>>>>"+order);
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
//    private Customer getExistingCustomer(CustomerPRequest.Customer customer) {
//        return  null;
//    }
    private Order getOrderFields(OrderPRequest request) {
        Address address = new Address(
                request.getOrder().getAddress().getDoorNumber(),
                request.getOrder().getAddress().getStreet(),
                request.getOrder().getAddress().getCity(),
                request.getOrder().getAddress().getState(),
                request.getOrder().getAddress().getPostalCode(),
                request.getOrder().getAddress().getCountry()
        );

        String customerUsername = request.getCustomerUsername();
        Optional<Customer> customerToConvert = customerRepository.findByUserName(customerUsername);

        Customer customer = customerToConvert.orElse(null);
        if (customer == null) {
            // If the customer doesn't exist, create a new customer
            customer = new Customer(
                    request.getOrder().getCustomer().getFirstName(),
                    request.getOrder().getCustomer().getLastName(),
                    request.getOrder().getCustomer().getUsername(),
                    request.getOrder().getCustomer().getPassword(),
                    address,
                    request.getOrder().getCustomer().getRole()
            );
        }

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
                customer.getAddress(),
                request.getOrder().getOrderDate(),
                request.getOrder().getDeliveryDate()
        );
    }
    private OrderP getOrderPFields(Order request) {

        AddressP addressP = AddressP.newBuilder()
                .setDoorNumber(request.getCustomer().getAddress().getDoorNumber())
                .setStreet(request.getCustomer().getAddress().getStreet())
                .setCity(request.getCustomer().getAddress().getCity())
                .setState(request.getCustomer().getAddress().getState())
                .setPostalCode(request.getCustomer().getAddress().getPostalCode())
                .setCountry(request.getCustomer().getAddress().getCountry())
                .build();

        CustomerP customerP;
        if (request.getCustomer().getId() != 0) {
            // If the customer already has an ID, use the existing customer
            customerP = CustomerP.newBuilder()
                    .setId(request.getCustomer().getId())
                    .build();
        } else {
            // If the customer doesn't have an ID, create a new customerP
            customerP = CustomerP.newBuilder()
                    .setId(request.getCustomer().getId())
                    .setFirstName(request.getCustomer().getFirstName())
                    .setLastName(request.getCustomer().getLastName())
                    .setUsername(request.getCustomer().getUserName())
                    .setPassword(request.getCustomer().getPassword())
                    .setAddress(addressP)
                    .build();
        }

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
                .setAddress(customerP.getAddress())
                .setOrderDate(request.getOrderDate())
                .setDeliveryDate(request.getDeliveryDate())
                .setTotalPrice(request.getTotalPrice())
                .build();
    }

}
