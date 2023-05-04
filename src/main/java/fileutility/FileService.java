package fileutility;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileService {

    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("([^\\s]+(\\.(?i)(txt|doc|csv|pdf))$)");

    public String createFile(String name, String fileType, String base64) throws IllegalAccessException {

        String url = "";

        Matcher matcher = FILE_EXTENSION_PATTERN.matcher(fileType);
        if (Boolean.FALSE.equals(matcher.matches())) {
            throw new IllegalAccessException("Invalid file type");
        }

        AWSCredentialsProvider awscp = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials("DO_ACCESS_KEY", "DO_SECRET_KEY")
        );

        AmazonS3 space = AmazonS3ClientBuilder
                .standard()
                .withCredentials(awscp)
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("BUCKET_ENDPOINT", "BUCKET_REGION")
                )
                .build();

        byte[] byteImage = Base64.getDecoder().decode(base64.getBytes());

        InputStream is = new ByteArrayInputStream(byteImage);
        ObjectMetadata om = new ObjectMetadata();
        om.setContentLength(byteImage.length);
        om.setContentType(fileType);

        String filepath = "/somefolder/someanotherfolder/testfile.jpg";
        space.putObject("BUCKET_NAME", filepath, is, om);
        return space.getUrl("BUCKET_NAME", filepath).toString();

    }
}
