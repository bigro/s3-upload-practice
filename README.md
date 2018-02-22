# s3-upload-practice
S3へのファイルアップロードの練習です。

[AWS SDK for Java 開発者ガイド](https://docs.aws.amazon.com/ja_jp/sdk-for-java/v1/developer-guide/welcome.html)

## 前準備
- リポジトリをcloneして、直下に以下の内容の `aws-credentials.properties` というファイルを作成
```
aws.accessKeyId=[[access_key_id]] --対象のIAMのアクセスキー
aws.secretKey=[[secret_key_id]] -- 対象のIAMのシークレットキー
```


## Amazon S3とは
[Amazon S3とは](https://docs.aws.amazon.com/ja_jp/AmazonS3/latest/dev/Welcome.html)

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

## アップロード
`AmazonS3#putObject` でアップロードできます。

- 第一引数 -> バケット名
- 第二引数 -> S3にアップロードした後のファイル名。 `/` 区切りにすればフルパス指定もでき、ディレクトリがなければ作成される。([オブジェクトキーとメタデータ](https://docs.aws.amazon.com/ja_jp/AmazonS3/latest/dev/UsingMetadata.html))
- 第三引数 -> `File` クラス。 `String` もあるが、こちらはファイルそのものではなく指定した文字列をファイルの内容としてアップロードする。
```
s3.putObject(bucketName, keyName, file);
```

また、 `PutObjectRequest` でまとめて渡すことも可能です。
```
PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, file);
s3.putObject(putObjectRequest);
```
