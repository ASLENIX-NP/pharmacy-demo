package ASLENIX.pharmacy.demo.api;

import ASLENIX.pharmacy.demo.servicesImpl.SystemInitializationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemInitializationApi {

    @Autowired
    private SystemInitializationServiceImpl systemInitializationService;


    @PostMapping("/api/system/initialize/{providedKey}")
    public String initializeSystem(@PathVariable String providedKey) {
        // Add your initialization logic here

        try{
            return systemInitializationService.initializeSystem(providedKey);
        }
        catch (RuntimeException e){
            return "Error occurred while initializing the system: " + e.getMessage();
        }
    }

}
