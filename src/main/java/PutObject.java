import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class PutObject {


    public static void main(String[] args) throws Exception {

        FileInputStream propFile = new FileInputStream( "aws-credentials.properties");
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);

        String bucket_name = "kit-sandbox";
        String file_path = "/Users/ooguro/Documents/git/s3-upload-practice/out/production/resources/hello.txt";
        File file = Paths.get(file_path).toFile();
        String key_name = Paths.get(file_path).getFileName().toString();

        System.out.format("Uploading %s to S3 bucket %s...\n", file_path, bucket_name);
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
        try {
            s3.putObject(bucket_name, key_name, file);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }
}