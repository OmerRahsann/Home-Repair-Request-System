package homerep.springy.repository;

import homerep.springy.entity.ServiceRequestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestTemplateRepository extends JpaRepository<ServiceRequestTemplate, Integer> {

    default List<ServiceRequestTemplate> findAllTemplates() {
        List<ServiceRequestTemplate> templates = findAll();
        if (templates.isEmpty()) {
            // We're in a dev environment and service_request_templates is empty.
            // Return some random templates.
            ServiceRequestTemplate unclogSink = new ServiceRequestTemplate();
            unclogSink.setTitle("Kitchen sink needs unclogging");
            unclogSink.setService("Plumbing");
            unclogSink.setDollars(400);

            ServiceRequestTemplate roofLeak = new ServiceRequestTemplate();
            roofLeak.setTitle("Roof is leaking and needs to be patched.");
            roofLeak.setService("Roofwork");
            roofLeak.setDollars(600);
            return List.of(unclogSink, roofLeak);
        }
        return templates;
    }
}
