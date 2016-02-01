How to release a new version
============================

1. Make sure all issues for the current milestone are closed.

2. Run ``version`` script to set new version and generate changelog:

    ``./resource/version <new_version_number>``

A list of changes in the current version will be extracted from git history and written into ``CHANGELOG.TXT`` file. The new version will be written into ``VERSION.TXT`` file.

*N.B.*: it might be a good idea to move this script into ``build.gradle``.

3. Edit changes list in ``CHANGELOG.TXT`` manualy to make it easier to read and understand.

4. Make a commit containing changes in ``CHANGELOG.TXT`` and ``VERSION.TXT``. Comment this commit with the new version number.

5. Create annotated tag on this commit:

    ``git tag -a serveraccess-<new_version_number> -m 'Release <new_version_number>'``

6. Push all changes. Don't forget to push the tag also!

7. Close milestone.

8. Let the others know about the release!
