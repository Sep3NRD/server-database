package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.model.Order;
import Sep3.serverdatabase.service.interfaces.AddressRepository;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import Sep3.serverdatabase.service.interfaces.ItemRepository;
import Sep3.serverdatabase.service.interfaces.OrderRepository;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sep3.server.*;

import java.time.LocalDate;
import java.util.*;

@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    private final OrderRepository repository;
    private  final CustomerRepository customerRepository;
    private  final ItemRepository itemRepository;
    private final AddressRepository addressRepository;


    @Autowired
    public OrderServiceImpl(OrderRepository repository, CustomerRepository customerRepository, ItemRepository itemRepository,AddressRepository addressRepository){
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.itemRepository = itemRepository;
        this.addressRepository=addressRepository;

    }

    @Override
    public void createOrder(CreateOrderP request, StreamObserver<SuccessMessage> responseObserver){

        try {
            Customer finalCustomer = null;
            Optional<Customer> customerFromDatabase = customerRepository.findByUserName(request.getCustomerUsername());
            if (customerFromDatabase.isPresent())
                finalCustomer = customerFromDatabase.get();

            Address finalAddress = null;
            Optional<Address> addressFromDatabase = addressRepository.findById(request.getAddressId());
            if (addressFromDatabase.isPresent())
                finalAddress = addressFromDatabase.get();

            Set<Item> finalItems = convertToSetOfItems(request);

            Order orderToSave = new Order(finalCustomer, finalItems, finalAddress, request.getOrderDate(), "Waiting for confirmation");

            repository.save(orderToSave);
            SuccessMessage successMessage = SuccessMessage.newBuilder().setMessage("SUCCESS").build();

            responseObserver.onNext(successMessage);
            responseObserver.onCompleted();

        }
        catch (Exception e)
        {
            // f an exception occurs during customer creation, handle the error
            System.out.println(e.getMessage());

            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not add an Order to the database"));
        }

    }

    @Override
    public void confirmOrder(OrderIdToConfirm request, StreamObserver<ConfirmationResponse> responseObserver){
        try {
            Optional<Order> optionalOrder = repository.findById(request.getOrderId());

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setConfirmed(true);
                order.setDeliveryDate(LocalDate.now().plusDays(3).toString());

//                // Assuming order has a reference to an address
//                Address address = order.getAddress();
//                addressRepository.save(address);

                repository.save(order);

                ConfirmationResponse response = ConfirmationResponse.newBuilder()
                        .setSuccess(order.isConfirmed())
                        .setDeliveryDate(order.getDeliveryDate())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Order not found with ID: " + request.getOrderId()).asException());
            }

        }
        catch(Exception e){
            System.out.println("Error in confirming the order");
            e.printStackTrace();
            // Handle any exceptions or errors
            responseObserver.onError(Status.INTERNAL.withDescription("Error confirming order").asException());
        }
    }

    @Override
    @Transactional
    public void getAllOrders(Empty request, StreamObserver<AllOrdersResponse> responseObserver){
        try{
            List<Order> orders = repository.findAll();

            AllOrdersResponse.Builder responseBuilder = AllOrdersResponse.newBuilder();

            for (Order order : orders) {
                Set<Address> addresses = order.getCustomer().getOtherAddresses();

                OrderP.Builder orderPBuilder = OrderP.newBuilder()
                        .setId(order.getId())
                        .setOrderDate(order.getOrderDate())
                        .setDeliveryDate(order.getDeliveryDate())
                        .setIsConfirmed(order.isConfirmed())
                        .setTotalPrice(order.getTotalPrice());

                Customer customer = order.getCustomer();
                CustomerP.Builder customerPBuilder = CustomerP.newBuilder()
                        .setId(customer.getId())
                        .setFirstName(customer.getFirstName())
                        .setLastName(customer.getLastName())
                        .setUsername(customer.getUserName());

                for (Address address : addresses) {
                    AddressP addressP = AddressP.newBuilder()
                            .setId(address.getId())
                            .setDoorNumber(address.getDoorNumber())
                            .setCity(address.getCity())
                            .setPostalCode(address.getPostalCode())
                            .setState(address.getState())
                            .setStreet(address.getStreet())
                            .setCountry(address.getCountry())
                            .build();
                    customerPBuilder.setAddress(addressP);
                }

                customerPBuilder.setAddress(customerPBuilder.getAddress());
                orderPBuilder.setCustomer(customerPBuilder.build());

                for (Item item : order.getItems()) {
                    ItemP itemP = ItemP.newBuilder()
                            .setItemId(item.getId())
                            .setPrice(item.getPrice())
                            .setName(item.getName())
                            .setStock(item.getStock())
                            .setCategory(item.getCategory())
                            .setDescription(item.getDescription())
                            .build();
                    orderPBuilder.addItems(itemP);
                }

                Address address = order.getAddress();
                AddressP addressP = AddressP.newBuilder()
                        .setId(address.getId())
                        .setDoorNumber(address.getDoorNumber())
                        .setCity(address.getCity())
                        .setPostalCode(address.getPostalCode())
                        .setState(address.getState())
                        .setStreet(address.getStreet())
                        .setCountry(address.getCountry())
                        .build();
                orderPBuilder.setAddress(addressP);

                OrderP orderP = orderPBuilder.build();
                responseBuilder.addOrdersP(orderP);
            }

            AllOrdersResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e){
            System.out.println(e.getMessage());

            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not get the order from database"));
        }

    }

    @Override
    public void getAllOrdersWithCustomerId(CustomerIdRequest request, StreamObserver<AllOrdersResponse> responseObserver){
        try{

            Optional<Customer> optionalCustomer = customerRepository.findById(request.getCustomerId());
            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();
                List<Order> orders = customer.getOrders();

                AllOrdersResponse.Builder responseBuilder = AllOrdersResponse.newBuilder();

                for (Order order : orders) {
                    Set<Address> addresses = order.getCustomer().getOtherAddresses();

                    OrderP.Builder orderPBuilder = OrderP.newBuilder()
                            .setId(order.getId())
                            .setOrderDate(order.getOrderDate())
                            .setDeliveryDate(order.getDeliveryDate())
                            .setIsConfirmed(order.isConfirmed())
                            .setTotalPrice(order.getTotalPrice());

                    Customer customerObj = order.getCustomer();
                    CustomerP.Builder customerPBuilder = CustomerP.newBuilder()
                            .setId(customerObj.getId())
                            .setFirstName(customerObj.getFirstName())
                            .setLastName(customerObj.getLastName())
                            .setUsername(customerObj.getUserName());

                    for (Address address : addresses) {
                        AddressP addressP = AddressP.newBuilder()
                                .setId(address.getId())
                                .setDoorNumber(address.getDoorNumber())
                                .setCity(address.getCity())
                                .setPostalCode(address.getPostalCode())
                                .setState(address.getState())
                                .setStreet(address.getStreet())
                                .setCountry(address.getCountry())
                                .build();
                        customerPBuilder.setAddress(addressP);
                    }

                    customerPBuilder.setAddress(customerPBuilder.getAddress());
                    orderPBuilder.setCustomer(customerPBuilder.build());

                    for (Item item : order.getItems()) {
                        ItemP itemP = ItemP.newBuilder()
                                .setItemId(item.getId())
                                .setPrice(item.getPrice())
                                .setName(item.getName())
                                .setStock(item.getStock())
                                .setCategory(item.getCategory())
                                .setDescription(item.getDescription())
                                .build();
                        orderPBuilder.addItems(itemP);
                    }

                    Address address = order.getAddress();
                    AddressP addressP = AddressP.newBuilder()
                            .setId(address.getId())
                            .setDoorNumber(address.getDoorNumber())
                            .setCity(address.getCity())
                            .setPostalCode(address.getPostalCode())
                            .setState(address.getState())
                            .setStreet(address.getStreet())
                            .setCountry(address.getCountry())
                            .build();
                    orderPBuilder.setAddress(addressP);

                    OrderP orderP = orderPBuilder.build();
                    responseBuilder.addOrdersP(orderP);
                }

                AllOrdersResponse response = responseBuilder.build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not get the order from database"));
        }
    }
    private Order convertToOrder(OrderP request) {
        Address address = new Address(
                request.getAddress().getDoorNumber(),
                request.getAddress().getStreet(),
                request.getAddress().getCity(),
                request.getAddress().getState(),
                request.getAddress().getPostalCode(),
                request.getAddress().getCountry()
        );
        String customerUsername = request.getCustomer().getUsername();
        Optional<Customer> customerToConvert = customerRepository.findByUserName(customerUsername);
        Customer customer = customerToConvert.orElse(null);
        if (customer == null) {
            // If the customer doesn't exist, create a new customer
            Set<Address> addressSet = new HashSet<>();
            customer = new Customer(
                    request.getCustomer().getFirstName(),
                    request.getCustomer().getLastName(),
                    request.getCustomer().getUsername(),
                    request.getCustomer().getPassword(),
                    request.getCustomer().getRole()
            );
        }
        Set<Item> items = new HashSet<>();
        for (ItemP itemP : request.getItemsList()) {
            // Check if the item with the same ID already exists in the database
            Optional<Item> existingItem = itemRepository.findById(itemP.getItemId());
            Item item;
            if (existingItem.isPresent()) {
                // If the item exists, reuse it
                item = existingItem.get();
            } else {
                // If the item doesn't exist, create a new one
                item = new Item(
                        itemP.getName(),
                        itemP.getPrice(),
                        itemP.getCategory(),
                        itemP.getStock(),
                        itemP.getDescription()
                );
            }
            items.add(item);
        }
        return new Order(
                customer,
                items,
                address,
                request.getOrderDate(),
                request.getDeliveryDate()
        );
    }


    private Set<Item> convertToSetOfItems(CreateOrderP request) {
        Set<Item> items = new HashSet<>();

        for (ItemP orderItemP : request.getItemsList()) {
            Optional<Item> existingItem = items.stream()
                    .filter(item -> item.getId()==orderItemP.getItemId())
                    .findFirst();

            if (existingItem.isPresent()) {
                // Item already exists in the set, update its quantity
                Item item = existingItem.get();
                int newQuantity = item.getQuantity() + orderItemP.getQuantity();
                item.setQuantity(newQuantity);
            } else {
                // Item does not exist in the set, add a new one with the specified quantity
                Item newItem = new Item(
                        orderItemP.getName(),
                        orderItemP.getPrice(),
                        orderItemP.getCategory(),
                        orderItemP.getStock(),
                        orderItemP.getDescription()
                );
                newItem.setId(orderItemP.getItemId());
                newItem.setQuantity(orderItemP.getQuantity());
                items.add(newItem);
            }
        }

        return items;
    }
    private OrderP getOrderPFields(Order request) {
        AddressP addressP = AddressP.newBuilder()
                .setDoorNumber(request.getAddress().getDoorNumber())
                .setStreet(request.getAddress().getStreet())
                .setCity(request.getAddress().getCity())
                .setState(request.getAddress().getState())
                .setPostalCode(request.getAddress().getPostalCode())
                .setCountry(request.getAddress().getCountry())
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

        Set<ItemP> itemsP = new HashSet<>();
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
