package homerep.springy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class ImageStorageConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "homerep.image-storage")
    public ImageStorageConfig imageStorageConfig() {
        return new ImageStorageConfig();
    }

    public static class ImageStorageConfig {
        private double quality = 90.0;

        private String storageRoot = Path.of("storage-root").toAbsolutePath().toString();

        public double getQuality() {
            return quality;
        }

        public void setQuality(double quality) {
            this.quality = quality;
        }

        public String getStorageRoot() {
            return storageRoot;
        }

        public void setStorageRoot(String storageRoot) {
            this.storageRoot = storageRoot;
        }
    }
}
