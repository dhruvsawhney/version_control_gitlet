package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    String branchName_;

    // hash of the latest commit on this branch
    String headCommit_;

    // the pointer to start of branch (from latest commit)
    String branchPtr_;

    // both head and brac
    public Branch(String branchName, String headCommit){
        branchName_= branchName;
        headCommit_ = headCommit;
        branchPtr_ = headCommit;
    }

    public String getBranchName_() {
        return branchName_;
    }

    public String getHeadCommit_() {
        return headCommit_;
    }

    public void setHeadCommit_(String headCommit_) {
        this.headCommit_ = headCommit_;
    }

    public String getBranchPtr_() {
        return branchPtr_;
    }

    public void setBranchPtr_(String branchPtr_) {
        this.branchPtr_ = branchPtr_;
    }
}
