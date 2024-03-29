package gitlet;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

class CommitTree implements Serializable {

    // the current branch
    private Branch activeBranch_;
    // mapping of all branch names to branch objects
    private Map<String, Branch> branchNameToBranch_;
    // file status for potential commits
    private StagingArea stagingArea_;

    private CommitTree(){
        branchNameToBranch_ = new HashMap<>();
        Commit temp = new Commit();
        stagingArea_ = new StagingArea(temp);
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

    // head commit on the active branch
    private Commit getHeadCommit(){
        return Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());
    }

    private boolean checkPrefixMatch(String prefix, String commitID){

        // prefix must be less than 40 characters (user input)
        // since the hash is consistently 40 characters
        if (prefix.length() < 40){
            String commitPrefix = commitID.substring(0, prefix.length());
            return commitPrefix.equals(prefix);
        }
        return false;
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

    // overwrite file with contents of Blob from a previous commit
    private void overwriteWorkingDir(Commit commit, String fileName){
        Blob blob = commit.getBlob(fileName);

        try {
            File file = new File(fileName);
            // false to overwrite file
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

    // return the Commit object with the given commitID, otherwise return null
    private Commit findCommitNode(String commitID){

        // search all branches for commitID
        boolean foundCommit = false;
        String targetCommitID = null;

        for (Branch branch : branchNameToBranch_.values()){
            targetCommitID = branch.getBranchPtr_();

            while(targetCommitID != null){

                Commit commit = Commit.readCommitFromDisk(targetCommitID);

                if (commit.getThisCommitID_().equals(commitID) || checkPrefixMatch(commitID, commit.getThisCommitID_())){
                    foundCommit = true;
                    break;
                }

                targetCommitID = commit.getParentCommitID_();
            }

            if (foundCommit){
                break;
            }
        }

        return targetCommitID != null ? Commit.readCommitFromDisk(targetCommitID) : null;
    }

    private boolean mergeFailureCases(String branchName){

        if (!stagingArea_.getFileToAdd_().isEmpty() || !stagingArea_.getFileToRemove_().isEmpty()){
            System.out.println("You have uncommitted changes.");
            return true;
        }

        if (!branchNameToBranch_.containsKey(branchName)){
            System.out.println("A branch with that name does not exist.");
            return true;
        }

        if (activeBranch_.getBranchName_().equals(branchName)){
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }

        // check for un-tracked files
        Branch mergeBranch = branchNameToBranch_.get(branchName);
        Commit mergeCommit = Commit.readCommitFromDisk(mergeBranch.getHeadCommit_());

        List<String> workingDirFiles = Utils.plainFilenamesIn(Utils.WORKING_DIR);
        for (String file : workingDirFiles){

            if (!stagingArea_.getPrevCommitFileToBlobIDMap().containsKey(file) && mergeCommit.getFileToBlobIDMap_().containsKey(file)){
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return true;
            }
        }

        return false;
    }


    // find the earliest common ancestor in the CommitTree with givenBranch
    // and the active branch
    private Commit getSplitNode(String givenBranch){

        Set<String> commitIDSet = new HashSet<>();

        // for complete search, start search from the branch pointer
        // in case the head and branch pointer diff
        String branchPtr = activeBranch_.getBranchPtr_();

        while (branchPtr != null){
            Commit commit = Commit.readCommitFromDisk(branchPtr);
            commitIDSet.add(commit.getThisCommitID_());

            branchPtr = commit.getParentCommitID_();
        }

        String givenBranchPtr = branchNameToBranch_.get(givenBranch).getBranchPtr_();

        while (givenBranchPtr != null){
            if (commitIDSet.contains(givenBranchPtr)){
                break;
            }

            Commit commit = Commit.readCommitFromDisk(givenBranchPtr);
            givenBranchPtr = commit.getParentCommitID_();
        }

        return Commit.readCommitFromDisk(givenBranchPtr);
    }

    private String getBlobID(String fileName, Commit node){
        return node.getFileToBlobIDMap_().get(fileName);
    }

    private boolean isFilePresent(String fileName, Commit node){
        return node.getFileToBlobIDMap_().containsKey(fileName);
    }


    // write the contents of the current and merge branch with merge conflict separators
    private void mergeConflictLogger(Blob currBlob, Blob mergeBlob, String file){

        byte[] currBranchContent = currBlob != null ? currBlob.getContentAsBytes_() : new byte[0];
        byte[] mergeBranchContent = mergeBlob != null ? mergeBlob.getContentAsBytes_() : new byte[0];

        byte[] firstSeparator = "<<<<<<< HEAD\n".getBytes();
        byte[] secondSeparator = "=======\n".getBytes();
        byte[] thirdSeparator = ">>>>>>>\n".getBytes();

        byte[] allByteArray = new byte[firstSeparator.length + currBranchContent.length + secondSeparator.length + mergeBranchContent.length + thirdSeparator.length];

        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(firstSeparator);
        buff.put(currBranchContent);
        buff.put(secondSeparator);
        buff.put(mergeBranchContent);
        buff.put(thirdSeparator);

        byte[] combined = buff.array();

        // write merge conflict output to conflict file
        File fileObj = new File(file);
        Utils.writeContents(fileObj, combined);
    }

    // called at initialization only
    static CommitTree initCommitTree(){

        Commit initCommit = new Commit("initial commit", null);
        initCommit.writeCommitToDisk();

        Branch initBranch = new Branch("master", initCommit.getThisCommitID_());

        CommitTree ct = new CommitTree();

        ct.setActiveBranch_(initBranch);
        ct.addBranchMapping(initBranch.getBranchName_(), initBranch);
        ct.setStagingAreaMapping(initCommit.getFileToBlobIDMap_());

        return ct;
    }

    void add(String fileName){

        List<String> filesInDir = Utils.plainFilenamesIn(Utils.WORKING_DIR);

        if (!filesInDir.contains(fileName)){
            System.out.println("File does not exist.");
            return;
        }

        // remove file from un-tracked status when added again
        stagingArea_.getFileToRemove_().remove(fileName);

        try {
            File fileToAdd = new File(fileName);
            byte[] fileToAddBytes = Files.readAllBytes(fileToAdd.toPath());
            String fileHash = Utils.sha1(fileToAddBytes);

            Commit currCommit = getHeadCommit();

            // and we use the blobID for the argument file
            // to check if file content is same
            Blob currBlob = currCommit.blobExist(fileHash);

            // do not add file if identical data or filename
            // if identical data, currBlob will be null
            if (currBlob != null && currBlob.getFileName_().equals(fileToAdd.getName())){
                return;
            }

            // add file to staging area if control reaches here
            stagingArea_.getFileToAdd_().add(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void commit(String commitMessage){
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

        // write commit to disk
        Commit commit = new Commit(commitMessage, getHeadCommit().getThisCommitID_(), stagingArea_.getPrevCommitFileToBlobIDMap());
        commit.writeCommitToDisk();

        // clear the staging area
        stagingArea_ = new StagingArea(commit);

        // change pointers for commit on active branch
        if (activeBranch_.getBranchPtr_().equals(activeBranch_.getHeadCommit_())){
            activeBranch_.setHeadCommit_(commit.getThisCommitID_());
            activeBranch_.setBranchPtr_(commit.getThisCommitID_());
        } else {
            // NOTE :: control should not reach here as detached head not implemented
            // in that case head and branch pointer will differ and we add commit on previous commit node
            activeBranch_.setHeadCommit_(commit.getThisCommitID_());
        }
    }

     void removeFile(String fileName){

        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        if (!stagingArea_.getFileToAdd_().contains(fileName) && !headCommit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("No reason to remove the file.");
            return;
        }

        if (headCommit.getFileToBlobIDMap_().containsKey(fileName)){

            Utils.restrictedDelete(fileName);
            stagingArea_.getFileToRemove_().add(fileName);
        }

        stagingArea_.getFileToAdd_().remove(fileName);
    }

    void log(){
        logPrinter(activeBranch_.getHeadCommit_());
    }

    void globalLog(){

        for (Branch branch : branchNameToBranch_.values()){

            System.out.println();
            System.out.println("Branch: " + branch.getBranchName_());

            logPrinter(branch.getBranchPtr_());
        }
    }

    void find(String commitMessage){

        boolean foundCommit = false;

        for (Branch branch : branchNameToBranch_.values()){

            // start from the branch pointer for complete search
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

    void status(){

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


    void checkoutFile(String fileName){

        // checkout file from the head commit of the active branch
        Commit headCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());

        if (!headCommit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
            return;
        }

        overwriteWorkingDir(headCommit, fileName);
    }

    void checkoutFilePrevCommmit(String commitID, String fileName){

        Commit commit = findCommitNode(commitID);

        if (commit == null){
            System.out.println("No commit with that id exists.");
            return;
        }

        if (!commit.getFileToBlobIDMap_().containsKey(fileName)){
            System.out.println("File does not exist in that commit.");
            return;
        }

        // overwrite file in working dir
        overwriteWorkingDir(commit, fileName);
    }

    void checkoutBranch(String branchName){

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

        // handle un-tracked files
        List<String> workingDirFiles = Utils.plainFilenamesIn(Utils.WORKING_DIR);
        for (String file : workingDirFiles){

            // the un-tracked file would be overwritten by checkout
            if (!stagingArea_.getPrevCommitFileToBlobIDMap().containsKey(file) && checkoutCommit.getFileToBlobIDMap_().containsKey(file)){
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return;
            }
        }

        for (String fileName : checkoutCommit.getFileToBlobIDMap_().keySet()){
            overwriteWorkingDir(checkoutCommit, fileName);
        }

        Commit currCommit = Commit.readCommitFromDisk(activeBranch_.getHeadCommit_());
        // delete files tracked by current branch but not present in checkoutBranch
        for (String fileName : currCommit.getFileToBlobIDMap_().keySet()){

            if (!checkoutCommit.getFileToBlobIDMap_().containsKey(fileName)){
                Utils.restrictedDelete(fileName);
            }
        }

        // clear staging area
        stagingArea_ = new StagingArea(checkoutCommit);

        // reset pointer for branch
        setActiveBranch_(checkoutBranch);
    }


    void branch(String branchName){

        if (branchNameToBranch_.containsKey(branchName)){
            System.out.println("A branch with that name already exists.");
            return;
        }

        Branch branch = new Branch(branchName, activeBranch_.getHeadCommit_());
        addBranchMapping(branchName, branch);
    }

    void removeBranch(String branchName){

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


    void reset(String commitID){

        Commit resetCommit = findCommitNode(commitID);

        if (resetCommit == null){
            System.out.println("No commit with that id exists.");
            return;
        }

        // handle un-tracked files
        List<String> workingDirFiles = Utils.plainFilenamesIn(Utils.WORKING_DIR);
        for (String file : workingDirFiles){

            // the un-tracked file would be overwritten by reset commit
            if (!stagingArea_.getPrevCommitFileToBlobIDMap().containsKey(file) && resetCommit.getFileToBlobIDMap_().containsKey(file)){
                System.out.println("There is an untracked file in the way; delete it or add it first.");
                return;
            }
        }

        // delete files tracked by current commit, but not by reset commit
        for (String file : getHeadCommit().getFileToBlobIDMap_().keySet()){

            if (!resetCommit.getFileToBlobIDMap_().containsKey(file)){
                Utils.restrictedDelete(file);
            }
        }

        // clear the staging area. Set the tracking files to resetCommit's tracking files
        stagingArea_ = new StagingArea(resetCommit);

        // reset the head commit
        activeBranch_.setHeadCommit_(resetCommit.getThisCommitID_());
    }

    void merge(String branchName){

        // deal with failure cases first
        boolean isFailureCase = mergeFailureCases(branchName);

        if (isFailureCase){
            return;
        }

        Commit splitNode = getSplitNode(branchName);
        Commit currNode = Commit.readCommitFromDisk(activeBranch_.getBranchPtr_());
        String mergeBranchPtr = branchNameToBranch_.get(branchName).getBranchPtr_();
        Commit mergeNode = Commit.readCommitFromDisk(mergeBranchPtr);

        if (splitNode.getThisCommitID_().equals(mergeNode.getThisCommitID_())){
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (splitNode.getThisCommitID_().equals(currNode.getThisCommitID_())){

            activeBranch_.setBranchPtr_(mergeBranchPtr);
            activeBranch_.setHeadCommit_(mergeBranchPtr);

            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // since split point, modified in given branch but not modified in current branch
        for (String file : currNode.getFileToBlobIDMap_().keySet()){

            if (!isFilePresent(file, splitNode) || !isFilePresent(file, mergeNode)){
                continue;
            }

            String blobIDMerge = getBlobID(file, mergeNode);
            String blobIDSplit = getBlobID(file, splitNode);
            String blobIDCurr = getBlobID(file, currNode);

            if (!Blob.isFileSame(blobIDMerge, blobIDSplit) && Blob.isFileSame(blobIDCurr, blobIDSplit)){

                // change object pointers and stage
                this.checkoutFilePrevCommmit(mergeNode.getThisCommitID_(),file);
                stagingArea_.getFileToAdd_().add(file);
            }
        }


        // not present at split point, only present in given branch
        // checkout (from given) and stage
        for (String file : mergeNode.getFileToBlobIDMap_().keySet()){

            if (!isFilePresent(file, splitNode) && !isFilePresent(file, currNode) && isFilePresent(file, mergeNode)){

                this.checkoutFilePrevCommmit(mergeNode.getThisCommitID_(), file);
                stagingArea_.getFileToAdd_().add(file);
            }
        }

        // present in split, unmodified in current, absent in given branch
        // removed (and un-tracked)
        for (String file : splitNode.getFileToBlobIDMap_().keySet()){

            String blobIDSplit = getBlobID(file, splitNode);
            String blobIDCurr = getBlobID(file, currNode);

            if (blobIDCurr != null && Blob.isFileSame(blobIDCurr, blobIDSplit) && !isFilePresent(file, mergeNode)){

                // remove and un-track
                Utils.restrictedDelete(file);
                stagingArea_.getFileToRemove_().add(file);
            }
        }

        boolean mergeConflict = false;

        for (String file : currNode.getFileToBlobIDMap_().keySet()){

            String blobIDCurr = getBlobID(file, currNode);
            String blobIDMerge = getBlobID(file, mergeNode);
            String blobIDSplit = getBlobID(file, splitNode);

            // conflict if file not same in current and given branches
            if (mergeNode.getFileToBlobIDMap_().containsKey(file) && !Blob.isFileSame(blobIDCurr, blobIDMerge)){

                mergeConflict = true;

                Blob currBlob = Blob.readBlobFromDisk(blobIDCurr);
                Blob mergeBlob = Blob.readBlobFromDisk(blobIDMerge);

                mergeConflictLogger(currBlob, mergeBlob, file);

                // a conflict caused by a file changed in one and removed in the other
                // split node helps identify whether a file was removed from either branch
            } else if (splitNode.getFileToBlobIDMap_().containsKey(file) && !mergeNode.getFileToBlobIDMap_().containsKey(file) && currNode.getFileToBlobIDMap_().containsKey(file) && !Blob.isFileSame(blobIDSplit, blobIDCurr)){

                mergeConflict = true;

                Blob currBlob = currNode.getFileToBlobIDMap_().containsKey(file) ? Blob.readBlobFromDisk(blobIDCurr) : null;
                Blob mergeBlob = mergeNode.getFileToBlobIDMap_().containsKey(file) ? Blob.readBlobFromDisk(blobIDMerge) : null;

                mergeConflictLogger(currBlob, mergeBlob, file);
            }
        }

        if (mergeConflict){
            System.out.println("Encountered a merge conflict.");
            return;
        }

        String message = String.format("Merged %s with %s.", activeBranch_.getBranchName_(), branchName);
        this.commit(message);
    }
}
