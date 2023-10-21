# Repair Radar Backend

### External dependencies
[ImageMagick](https://imagemagick.org/script/download.php): Needs to be installed. This is used for resizing and striping metadata from images.\
`other/policy.xml` must be installed into a path listed [here](https://imagemagick.org/script/resources.php#configure).
This policy file restricts the allowed image formats for security and adds resource limits for ImageMagick.  

### Running
Linux: `./gradlew bootRun`\
Windows: `gradlew.bat bootRun`

With IntelliJ IDEA, you can run the `bootRun` Gradle task or run the `homerep.springy.SpringyApplication` main class.

### Unit Tests
Linux: `./gradlew test`\
Windows: `gradlew.bat test`

### Deploying
Linux: `./gradlew build`\
Windows: `gradlew.bat build`

The jar produced is located at `build/libs/homerep-<version>.jar`.\
An example config is located at `src/main/resources/application-prod-example.yml` which can be copied+edited for the
production environment.

To start the backend with the newly created config:\
`java -jar homerep-<version>.jar --spring.config.location=classpath:application.yml,<path to new config.yml>`