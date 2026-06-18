package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.ExpiryDateNotification;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.imageio.plugins.tiff.ExifGPSTagSet;
import java.util.List;

public interface ExpiryDateNotificationRepository extends JpaRepository<ExpiryDateNotification , Long> {
    List<ExpiryDateNotification> findByActionTakenFalse();


}
