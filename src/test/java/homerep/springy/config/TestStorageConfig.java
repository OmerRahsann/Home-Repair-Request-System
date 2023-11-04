package homerep.springy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class TestStorageConfig {

    public static final Path TEST_STORAGE_ROOT = Path.of("test-storage-root").toAbsolutePath();

    @Bean
    @Primary
    public ImageStorageConfig configureTestStorage(ImageStorageConfig config) throws IOException {
        FileSystemUtils.deleteRecursively(TEST_STORAGE_ROOT);
        config.setStorageRoot(TEST_STORAGE_ROOT.toString());
        return config;
    }
}
