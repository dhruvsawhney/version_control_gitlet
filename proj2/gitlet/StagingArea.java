package gitlet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class StagingArea implements Serializable {

    private Set<String> filesToAdd_;
    private Set<String> filesToRemove_;
    // file-name to blobID
    private Map<String, String> prevCommitFileToBlobIDMap;

    // use the mapping from the previous commit for tracking file
    StagingArea(Commit previousCommit){
        filesToAdd_ = new HashSet<>();
        filesToRemove_ = new HashSet<>();
        prevCommitFileToBlobIDMap = previousCommit.getFileToBlobIDMap_();
    }

    Set<String> getFileToAdd_() {
        return filesToAdd_;
    }

    Set<String> getFileToRemove_() {
        return filesToRemove_;
    }

    void setPrevCommitFileToBlobIDMap(Map<String, String> prevCommitFileToBlobIDMap) {
        this.prevCommitFileToBlobIDMap = prevCommitFileToBlobIDMap;
    }

    Map<String, String> getPrevCommitFileToBlobIDMap() {
        return prevCommitFileToBlobIDMap;
    }
}
