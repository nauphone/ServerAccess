SSH backend migration plan
===

Corresponding issue: [#57](https://github.com/apatrushev/ServerAccess/issues/57)

Currently, we have following classes that depend on `mindterm` library:

![diagram](mindterm.png)

Refactoring should move along with the plan:

Stage 1: isolate dependency upon MindTerm inside Backend class solely
---

 - [x] remove dependency from `HTTPProxy` class
 - [x] remove dependency from `Util` class

Stage 2: implement Backend feature toggle
---

 - [ ] extract interface from `Backend` class
 - [ ] use new interface everywhere
 - [ ] create (empty) alternative `Backend` implementation based on `sshj` or `jsch`
 - [ ] impement (temporarily) config option that allows to choose alternative `Backend` implementation

Stage 3: implement new Backend
---

 - [ ] make alternative backend work, step by step (there will be more steps)

Stage 4: remove feature toggle, simplify the code
---

 - [ ] remove config option
 - [ ] remove old backend
 - [ ] clean all the things
