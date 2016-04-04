Haedus Toolbox SCA

Samantha F McCabe, 2016

# Changelog:
<!---
## [Unreleased]
### Added 
### Changed 
### Deprecated
### Removed 
### Fixed 
### Security 

## [0.8.0]
### Added

### Changed

### Deprecated
### Removed 
### Fixed 
### Security 
-->

## [0.7.0]
### Added 
* Support for phonetic features [toolbox-sca-47; toolbox-sca-71]
* Support for negation/complementation in state machines [toolbox-sca-3; tooblox-sca-79]
* Add support for feature constraints [toolbox-sca-87] and aliases [toolbox-sca-90]

### Changed 
* Ensured that lexicons were re-normalized before being written out [toolbox-sca-50]
* Fixed bug  preventing `EXECUTE` command from working correctly [toolbox-sca-74]
* Made `Segment` object implement `Comparable` interface [toolbox-sca-72]
* Corrected behavior of `NOT` chains in rule conditions [toolbox-sca-81]
* Fixed but where `IMPORT` did not load variable definitions [toolbox-sca-84]
* Refactored feature arrays to an interface

<!---
### Deprecated
### Removed 
### Fixed 
### Security 
-->

## [0.6.0] 2015-08-16
### Added 
* Allows insertion / epenthesis of segments
* Logging to file + console
* Now attempts to log all compilation errors encountered in all loaded scripts before quitting

### Changed
* Improved logging, including line number and file name for the script in which the error was found.
* Added some clarifications to the manual where users indicated some sections were unclear.

### Removed
* "Basic mode" for loading a single rules file and lexicon; all lexicon IO is specified in the rules file

<!---
 ### Deprecated 
 ### Removed 
 ### Fixed 
 ### Security 
-->

## [0.5.0] 2015-07-25
### Added
* Runnable using shell scripts or batch scripts
* User Manual

### Changed
* Improved normalization mode operation
* Rewrote regular expression engine, including support for the `.` dot metacharacter
* Numerous bug fixes

<!---
### Deprecated
### Removed 
### Fixed 
### Security 
-->


## [0.1.0] 2014-10-25
### Added
* Allow user to change segmentation and normalization modes
* Added support for scripting language (`IMPORT`, `EXECUTE` commands)
* Added file IO commands (`OPEN`, `CLOSE`, `WRITE`) and file-handles;
* Support for metathesis and total assimilation through use of back-references
* Added `OR` condition chaining
* Added `NOT` for excluding conditions
* Improved back-end handling of compiled rules
