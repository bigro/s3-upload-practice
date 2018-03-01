import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.services.s3.transfer.internal.TransferManagerUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void ファイルなしで文字列でのアップロードができる() throws Exception {
        setProperty();

        String bucketName = "kit-sandbox";

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
        try {
            s3.putObject(bucketName, "hello.txt", "content");
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
        // dd if=/dev/zero of=file bs=1024 count=10000 で作ったファイルなので開くと重いよ
        String filePath = getClass().getClassLoader().getResource("multipart.txt").getPath();
        File file = Paths.get(filePath).toFile();
        String keyName = Paths.get(filePath).getFileName().toString();

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        TransferManager transferManager = TransferManagerBuilder
                .standard()
                // デフォルト5Mなので6Mにしてみる
                .withMinimumUploadPartSize(6L * Constants.MB)
                // デフォルト16Mでmultipart.txtが約10Mなのでmultipartになるように閾値を下げておく
                // 通常はマルチパートサイズより閾値の方が上のはず
                .withMultipartUploadThreshold(5L * Constants.MB)
                // リージョンを指定してないと落ちるのでAmazonS3経由で設定してるが、公式では設定なしでいけてる・・・
                .withS3Client(s3)
                .build();

        PutObjectRequest putObjectRequest = new PutObjectRequest(existingBucketName, keyName, file);

        // 1つのpartのサイズ
        assertThat(TransferManagerUtils.calculateOptimalPartSize(putObjectRequest, transferManager.getConfiguration())).isEqualTo(6L * Constants.MB);
        // マルチパートアップロードとして処理する必要があるか
        assertThat(TransferManagerUtils.shouldUseMultipartUpload(putObjectRequest, transferManager.getConfiguration())).isEqualTo(true);
        // 並列パートアップロードを使用できるか
        assertThat(TransferManagerUtils.isUploadParallelizable(putObjectRequest, false)).isEqualTo(true);

        // アップロード
        Upload upload = transferManager.upload(putObjectRequest);

        // アップロード完了まで待つ
        upload.waitForCompletion();

        // 完了後のステータスがCompletedになっている
        assertThat(upload.getState()).isEqualTo(Transfer.TransferState.Completed);

        // 完了したらTransferManagerインスタンスをシャットダウンする。
        transferManager.shutdownNow();
    }

    @Test
    public void 複数ファイルを同時にアップロードできる() throws Exception {
        setProperty();

        String existingBucketName = "kit-sandbox";
        File multipartFile = createFile("multipart.txt");
        String directory = multipartFile.getParent();
        File helloFile = new File(directory, "hello.txt");

        // アプロード対象のファイルが格納されてるディレクトリのパスでFileのインスタンスを作成します。（TransferManager#uploadFileListの引数）
        File fileDirectory = new File(directory);

        // アップロード対象のFileのListを作成します。（TransferManager#uploadFileListの引数）
        List<File> files = Arrays.asList(multipartFile, helloFile);

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        TransferManager transferManager = TransferManagerBuilder
                .standard()
                .withS3Client(s3)
                .build();


        // 対象のバケット名、S3に格納するディレクトリ、アプロード対象のファイルが格納されているディレクトリの値を持ったFileインスタンス、アップロード対象のFileのリストを引数に渡します。
        MultipleFileUpload multipleFileUpload = transferManager.uploadFileList(existingBucketName, "upload-list", fileDirectory, files);

        multipleFileUpload.waitForCompletion();

        // 完了後のステータスがCompletedになっている
        assertThat(multipleFileUpload.getState()).isEqualTo(Transfer.TransferState.Completed);

        transferManager.shutdownNow();
    }

    private File createFile(String fileName) {
        String path = getClass().getClassLoader().getResource(fileName).getPath();
        return Paths.get(path).toFile();
    }

    private void setProperty() throws IOException {
        FileInputStream propFile = new FileInputStream("aws-credentials.properties");
        Properties p = new Properties(System.getProperties());
        p.load(propFile);
        System.setProperties(p);
    }
}
