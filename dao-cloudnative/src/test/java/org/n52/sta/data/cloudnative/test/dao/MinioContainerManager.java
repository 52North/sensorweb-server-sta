package org.n52.sta.data.cloudnative.test.dao;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class MinioContainerManager {

    private static GenericContainer<?> minioContainer;
    public static MinioClient minioClient;

    @BeforeAll
    public static void setUp() {
        minioContainer = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
                .withExposedPorts(9000, 9001)
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                .withCommand("server /data --console-address \":9001\"");

        minioContainer.start();

        String minioAddress = "http://" + minioContainer.getHost();
        String bucketName = "52n-sta";
        minioClient = MinioClient.builder()
                .endpoint(minioAddress, minioContainer.getMappedPort(9000), false)
                .credentials("minioadmin", "minioadmin")
                .build();

        // Create a bucket for testing
        try {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // upload parquet files
        String directoryPath = "src/test/resources/52n-sta-parquet-files";

        try (Stream<Path> filePaths = Files.walk(Paths.get(directoryPath))) {
            filePaths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".parquet"))
                    .forEach(file -> uploadFileToMinio(file, bucketName));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void uploadFileToMinio(Path filePath, String bucket) {
        try {
            MinioContainerManager.minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath.getFileName().toString())
                            .filename(filePath.toString())
                            .build()
            );
            System.out.println("Uploaded: " + filePath.getFileName().toString());
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    @AfterAll
    public static void tearDown() {
        if (minioContainer != null) {
            minioContainer.stop();
        }
    }
}
