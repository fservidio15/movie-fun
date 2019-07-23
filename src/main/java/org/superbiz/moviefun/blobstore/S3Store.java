package org.superbiz.moviefun.blobstore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.tika.Tika;

import java.util.Optional;

public class S3Store implements BlobStore {
    AmazonS3Client s3Client;
    String photoStorageBucket;

    public static final String DEFAULT_COVER_JPG = "default-cover.jpg";

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;

    }


    @Override
    public void put(Blob blob) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(blob.contentType);
        s3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, metadata);
    }

    @Override
    public Optional<Blob> get(String name) {
        S3Object object = null;
        try {
            object = s3Client.getObject(photoStorageBucket, name);
        }catch (AmazonServiceException e){
            //key does not exist
        }
        Blob blob;
        if (object != null) {
            blob = new Blob(name, object.getObjectContent(), new Tika().detect(object.getObjectMetadata().getContentType()));
            return Optional.of(blob);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }


}
