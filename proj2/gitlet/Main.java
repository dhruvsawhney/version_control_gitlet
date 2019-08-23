package gitlet;

import java.awt.color.CMMException;
import java.io.*;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {

    private static final String GITLET_DIR = ".gitlet/";
    private static final String COMMIT_DIR = "Commits";
    private static final String BLOBS_DIR = "Blobs";
    private static final String COMMIT_TREE = "commitTree";

    private CommitTree tree_;

    public CommitTree getTree_() {
        return tree_;
    }

    public void setTree_(CommitTree tree_) {
        this.tree_ = tree_;
    }

    // create the .gitlet directory
    // add the two subdirectories: Commit, Blobs
    public boolean createGitletDirectory(){
        File dir = new File(GITLET_DIR);

        if(!dir.exists()){
            dir.mkdir();

        } else {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            return false;
        }

        File commitDir = Utils.join(GITLET_DIR, COMMIT_DIR);

        if(!commitDir.exists()){
            commitDir.mkdir();
        }

        File blobDir = Utils.join(GITLET_DIR, BLOBS_DIR);

        if (!blobDir.exists()){
            blobDir.mkdir();
        }
        return true;
    }

    public void SaveTree(){

        File outFile = Utils.join(".gitlet", COMMIT_TREE);

        try {

            if(!outFile.exists()){
                outFile.createNewFile();
            }

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(tree_);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CommitTree loadTree(){

        File inFile = Utils.join(".gitlet", COMMIT_TREE);

        if (!inFile.exists()){
            return null;
        }

        CommitTree tree = null;
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            tree = (CommitTree) inp.readObject();
            inp.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return tree;
    }



    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        // TODO: YOUR CODE HERE

        Main program = new Main();

        String command = args[0];

        program.setTree_(program.loadTree());

        switch (command){
            case "init":
                boolean created = program.createGitletDirectory();
                if (created){
                    program.setTree_(CommitTree.initCommitTree());
                }
                break;

            case "add":
                program.getTree_().add(args[1]);
                break;
            case "commit":
                program.getTree_().commit(args[1]);
                break;
            case "log":
                program.getTree_().log();
                break;
            case "checkout":
                // 3 args
                program.getTree_().checkoutSingleFile(args[2]);
                break;

        }

        program.SaveTree();
    }
}
