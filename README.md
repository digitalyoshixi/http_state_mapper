# Todo
- [ ] able to create valid state machines
    - [x] first, each transition and state is visible
    - [ ] reduce power of merging by custom automata learning algo
- [ ] post-corpus-parsing, use myers diffing algorithm to remove subtle variants (session cookies, etc, but keep param names, etc)
- [ ] generate dataset and classifier for negative samples
- [ ] look into hybrid learning (look into a few techniques)
- [x] look into human in the loop hybridized learning (https://arxiv.org/pdf/1707.09430)
    - [] revised merging algorithm must have confidence score of promoting blue -> red, human verifies this, or sets limits to confidence to promote
- [ ] look into developing a burp fuzzer extension

# Compilation and Run Command
```
mvn -q exec:java -Dexec.mainClass=webmapper.WebMapper -Dexec.args="corpus/ctfreqs"
```
