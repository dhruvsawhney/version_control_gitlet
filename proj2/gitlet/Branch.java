package gitlet;

import java.io.Serializable;

class Branch implements Serializable {

    // the branch name
    private String branchName_;
    // the SHA-ID for the head pointer
    private String headCommit_;
    // the SHA-ID for the branch pointer
    private String branchPtr_;

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
