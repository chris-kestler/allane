package de.sixt.allane.kestler.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.sixt.allane.kestler.model.Contract;
import de.sixt.allane.kestler.model.Customer;
import de.sixt.allane.kestler.model.Identifiable;
import de.sixt.allane.kestler.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private RentalService rentalService;

    /**
     * Turns database data into JSON.
     *
     * Because of the small size of this project; this was just put here since its used the most here
     */
    public static <T> String serializeObject(T t){
        try {
            if (t != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public <K extends Serializable, T extends Identifiable<K>> String retrieveSerialized(Class<T> type, K key){
        return serializeObject(rentalService.retrieve(type, key));
    }

// Boilerplate spring methods to get individual rows in tables
    @GetMapping("/customers/get/{id}")
    public String getCustomer(@PathVariable long id) { return retrieveSerialized(Customer.class, id); }

    @GetMapping("/vehicles/get/{vid}")
    public String getVehicle(@PathVariable long vid) { return retrieveSerialized(Vehicle.class, vid); }

    @GetMapping("/contracts/get/{num}")
    public String getContract(@PathVariable long num) { return serializeObject(rentalService.retrieveContract(num)); }

/*
 * The methods below retrieve the entire tables.
 * If I wanted to make this code more efficient, I would CSV instead of JSON. But I did not want to complicate this further
 */

    @GetMapping("/customers/get")
    public List<Customer> getAllCustomers() {
        return rentalService.retrieveAll(Customer.class);
    }

    @GetMapping("/contracts/get")
    public List<Contract> getAllContracts() { return rentalService.retrieveAllContracts(); }

    // Retrieve all vehicles is a bit different as the frontend marks in-use vehicles so they aren't used twice
    @GetMapping("/vehicles/get")
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = rentalService.retrieveAll(Vehicle.class);
        Collection<Long> used = rentalService.retrieveUsedVehicles();
        vehicles.forEach( vehicle -> {
            if(used.contains(vehicle.getId()))
                vehicle.markUsed();
        });
        return vehicles;
    }


    /**
     * Used to update or create new rows of the three tables and return JSON response entity
     *
     * @param t the data to save
     * @return
     * @param <T>
     */
    private <T extends Identifiable<Long>> ResponseEntity<String> postData(T t){
        T data = rentalService.save(t);
        if(data != null) {
            return ResponseEntity.ok("{\"id\":" + t.getId() + "}");
        } else {
//            return ResponseEntity.ofNullable("{error:\"Internal Error\"}");
            return null;
        }
    }

    @PostMapping("/customers/add")
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) { return postData(customer); }

    @PostMapping("/vehicles/add")
    public ResponseEntity<String> createVehicle(@RequestBody Vehicle vehicle) { return postData(vehicle); }

    /**
     * This method differs as it supports posting incomplete vehicle and customer classes. (Only id is needed for example {id:x})
     */
    @PostMapping("/contracts/add")
    public ResponseEntity<String> createContract(@RequestBody Contract contract) {
        Customer customer = rentalService.retrieve(Customer.class, contract.getCustomer().getId());
        Vehicle vehicle = rentalService.retrieve(Vehicle.class, contract.getVehicle().getId());
        contract.setVehicle(vehicle);
        contract.setCustomer(customer);

        return postData(contract);
    }



    /**
     * Used to delete an Identifiable object with a specific key. This merely wraps RentalService.delete to provide a Response
     *
     * @see RentalService.delete
     */
    private <K extends Serializable, T extends Identifiable<K>> ResponseEntity<String> serviceDelete(Class<T> type, K id){
        System.out.println("Deleting "+type.getName()+" "+id);
        if(rentalService.delete(type, id))
            return ResponseEntity.ok("{}");
        else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/customers/delete")
    public ResponseEntity<String> deleteCustomer(@RequestBody Long pid) { return serviceDelete(Customer.class, pid); }

    @PostMapping("/vehicles/delete")
    public ResponseEntity<String> deleteVehicle(@RequestBody Long pid) { return serviceDelete(Vehicle.class, pid); }

    @PostMapping("/contracts/delete")
    public ResponseEntity<String> deleteContract(@RequestBody Long pid) {  return serviceDelete(Contract.class, pid); }
}