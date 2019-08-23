package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    String branchName_;

    // hash of the latest commit on this branch
    String headCommit_;

    public Branch(String branchName, String headCommit){
        branchName_= branchName;
        headCommit_ = headCommit;
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
}
