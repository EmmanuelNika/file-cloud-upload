package fileutility;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.mq.model.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileService {

    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("([^\\s]+(\\.(?i)(png|txt|doc|csv|pdf))$)");

    private String name;

    private String fileType;

    private String base64;

    public FileService(String name, String fileType, String base64) {
        this.name = name;
        this.fileType = fileType;
        this.base64 = base64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String uploadToDigitalOcean() throws IllegalAccessException {

        byte[] byteImage = Base64.getDecoder().decode(this.base64.getBytes());

        Properties properties = new Properties();

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(PropertiesStatics.PROPERTIES_FILE);
             InputStream is = new ByteArrayInputStream(byteImage)) {
            properties.load(input);

            String accessKey = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.KEY));
            String secretKey = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.SECRET));
            String bucketEndpoint = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.URL));
            String bucketRegion = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.REGION));
            String bucket = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.BUCKET));
            String filepath = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.PATH)) != null ? properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.PATH)) + PropertiesStatics.PATH_SUFFIX.formatted(this.name, this.fileType) : "." + PropertiesStatics.PATH_SUFFIX.formatted(this.name, this.fileType);

            Matcher matcher = FILE_EXTENSION_PATTERN.matcher(this.name + "." + this.fileType);
            if (Boolean.FALSE.equals(matcher.matches())) {
                throw new IllegalAccessException("Invalid file type");

            } else {

                AWSCredentialsProvider awsCredProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));

                AmazonS3 space = AmazonS3ClientBuilder
                        .standard()
                        .withCredentials(awsCredProvider)
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(bucketEndpoint, bucketRegion)
                        )
                        .build();

                ObjectMetadata om = new ObjectMetadata();
                om.setContentLength(byteImage.length);
                om.setContentType(fileType);

                space.putObject(bucket, filepath, is, om);

                return space.getUrl(bucket, filepath).toString();
            }

        } catch (IOException e) {
            throw new NotFoundException("Application properties file is missing! %s" + e.getLocalizedMessage());
        }
    }
}
