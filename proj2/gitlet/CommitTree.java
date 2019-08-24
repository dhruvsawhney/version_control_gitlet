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
        if (activeBranch_.getBranchPtr_().equals(activeBranch_.getHeadCommit_())){
            activeBranch_.setHeadCommit_(commit.getThisCommitID_());
            activeBranch_.setBranchPtr_(commit.getThisCommitID_());
        } else {
            // TODO :: in detached head state and a commit is made
            activeBranch_.setHeadCommit_(commit.getThisCommitID_());
        }

    }


    // TODO :: not sure about branching logic
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


    private void logPrinter(String commitHash){
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

    public void log(){
        logPrinter(activeBranch_.getHeadCommit_());
    }

    public void globalLog(){

        for (Branch branch : branchNameToBranch_.values()){

            System.out.println();
            System.out.println("Branch: " + branch.getBranchName_());

            logPrinter(branch.getBranchPtr_());
        }
    }

    public void find(String commitMessage){

        boolean foundCommit = false;

        for (Branch branch : branchNameToBranch_.values()){

            // start from the branch pointer for case that head and branchPtr differ
            String commitID = branch.getBranchPtr_();

            while (commitID != null){

                Commit commit = Commit.readCommitFromDisk(commitID);;

                if (commit.getCommitMessage_().equals(commitMessage)){
                    foundCommit = true;
                    System.out.println(commit.getThisCommitID_());
                }

                commitID = commit.getParentCommitID_();
            }
        }

        if (!foundCommit){
            System.out.println("Found no commit with that message.");
        }
    }

    public void status(){

        List<String> branches = new ArrayList<>(branchNameToBranch_.keySet());
        List<String> stagedFiles = new ArrayList<>(stagingArea_.getFileToAdd_());
        // TODO :: use of the remove files in staging area?
        List<String> removedFiles = new ArrayList<>(stagingArea_.getFileToRemove_());

        Collections.sort(branches);
        Collections.sort(stagedFiles);
        Collections.sort(removedFiles);

        System.out.println("=== Branches ===");
        for (String branchName : branches){
            if (branchName.equals(activeBranch_.getBranchName_())){
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }


        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String stagedFile : stagedFiles){
            System.out.println(stagedFile);
        }



        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String removedFile : removedFiles){
            System.out.println(removedFile);
        }

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");

        System.out.println();
        System.out.println("=== Untracked Files ===");
    }


    // pass the commit object and fileName to overwrite in current dir
    // Note: does not check presence of file in working dir
    private void overwriteWorkingDir(Commit commit, String fileName){
        Blob blob = commit.getBlob(fileName);

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

    // TODO :: error handling
    public void checkoutFile(String fileName){

        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        if (!headCommit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
            return;
        }

        overwriteWorkingDir(headCommit, fileName);
    }

    public void checkoutFilePrevCommmit(String commitID, String fileName){

        // TODO :: find commit on this branch only?
        String currPtr = activeBranch_.getBranchPtr_();
        boolean found = false;

        while (currPtr != null){
            Commit commit = Commit.readCommitFromDisk(currPtr);

            if (commit.getThisCommitID_().equals(commitID)){
                found = true;
                break;
            }

            currPtr = commit.getParentCommitID_();
        }

        if (!found){
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit commit = Commit.readCommitFromDisk(currPtr);

        if (!commit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
            return;
        }

        // overwrite file in working dir if control reaches here
        overwriteWorkingDir(commit, fileName);
    }

    public void checkoutBranch(String branchName){

        if (!branchNameToBranch_.containsKey(branchName)){
            System.out.println("No such branch exists.");
            return;
        }

        if (branchName.equals(activeBranch_.getBranchName_())){
            System.out.println("No need to checkout the current branch.");
            return;
        }

        // TODO :: checkout from head or branchPtr?
        Branch checkoutBranch = branchNameToBranch_.get(branchName);
        Commit checkoutCommit = Commit.readCommitFromDisk(checkoutBranch.getHeadCommit_());

        Commit currCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        // TODO :: untracked files (2 conditions stated in reading)
        List<String> untrackedFiles = new ArrayList<>();
        List<String> workingDirFiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));

        for (String file : workingDirFiles){
            if (!stagingArea_.getFileToAdd_().contains(file) && !currCommit.getFileToBlobIDMap_().containsKey(file)
            || stagingArea_.getFileToRemove_().contains(file)){
                untrackedFiles.add(file);
            }
        }

        for (String untrackedFile : untrackedFiles){
            if (checkoutCommit.getFileToBlobIDMap_().containsKey(untrackedFile)){
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return;
            }
        }

        for (String fileName : checkoutCommit.getFileToBlobIDMap_().keySet()){
            overwriteWorkingDir(checkoutCommit, fileName);
        }

        // delete files tracked by current branch but not present in checkoutBranch
        for (String fileName : currCommit.getFileToBlobIDMap_().keySet()){

            if (!checkoutCommit.getFileToBlobIDMap_().containsKey(fileName)){
                File file = new File(fileName);
                file.delete();
            }
        }

        // clear staging area
        stagingArea_ = new StagingArea(checkoutCommit.getFileToBlobIDMap_());

        // reset pointer for branch
        setActiveBranch_(checkoutBranch);
    }
}
