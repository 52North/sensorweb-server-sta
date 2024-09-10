//package org.n52.sta.data.cloudnative.test.dao;
//
//import io.minio.MakeBucketArgs;
//import io.minio.MinioClient;
//import io.minio.UploadObjectArgs;
//import io.minio.errors.MinioException;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.stereotype.Component;
//import org.springframework.test.context.ActiveProfiles;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Container;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.util.stream.Stream;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ActiveProfiles("cloudnative")
//@Component
//public class MinioContainerManager {
//
//    @Container
//    private static final GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
//            .withEnv("MINIO_ACCESS_KEY", "minioadmin")
//            .withEnv("MINIO_SECRET_KEY", "minioadmin")
//            .withCommand("server /data")
//            .withExposedPorts(9000);
//    public static MinioClient minioClient;
//
//
//    //@BeforeAll
//    public static void setUp() {
//        minio.start();
//        minioClient = MinioClient.builder()
//                .endpoint(minio.getHost(), minio.getMappedPort(9000), false)
//                .credentials("minioadmin", "minioadmin")
//                .build();
//
//        try {
//            minioClient.makeBucket(MakeBucketArgs.builder().bucket("52n-sta").build());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // upload parquet files
//        String directoryPath = "src/test/resources/52n-sta-parquet-files";
//
//        try (Stream<Path> filePaths = Files.walk(Paths.get(directoryPath))) {
//            filePaths
//                    .filter(Files::isRegularFile)
//                    .filter(path -> path.toString().endsWith(".parquet"))
//                    .forEach(file -> uploadFileToMinio(file, "52n-sta"));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    private static void uploadFileToMinio(Path filePath, String bucket) {
//        try {
//            MinioContainerManager.minioClient.uploadObject(
//                    UploadObjectArgs.builder()
//                            .bucket(bucket)
//                            .object(filePath.getFileName().toString())
//                            .filename(filePath.toString())
//                            .build()
//            );
//            System.out.println("Uploaded: " + filePath.getFileName().toString());
//        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
//    //@AfterAll
//    public static void tearDown() {
//        System.out.println("Stopping Minio container");
//        if (minio != null) {
//            minio.stop();
//        }
//    }
//}
