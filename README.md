# s3-upload-practice
S3へのファイルアップロードの練習です。

[AWS SDK for Java 開発者ガイド](https://docs.aws.amazon.com/ja_jp/sdk-for-java/v1/developer-guide/welcome.html)

## マルチパートアップロード
マルチパートアップロード API を使用すると、大容量オブジェクトをいくつかに分けてアップロードできるようになります。

[マルチパートアップロードの概要](https://docs.aws.amazon.com/ja_jp/AmazonS3/latest/dev/mpuoverview.html)

## GradleでAWS SDKを取得する
[Gradle とともに SDK を使用する](https://docs.aws.amazon.com/ja_jp/sdk-for-java/v1/developer-guide/setup-project-gradle.html)

## リージョン一覧
[AWS のリージョンとエンドポイント](https://docs.aws.amazon.com/ja_jp/general/latest/gr/rande.html)

## 認証情報の設定
[認証情報の設定](https://docs.aws.amazon.com/ja_jp/sdk-for-java/v1/developer-guide/credentials.html)

- コードで設定
```
BasicAWSCredentials awsCreds = new BasicAWSCredentials("access_key_id", "secret_key_id");
AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .build();
```

- Java のシステムプロパティ

システムプロパティで `aws.accessKeyId` と `aws.secretKey` で設定すると `SystemPropertiesCredentialsProvider` を使用してプロパティを読み込んでくれます。
