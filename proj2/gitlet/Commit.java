package gitlet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Commit implements Serializable {

    private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String commitMessage_;
    private String parentCommitID_;
    private String thisCommitID_;
    private String timestamp_;

    // file-name to blobID (hash)
    private Map<String, String> fileToBlobIDMap_;


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

    public String getThisCommitID_() {
        return thisCommitID_;
    }

    public Map<String, String> getFileToBlobIDMap_() {
        return fileToBlobIDMap_;
    }


    public String getParentCommitID_() {
        return parentCommitID_;
    }

    public String getTimestamp_() {
        return timestamp_;
    }

    public String getCommitMessage_() {
        return commitMessage_;
    }

    // write commit object to disk
    public void writeCommitToDisk(){

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

    public static Commit readCommitFromDisk(String commitID){

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
    public Blob blobExist(String blobHash){

        for(String blobID : fileToBlobIDMap_.values()){

            if (blobID.equals(blobHash)){
                return Blob.readBlobFromDisk(blobID);
            }
        }

        return null;
    }

    public Blob getBlob(String fileName){

        return Blob.readBlobFromDisk(fileToBlobIDMap_.get(fileName));
    }
}
