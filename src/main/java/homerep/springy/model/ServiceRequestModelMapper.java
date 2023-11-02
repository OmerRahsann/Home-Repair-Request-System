package homerep.springy.model;

import homerep.springy.model.ServiceRequestModel;
import homerep.springy.entity.ServiceRequest;
import java.util.List;
import java.util.stream.Collectors;

@Component 
public class ServiceRequestModelMapper {

    /**
     * Maps a ServiceRequest entity to a ServiceRequestModel.
     * 
     * @param entity The ServiceRequest entity to be mapped.
     * @return The mapped ServiceRequestModel.
     */
    public ServiceRequestModel toModel(ServiceRequest entity) {
        return new ServiceRequestModel(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getService(),
            entity.getStatus(),
            entity.getDollars(),
            entity.getAddress(),
            entity.getPictures(),
            entity.getCreationDate()
        );
    }

    /**
     * Maps a list of ServiceRequest entities to a list of ServiceRequestModels.
     * 
     * @param entities The list of ServiceRequest entities to be mapped.
     * @return The list of mapped ServiceRequestModels.
     */
    public List<ServiceRequestModel> toModelList(List<ServiceRequest> entities) {
        return entities.stream()
                       .map(this::toModel)
                       .collect(Collectors.toList());
    }
}

