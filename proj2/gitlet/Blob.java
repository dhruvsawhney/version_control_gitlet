package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

class Blob implements Serializable {

    private String fileName_;

    private byte[] contentAsBytes_;

    private String contentHash_;

    Blob(String fileName){
        fileName_ = fileName;
        contentAsBytes_ = setContentAsBytes_();
        contentHash_ = setContentHash_();
    }

    String getFileName_() {
        return fileName_;
    }

    String getContentHash_() {
        return contentHash_;
    }

    private byte[] setContentAsBytes_() {

        return Utils.readContents(new File(fileName_));
    }

    byte[] getContentAsBytes_() {
        return contentAsBytes_;
    }

    // create hash based on the content of the file (as byte arr)
    private String setContentHash_() {
        File tempFile = new File(fileName_);

        byte[] fileToAddBytes = new byte[0];
        try {
            fileToAddBytes = Files.readAllBytes(tempFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileHash = Utils.sha1(fileToAddBytes);
        return fileHash;
    }

    // write commit object to disk
    void writeBlobToDisk(){

        File outFile = Utils.join(Utils.GITLET_DIR, Utils.BLOBS_DIR, contentHash_);

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

    static Blob readBlobFromDisk(String blobID){

        Blob blobObj = null;
        File inFile =  Utils.join(Utils.GITLET_DIR, Utils.BLOBS_DIR, blobID);
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            blobObj = (Blob) inp.readObject();
            inp.close();

        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return blobObj;
    }

    static boolean isFileSame(String blobID1, String blobID2){
        Blob blob1 = Blob.readBlobFromDisk(blobID1);
        Blob blob2 = Blob.readBlobFromDisk(blobID2);

        return Arrays.equals(blob1.getContentAsBytes_(), blob2.getContentAsBytes_());
    }
}

