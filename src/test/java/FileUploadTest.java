import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class FileUploadTest {
    @Test
    public void S3に1つのアップロードができる() throws Exception {
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

    @Test
    public void S3にマルチパートアップロードができる() throws Exception {
        setProperty();

        String existingBucketName = "kit-sandbox";
        String filePath           = getClass().getClassLoader().getResource("hello.txt").getPath();
        String keyName            = Paths.get(filePath).getFileName().toString();

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        TransferManager transferManager = TransferManagerBuilder
                .standard()
                .withS3Client(s3)
                .build();
        System.out.println("Hello");

        // TransferManager processes all transfers asynchronously,
        // so this call will return immediately.
        Upload upload = transferManager.upload(existingBucketName, keyName, new File(filePath));
        TransferProgress progress = upload.getProgress();
        System.out.println("progress: " + progress.getPercentTransferred());
        System.out.println("Hello2");

        try {
            // Or you can block and wait for the upload to finish
            upload.waitForCompletion();
            System.out.println("Upload complete.");
        } catch (AmazonClientException amazonClientException) {
            System.out.println("Unable to upload file, upload was aborted.");
            amazonClientException.printStackTrace();
        }
    }

    private void setProperty() throws IOException {
        FileInputStream propFile = new FileInputStream( "aws-credentials.properties");
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);
    }
}
