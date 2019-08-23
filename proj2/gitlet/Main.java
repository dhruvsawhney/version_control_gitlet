package gitlet;

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

    private static void checkNumArgs(int numArgs, String[] args){
;
        if (args.length != numArgs){
            System.out.println("Incorrect operands.");
            System.exit(1);
        }
//        return args.length == numArgs;
    }

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        // TODO: Incorrect operands not clean
        if (args.length == 0){
            System.out.println("Please enter a command");
            return;
        }

        Main program = new Main();
        program.setTree_(program.loadTree());

        if (program.getTree_() == null && !args[0].equals("init")){
            System.out.println("Not in an initialized gitlet directory.");
            return;
        }

        String command = args[0];
        switch (command){
            case "init":

                checkNumArgs(1, args);

                boolean created = program.createGitletDirectory();
                if (created){
                    program.setTree_(CommitTree.initCommitTree());
                }
                break;

            case "add":

                checkNumArgs(2, args);
                program.getTree_().add(args[1]);
                break;

            case "commit":

                checkNumArgs(2, args);
                program.getTree_().commit(args[1]);
                break;

            case "log":

                checkNumArgs(1, args);
                program.getTree_().log();
                break;

            case "global-log":

                checkNumArgs(1, args);
                program.getTree_().globalLog();
                break;

            case "find":

                checkNumArgs(2, args);
                program.getTree_().find(args[1]);
                break;

            case "checkout":
                // 3 args
                program.getTree_().checkoutSingleFile(args[2]);
                break;

            default:
                System.out.println("No command with that name exists.");
                break;
        }

        program.SaveTree();
    }
}
