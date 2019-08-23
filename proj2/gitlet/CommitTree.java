package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CommitTree implements Serializable {

    Branch activeBranch_;
    Map<String, Branch> branchNameToBranch_;
    StagingArea stagingArea_;

    public CommitTree(){
        branchNameToBranch_ = new HashMap<>();
        stagingArea_ = new StagingArea(new HashMap<>());
    }

    public void setActiveBranch_(Branch activeBranch) {
        activeBranch_ = activeBranch;
    }

    public void addBranchMapping(String branchName, Branch newBranch){
        branchNameToBranch_.put(branchName, newBranch);
    }

    public void setStagingAreaMapping(Map<String, String> prevFileToBlobMap){
        stagingArea_.setPrevCommitFileToBlobIDMap(prevFileToBlobMap);
    }

    // return the head commit on the active branch
    public Commit getHeadCommit(){
        return Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());
    }

    // called at initialization only
    public static CommitTree initCommitTree(){

        Commit initCommit = new Commit("initial commit", null);
        initCommit.writeCommitToDisk();

        Branch initBranch = new Branch("master", initCommit.getThisCommitID_());

        CommitTree ct = new CommitTree();

        ct.setActiveBranch_(initBranch);
        ct.addBranchMapping(initBranch.getBranchName_(), initBranch);
        ct.setStagingAreaMapping(initCommit.getFileToBlobIDMap_());

        return ct;
    }

    public void add(String fileName){

        String dirName = System.getProperty("user.dir");
        List<String> filesInDir = Utils.plainFilenamesIn(dirName);

        if (!filesInDir.contains(fileName)){
            System.out.println("File does not exist.");
            return;
        }

        File fileToAdd = new File(fileName);
        try {
            byte[] fileToAddBytes = Files.readAllBytes(fileToAdd.toPath());
            String fileHash = Utils.sha1(fileToAddBytes);

            // do not add file if identical to current commit
            Commit currCommit = getHeadCommit();

            // TODO :: not efficient
            if (currCommit.blobExist(fileHash)){
                return;
            }

            // TODO: CANNOT REMOVE HERE :: separate command
            stagingArea_.getFileToAdd_().add(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commit(String commitMessage){
        if (commitMessage == null){
            System.out.println("Please enter a commit message.");
            return;
        }

        if (stagingArea_.getFileToAdd_().isEmpty() && stagingArea_.getFileToRemove_().isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }

        for (String fileName : stagingArea_.getFileToRemove_()){
            stagingArea_.getPrevCommitFileToBlobIDMap().remove(fileName);
        }

        for (String fileName : stagingArea_.getFileToAdd_()){

            Blob blob = new Blob(fileName);
            blob.writeBlobToDisk();

            stagingArea_.getPrevCommitFileToBlobIDMap().put(blob.getFileName_(), blob.getContentHash_());
        }

        // write out the commit to disk
        Commit commit = new Commit(commitMessage, getHeadCommit().getThisCommitID_(), stagingArea_.getPrevCommitFileToBlobIDMap());
        commit.writeCommitToDisk();

        // clear staging area
        Map<String, String> prevCommitFileToBlob = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_()).getFileToBlobIDMap_();
        stagingArea_ = new StagingArea(prevCommitFileToBlob);

        // change pointers for commit on active branch
        activeBranch_.setHeadCommit_(commit.getThisCommitID_());
    }


    public void rm(String fileName){

        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        if (!stagingArea_.getFileToAdd_().contains(fileName) && !headCommit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("No reason to remove the file.");
            return;
        }

        if (headCommit.getFileToBlobIDMap_().containsKey(fileName)){

            // TODO :: the path to the file?
            File file = new File(fileName);
            file.delete();

            //mark un-track
            stagingArea_.getFileToRemove_().add(fileName);
        }

        // in the case not on head commit but said to track
        stagingArea_.getFileToAdd_().remove(fileName);
    }


    public void log(){
        String commitHash = activeBranch_.getHeadCommit_();

        while (commitHash != null){

            Commit currCommit = Commit.readCommitFromDisk(commitHash);
            System.out.println("===");
            System.out.println("Commit " + currCommit.getThisCommitID_());
            System.out.println(currCommit.getTimestamp_());
            System.out.println(currCommit.getCommitMessage_());
            System.out.println();

            commitHash = currCommit.getParentCommitID_();
        }
    }

    // TODO :: error handling
    public void checkoutSingleFile(String fileName){

        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());
        Blob blob = headCommit.getBlob(fileName);

        try {
            File file = new File(fileName);
            // false to overwrite
            FileOutputStream fileStream = new FileOutputStream(file, false);

            byte[] blobContents = blob.getContentAsString_().getBytes();
            fileStream.write(blobContents);
            fileStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
