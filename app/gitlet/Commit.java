package gitlet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Commit implements Serializable {

    private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // the user message for this commit
    private String commitMessage_;
    // the parent pointer for this commit
    private String parentCommitID_;
    // this commit's SHA-ID
    private String thisCommitID_;
    // the timestamp for this commit
    private String timestamp_;
    // mapping from file-name to BlobID (files tracked by this commit)
    private Map<String, String> fileToBlobIDMap_;

    // used to initialize staging area
    public Commit(){
        parentCommitID_ = null;
        fileToBlobIDMap_ = new HashMap<>();
    }

    public Commit(String commitMessage, String parentCommitID){
        commitMessage_ = commitMessage;
        parentCommitID_ = parentCommitID;

        Date date = new Date();
        timestamp_ = sdf.format(date);

        fileToBlobIDMap_ = new HashMap<>();
        thisCommitID_ = getHash();
    }

    public Commit(String commitMessage, String parentCommitID, Map<String, String> prevCommitFileToBlobMap){
        commitMessage_ = commitMessage;
        parentCommitID_ = parentCommitID;

        Date date = new Date();
        timestamp_ = sdf.format(date);

        fileToBlobIDMap_ = new HashMap<>(prevCommitFileToBlobMap);
        thisCommitID_ = getHash();
    }


    // get the hash for this commit object
    private String getHash(){
        String temp = commitMessage_ + parentCommitID_ + timestamp_;
        return Utils.sha1(temp);
    }

    String getThisCommitID_() {
        return thisCommitID_;
    }

    Map<String, String> getFileToBlobIDMap_() {
        return fileToBlobIDMap_;
    }


    String getParentCommitID_() {
        return parentCommitID_;
    }

    String getTimestamp_() {
        return timestamp_;
    }

    String getCommitMessage_() {
        return commitMessage_;
    }

    // write commit object to disk
    void writeCommitToDisk(){

        File outFile = Utils.join(Utils.GITLET_DIR, Utils.COMMIT_DIR, thisCommitID_);

        try {
            outFile.createNewFile();

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(this);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // read commit object from disk
    static Commit readCommitFromDisk(String commitID){

        Commit commitObj = null;
        File inFile =  Utils.join(Utils.GITLET_DIR, Utils.COMMIT_DIR, commitID);
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            commitObj = (Commit) inp.readObject();
            inp.close();

        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return commitObj;
    }

    // return the blob if it exists
    Blob blobExist(String blobHash){

        for(String blobID : fileToBlobIDMap_.values()){

            if (blobID.equals(blobHash)){
                return Blob.readBlobFromDisk(blobID);
            }
        }

        return null;
    }

    Blob getBlob(String fileName){

        return Blob.readBlobFromDisk(fileToBlobIDMap_.get(fileName));
    }
}
