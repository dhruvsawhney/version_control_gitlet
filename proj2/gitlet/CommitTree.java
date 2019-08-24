package gitlet;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class CommitTree implements Serializable {

    private Branch activeBranch_;
    private Map<String, Branch> branchNameToBranch_;
    private StagingArea stagingArea_;

    public CommitTree(){
        branchNameToBranch_ = new HashMap<>();
        stagingArea_ = new StagingArea(new HashMap<>());
    }

    private void setActiveBranch_(Branch activeBranch) {
        activeBranch_ = activeBranch;
    }

    private void addBranchMapping(String branchName, Branch newBranch){
        branchNameToBranch_.put(branchName, newBranch);
    }

    private void deleteBranchMapping(String branchName){
        branchNameToBranch_.remove(branchName);
    }

    private void setStagingAreaMapping(Map<String, String> prevFileToBlobMap){
        stagingArea_.setPrevCommitFileToBlobIDMap(prevFileToBlobMap);
    }

    // return the head commit on the active branch
    private Commit getHeadCommit(){
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

        // remove from staging area: file un-tracked from next commit
        stagingArea_.getFileToRemove_().remove(fileName);


        try {
            File fileToAdd = new File(fileName);
            byte[] fileToAddBytes = Files.readAllBytes(fileToAdd.toPath());
            String fileHash = Utils.sha1(fileToAddBytes);


            Commit currCommit = getHeadCommit();
            Blob currBlob = currCommit.blobExist(fileHash);

            // do not add if identical data and filename
            if (currBlob != null && currBlob.getFileName_().equals(fileToAdd.getName())){
                return;
            }

            stagingArea_.getFileToAdd_().add(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void commit(String commitMessage){
        if (commitMessage.length() == 0){
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

    public void removeFile(String fileName){

        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        // neither staged nor tracked by current commit
        if (!stagingArea_.getFileToAdd_().contains(fileName) && !headCommit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("No reason to remove the file.");
            return;
        }

        if (headCommit.getFileToBlobIDMap_().containsKey(fileName)){

            Utils.restrictedDelete(fileName);
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

            byte[] blobContents = blob.getContentAsBytes_();
            fileStream.write(blobContents);
            fileStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkoutFile(String fileName){

        // checkout file from the head commit
        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        if (!headCommit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
            return;
        }

        overwriteWorkingDir(headCommit, fileName);
    }

    public void checkoutFilePrevCommmit(String commitID, String fileName){

        // checkout file on the active branch
        // use of BranchPtr to access all commits along branch
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

        // From the checkout Branch, use its head-commit
        // as it is indicative of active node on branch
        Branch checkoutBranch = branchNameToBranch_.get(branchName);
        Commit checkoutCommit = Commit.readCommitFromDisk(checkoutBranch.getHeadCommit_());

        Commit currCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());


        // Un-tracked file:  neither staged for addition nor tracked
        // or staged for removal, but then re-added without gitlet’s knowledge
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
                Utils.restrictedDelete(fileName);
            }
        }

        // clear staging area
        stagingArea_ = new StagingArea(checkoutCommit.getFileToBlobIDMap_());

        // reset pointer for branch
        setActiveBranch_(checkoutBranch);
    }


    public void branch(String branchName){

        if (branchNameToBranch_.containsKey(branchName)){
            System.out.println("A branch with that name already exists.");
            return;
        }

        Branch branch = new Branch(branchName, activeBranch_.getHeadCommit_());
        addBranchMapping(branchName, branch);
    }

    public void removeBranch(String branchName){

        if (!branchNameToBranch_.containsKey(branchName)){
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (activeBranch_.getBranchName_().equals(branchName)){
            System.out.println("Cannot remove the current branch.");
            return;
        }

        deleteBranchMapping(branchName);
    }
}
