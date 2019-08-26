package gitlet;

import java.io.Serializable;

class Branch implements Serializable {

    private String branchName_;

    // hash of the latest commit on this branch
    private String headCommit_;

    // the pointer to start of branch (from latest commit)
    private String branchPtr_;

    // both head and brac
    Branch(String branchName, String headCommit){
        branchName_= branchName;
        headCommit_ = headCommit;
        branchPtr_ = headCommit;
    }

    String getBranchName_() {
        return branchName_;
    }

    String getHeadCommit_() {
        return headCommit_;
    }

    void setHeadCommit_(String headCommit_) {
        this.headCommit_ = headCommit_;
    }

    String getBranchPtr_() {
        return branchPtr_;
    }

    void setBranchPtr_(String branchPtr_) {
        this.branchPtr_ = branchPtr_;
    }
}
