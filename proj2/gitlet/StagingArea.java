package gitlet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class StagingArea implements Serializable {

    // the files to-be-tracked from the next commit
    private Set<String> filesToAdd_;
    // the files to-be-removed from the next commit
    private Set<String> filesToRemove_;
    // mapping from file-name to BlobID (files currently being tracked)
    private Map<String, String> prevCommitFileToBlobIDMap;

    // use the mapping from the previous commit for tracking files
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

    Map<String, String> getPrevCommitFileToBlobIDMap() {
        return prevCommitFileToBlobIDMap;
    }

    void setPrevCommitFileToBlobIDMap(Map<String, String> prevCommitFileToBlobIDMap) {
        this.prevCommitFileToBlobIDMap = prevCommitFileToBlobIDMap;
    }
}
