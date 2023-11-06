package homerep.springy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "homerep.service-request.images")
public class ServiceRequestPictureConfig {
    /**
     * The maximum number of pixels for the width and height of the picture.
     */
    private int maxSizePixels = 1920;

    /**
     * The maximum number pictures that can be attached to a single service request.
     */
    private int maxNumPictures = 10;

    public int getMaxSizePixels() {
        return maxSizePixels;
    }

    public void setMaxSizePixels(int maxSizePixels) {
        this.maxSizePixels = maxSizePixels;
    }

    public int getMaxNumPictures() {
        return maxNumPictures;
    }

    public void setMaxNumPictures(int maxNumPictures) {
        this.maxNumPictures = maxNumPictures;
    }
}
