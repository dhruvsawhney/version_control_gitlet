package gitlet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Commit implements Serializable {

    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    String commitMessage_;
    String parentCommitID_;
    String thisCommitID_;
    String timestamp_;

    // fileName to blobID (hash)
    Map<String, String> fileToBlobIDMap_;


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

        // TODO :: is the deep copy needed?
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
    // write commit object to disk
    public void writeCommitToDisk(){
        String[] tempArr = new String[2];
        tempArr[0] = "Commits";
        tempArr[1] = thisCommitID_;

        File outFile = Utils.join(".gitlet", tempArr);

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
        String[] tempArr = new String[2];
        tempArr[0] = "Commits";
        tempArr[1] = commitID;

        Commit commitObj = null;
        File inFile =  Utils.join(".gitlet", "Commits", commitID);
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

    public boolean blobExist(String blobHash){

        for(String b : fileToBlobIDMap_.values()){

            if (b.equals(blobHash)){
                return true;
            }
        }

        return false;
    }

    public Blob getBlob(String fileName){

        return Blob.readBlobFromDisk(fileToBlobIDMap_.get(fileName));
    }
}