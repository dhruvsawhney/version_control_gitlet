shopt -s nullglob
for i in autograder/*.in; do
    python3 runner.py --verbose $i
done
