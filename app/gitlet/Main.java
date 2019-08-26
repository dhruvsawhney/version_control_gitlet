package gitlet;

import java.io.*;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author Dhruv Sawhney
*/
public class Main {

    private CommitTree tree_;

    private CommitTree getTree_() {
        return tree_;
    }

    private void setTree_(CommitTree tree_) {
        this.tree_ = tree_;
    }

    // create the .gitlet directory
    // add the two subdirectories: Commit, Blobs
    private boolean createGitletDirectory(){
        File dir = new File(Utils.GITLET_DIR);

        if(!dir.exists()){
            dir.mkdir();

        } else {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            return false;
        }

        File commitDir = Utils.join(Utils.GITLET_DIR, Utils.COMMIT_DIR);

        if(!commitDir.exists()){
            commitDir.mkdir();
        }

        File blobDir = Utils.join(Utils.GITLET_DIR, Utils.BLOBS_DIR);

        if (!blobDir.exists()){
            blobDir.mkdir();
        }
        return true;
    }

    private void SaveTree(){

        File outFile = Utils.join(Utils.GITLET_DIR, Utils.COMMIT_TREE);

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

    private CommitTree loadTree(){

        File inFile = Utils.join(Utils.GITLET_DIR, Utils.COMMIT_TREE);

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

        if (args.length != numArgs){
            System.out.println("Incorrect operands.");
            System.exit(1);
        }
    }

    private void checkoutArgs(String[] args){

        if (args.length > 4){
            System.out.println("Incorrect operands.");
            return;
        }

        // len is at least 1 as this function is called from main
        if (args.length == 2){
            this.getTree_().checkoutBranch(args[1]);
        } else if (args.length == 3){

            if (!args[1].equals("--")){
                System.out.println("Incorrect operands.");
                return;
            }
            this.getTree_().checkoutFile(args[2]);
        } else if (args.length == 4){

            if (!args[2].equals("--")){
                System.out.println("Incorrect operands.");
                return;
            }
            this.getTree_().checkoutFilePrevCommmit(args[1], args[3]);
        }
    }

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        if (args.length == 0){
            System.out.println("Please enter a command");
            return;
        }

        // load commit-tree if .gitlet initialized
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

            case "rm":

                checkNumArgs(2, args);
                program.getTree_().removeFile(args[1]);
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

            case "status":

                checkNumArgs(1, args);
                program.getTree_().status();
                break;

            case "checkout":

                // check arguments and invoke specific checkout command
                program.checkoutArgs(args);
                break;

            case "branch":

                checkNumArgs(2, args);
                program.getTree_().branch(args[1]);
                break;

            case "rm-branch":

                checkNumArgs(2, args);
                program.getTree_().removeBranch(args[1]);
                break;

            case "reset":

                checkNumArgs(2, args);
                program.getTree_().reset(args[1]);
                break;

            case "merge":

                checkNumArgs(2, args);
                program.getTree_().merge(args[1]);
                break;

            default:

                System.out.println("No command with that name exists.");
                break;
        }

        // save commit-tree to disk
        program.SaveTree();
    }
}
