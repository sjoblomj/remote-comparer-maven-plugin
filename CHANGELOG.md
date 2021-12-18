# Changelog

All notable changes to this project will be documented in this file.


## [1.0.3] - 2021-12-18

### Added

- Thread safe annotation to plugin.
- This Changelog file.

### Changed

- Will now look for the `localFilePath` relative to the base directory of the project, rather than attempting to find the path by using the projects name.
- Readme improvement.
- More elegant handling of warning message decoration.

## [1.0.2] - 2021-12-16

### Changed

- Readme improvement.
- Changing groupId of plugin.

## [1.0.1] - 2021-12-16

### Changed

- Nothing, trial release to GitHub.

## [1.0.0] - 2021-12-13

### Added
- Everything; Initial release. Can compare a local file to a remote file, and has options for timeout, whether to fail if files are different, whether to fail if either file can't be found, and an option to use a small warning message.
