SSH backend migration plan
===

Corresponding issue: [#57](https://github.com/apatrushev/ServerAccess/issues/57)

Currently, we have following classes that depend on `mindterm` library:

![diagram](mindterm.png)

Refactoring should move along with the plan:

 - [x] remove dependency from `HTTPProxy` class
 - [ ] remove dependency from `Util` class
 - [ ] extract interface from `Backend` class
 - [ ] use new interface everywhere
 - [ ] create (empty) alternative `Backend` implementation based on `sshj` or `jsch`
 - [ ] impement (temporarily) config option that allows to choose alternative `Backend` implementation
 - [ ] make alternative backend work, step by step
 - [ ] remove config option
 - [ ] remove old backend
 - [ ] clean all the things
