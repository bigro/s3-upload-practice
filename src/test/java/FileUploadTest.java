import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class FileUploadTest {
    @Test
    public void S3にファイルをアップロードできる() throws Exception {
        setProperty();

        String bucketName = "kit-sandbox";
        String filePath = getClass().getClassLoader().getResource("hello.txt").getPath();
        File file = Paths.get(filePath).toFile();
        String keyName = Paths.get(filePath).getFileName().toString();

        System.out.format("Uploading %s to S3 bucket %s...\n", filePath, bucketName);
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
        try {
            s3.putObject(bucketName, keyName, file);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }

    private void setProperty() throws IOException {
        FileInputStream propFile = new FileInputStream( "aws-credentials.properties");
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);
    }
}
