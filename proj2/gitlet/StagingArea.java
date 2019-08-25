package gitlet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StagingArea implements Serializable {

    private Set<String> filesToAdd_;
    private Set<String> filesToRemove_;
    // file-name to blobID
    private Map<String, String> prevCommitFileToBlobIDMap;

    // use the mapping from the previous commit
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
