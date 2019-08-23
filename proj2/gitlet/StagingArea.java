package gitlet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StagingArea implements Serializable {

    // newStaged, newRemoved, stagedFiles (from previous commit)

    Set<String> filesToAdd_;
    Set<String> filesToRemove_;

    // fileName to blobID
    Map<String, String> prevCommitFileToBlobIDMap;

    // TODO :: change this mapping input
    public StagingArea(Map<String, String> prevFileToBlobMap){
        filesToAdd_ = new HashSet<>();
        filesToRemove_ = new HashSet<>();
        prevCommitFileToBlobIDMap = prevFileToBlobMap;
    }

    public Set<String> getFileToAdd_() {
        return filesToAdd_;
    }

    public Set<String> getFileToRemove_() {
        return filesToRemove_;
    }

    public void setPrevCommitFileToBlobIDMap(Map<String, String> prevCommitFileToBlobIDMap) {
        this.prevCommitFileToBlobIDMap = prevCommitFileToBlobIDMap;
    }

    public Map<String, String> getPrevCommitFileToBlobIDMap() {
        return prevCommitFileToBlobIDMap;
    }

}
