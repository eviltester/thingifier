package uk.co.compendiumdev.challenge.persistence;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import uk.co.compendiumdev.challenge.ChallengerAuthData;

import java.io.*;

public class AwsS3Storage implements PersistenceMechanism{

    static AmazonS3 s3Client;

    // to work we need environment variables for
    // AWSBUCKET
    // AWS_ACCESS_KEY_ID
    // AWS_SECRET_ACCESS_KEY

    // https://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html
    // https://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html

    @Override
    public PersistenceResponse saveChallengerStatus(final ChallengerAuthData data) {

        if(data==null){
            return new PersistenceResponse().withSuccess(false).withErrorMessage("no data provided");
        }

        String bucketName = System.getenv("AWSBUCKET");

        try{
            ensureClientExists();


            final String dataString = new Gson().toJson(data);
            // Upload a text string as a new object.
            s3Client.putObject(bucketName, data.getXChallenger(), dataString);
            return new PersistenceResponse().withSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error storing data to bucket for guid: " + data.getXChallenger());
            return new PersistenceResponse().withSuccess(false).withErrorMessage(e.getMessage());
        }
    }

    private void ensureClientExists() {
        if(s3Client!=null){
            return;
        }

        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .build();

    }

    @Override
    public PersistenceResponse loadChallengerStatus(final String guid) {

        String bucketName = System.getenv("AWSBUCKET");

        try {
            ensureClientExists();

            final S3Object fullObject = s3Client.getObject(new GetObjectRequest(bucketName, guid));
            String dataString = getObjectContent(fullObject.getObjectContent());
            return new PersistenceResponse().withSuccess(true).withChallengerAuthData(
                    new Gson().fromJson(dataString, ChallengerAuthData.class));
        } catch (Exception e) {
            e.getStackTrace();
            System.out.println("Error Reading Challenge Status From S3: " + guid);
            return new PersistenceResponse().
                    withSuccess(false).
                    withErrorMessage(e.getMessage());
        }
    }

    private static String getObjectContent(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        String content = "";
        while ((line = reader.readLine()) != null) {
            content=content + line + "\n";
        }
        return content;
    }
}
