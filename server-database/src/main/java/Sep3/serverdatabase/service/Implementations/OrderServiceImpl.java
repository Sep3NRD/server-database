package Sep3.serverdatabase.service.Implementations;

import Sep3.serverdatabase.model.Address;
import Sep3.serverdatabase.model.Customer;
import Sep3.serverdatabase.model.Item;
import Sep3.serverdatabase.model.Order;
import Sep3.serverdatabase.service.interfaces.AddressRepository;
import Sep3.serverdatabase.service.interfaces.CustomerRepository;
import Sep3.serverdatabase.service.interfaces.ItemRepository;
import Sep3.serverdatabase.service.interfaces.OrderRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
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
//             If an exception occurs during customer creation, handle the error
            System.out.println(e.getMessage());

            // Send an error response to the client with a descriptive message
            responseObserver.onError(new Throwable("Could not add an Order to the database"));
        }











//            Order order = getOrderFields(request);
//
//            Optional<Customer> customerToFind = customerRepository.findByUserName(request.getCustomerUsername());
//
//
//            if (customerToFind.isPresent()) {
//                // Fetch the existing customer or merge the detached customer back into the persistence context
//                Customer existingCustomer = customerToFind.get();
//
//
//                Optional<Address> foundedAddress = existingCustomer.getOtherAddresses().stream().findFirst();
//
//                addressRepository.save(foundedAddress.get());
//                order.setCustomer(existingCustomer);
//                order.setAddress(foundedAddress.get());
//
//                // Check and process items
//                Set<Item> items = processItems(request.getItemsList());
//                order.setItems(items);
//
//                if (order.isConfirmed()) {
//                    // String currentDate = LocalDate.now().toString();
//                    String deliveryDate = LocalDate.now().plusDays(3).toString();
//                    order.setDeliveryDate(deliveryDate.toString());
//                }
//            }
//
//            repository.save(order);
//            OrderP orderP = getOrderPFields(order);
//            OrderResponseP orderResponseP = OrderResponseP.newBuilder()
//                   .setOrder(orderP).build();
//
//
//           responseObserver.onNext(orderResponseP);
//           responseObserver.onCompleted();
//
//        }
//        catch (Exception e) {
//            // If an exception occurs during customer creation, handle the error
//            System.out.println(e.getMessage());
//
//            // Send an error response to the client with a descriptive message
//            responseObserver.onError(new Throwable("Could not add an Order to the database"));
//        }
    }

    @Override
    public void confirmOrder(OrderP request, StreamObserver<ConfirmationResponse> responseObserver){
        try {
            Order order = convertToOrder(request);
            order.setConfirmed(true);
            order.setDeliveryDate(LocalDate.now().plusDays(3).toString());
            addressRepository.save(order.getAddress());
            repository.save(order);
            ConfirmationResponse response = ConfirmationResponse.newBuilder()
                    .setSuccess(order.isConfirmed())
                    .setDeliveryDate(order.getDeliveryDate())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            System.out.println("Error in confirming the order");
            e.printStackTrace();
            // Handle any exceptions or errors
            responseObserver.onError(Status.INTERNAL.withDescription("Error confirming order").asException());
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




    private Set<Item> processItems(List<ItemP> itemPList) {
        Set<Item> items = new HashSet<>();

        for (ItemP itemP : itemPList) {
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

            // Add the item to the set
            items.add(item);
        }

        return items;
    }

    private Set<Item> convertToSetOfItems(CreateOrderP request) {

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
