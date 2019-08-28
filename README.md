
Project overview:

    Gitlet, is a version-control system mimics some of the basic features of the popular version-control system, Git.

    This is a design project built from scratch and guidelines for specific command overview can be found here: https://cs61bl.org/su19/projects/gitlet/

The list of Gitlet commands implemented are:

    init, add, commit, rm, log, global-log, find, status, checkout, branch, rm-branch, reset and merge
    
    
Using the version-control system locally:
    
    1. Clone this repository

    2. From the root dir, cd into "app" dir

    3. We need to compile the source files: javac gitlet/*.java

    4. Create a test dir and copy the .class files in a new gitlet dir: 

        mkdir ~/test-gitlet

        mkdir ~/test-gitlet/gitlet

        cp gitlet/*.class ~/test-gitlet/gitlet

    This will place a directory called test-gitlet in your home directory, along with all the necessary files to run your version-control system. You can then cd into this directory, and start running commands!

        cd ~/test-gitlet
        java gitlet.Main init
        java gitlet.Main status
        ...


## Command Usage (gitlet commands after each command description):

1. init: initialize the .gitlet repository

    java gitlet.Main init

2. add: Adds a copy of the file as it currently exists to the staging area

    java gitlet.Main add [file name]
    
3. commit: Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit.

    java gitlet.Main commit [message]

4. rm: Untrack the file; that is, indicate that it is not to be included in the next commit
    
    java gitlet.Main rm [file name]

5. log: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit. 
    
    java gitlet.Main log
    
6. global-log: Like log, except displays information about all commits ever made. 
    
    java gitlet.Main global-log
    
7. find: Prints out the ids of all commits that have the given commit message.
    
    java gitlet.Main find [commit message]
    
8. status: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged or marked for untracking. 
    
    java gitlet.Main status

9. checkout: Checkout is a kind of general command that can do a few different things depending on what its arguments are.

    java gitlet.Main checkout -- [file name]
    
    java gitlet.Main checkout [commit id] -- [file name]
    
    java gitlet.Main checkout [branch name]
    
10. branch: Creates a new branch with the given name branch name, and points the branch at the current head node.

    java gitlet.Main branch [branch name]
    
11. rm-branch: Deletes the branch with the given name.
    
    java gitlet.Main rm-branch [branch name]
    
12. reset: Checks out all the files tracked by the given commit.

    java gitlet.Main reset [commit id]
    
13. merge: Merges files from the given branch into the current branch. 
    
    java gitlet.Main merge [branch name]
    

