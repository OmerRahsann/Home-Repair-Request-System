package homerep.springy.repository;

import homerep.springy.entity.ImageInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageInfoRepository extends JpaRepository<ImageInfo, UUID> {
}
