package ASLENIX.pharmacy.demo.services;

public interface SystemInitializationService {
    String initializeSystem(String providedKey);

    void updatedDefaultUserAsActiveAdmin();

    void addDefaultUser();
}
