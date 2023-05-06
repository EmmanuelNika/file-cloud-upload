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

    public File file;

    public FileService(File file) {
        this.file = file;
    }

    public String uploadToDigitalOcean() throws IllegalAccessException {

        byte[] byteImage = Base64.getDecoder().decode(this.file.getBase64().getBytes());

        Properties properties = new Properties();

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(PropertiesStatics.PROPERTIES_FILE);
             InputStream is = new ByteArrayInputStream(byteImage)) {
            properties.load(input);

            String accessKey = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.KEY));
            String secretKey = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.SECRET));
            String bucketEndpoint = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.URL));
            String bucketRegion = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.REGION));
            String bucket = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.BUCKET));
            String filepath = properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.PATH)) != null ? properties.getProperty(PropertiesStatics.PROPERTIES_FORMAT.formatted(PropertiesStatics.PROPERTIES_PREFIX, CloudApps.DIGITAL_OCEAN, PropertiesStatics.PATH)) + PropertiesStatics.PATH_SUFFIX.formatted(this.file.getName(), this.file.getFileType()) : "." + PropertiesStatics.PATH_SUFFIX.formatted(this.file.getName(), this.file.getFileType());

            Matcher matcher = FILE_EXTENSION_PATTERN.matcher(this.file.getName() + "." + this.file.getFileType());
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
                om.setContentType(this.file.getFileType());

                space.putObject(bucket, filepath, is, om);

                return space.getUrl(bucket, filepath).toString();
            }

        } catch (IOException e) {
            throw new NotFoundException("Application properties file is missing! %s" + e.getLocalizedMessage());
        }
    }
}
