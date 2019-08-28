
Project overview:

    Gitlet, is a version-control system mimics some of the basic features of the popular version-control system, Git.

    This is a design project built from scratch and guidelines for specific command overview can be found here: https://cs61bl.org/su19/projects/gitlet/

The list of Gitlet commands are:

    init, add, commit, rm, log, global-log, find, status, checkout, branch, rm-branch, reset and merge
    
    
Using the version-control system locally:
    
    1. Clone this repository

    2. From the root dir, cd into "app" dir

    3. We need to compile the source files: javac gitlet/*.java

    4. Create a test dir and copy the .class files in a gitlet dir: 

        mkdir ~/test-gitlet

        mkdir ~/test-gitlet/gitlet

        cp gitlet/*.class ~/test-gitlet/gitlet

    This will place a directory called test-gitlet in your home directory, along with all the necessary files to run your version-control system. You can then cd into this directory, and start running commands!

        cd ~/test-gitlet
        java gitlet.Main init
        java gitlet.Main status
        ...


## Command Usage:
    
    #init

        java gitlet.Main init
