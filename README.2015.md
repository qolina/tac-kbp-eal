This is code developed by BBN to support the
2015 KBP Event Argument and Linking Shared Task.
A description of this task may be found [here](http://www.nist.gov/tac/2015/KBP/Event/Argument/guidelines.html).

This repository contains three artifacts:
* `kbp-events2014` contains classes to represent system responses, assessments, and linkings for
the task. While these are mainly to support the executables in this repository,
if your system is based on Java or another JVM language, you are strongly encouraged
to use them.  Note that they can be used from Python via Jython.
* `kbp-events2014-scorer` contains the scoring code (but not scoring binary).
* `kbp-events2014-bin` contains all the executable programs: the validator, the pooler, the scorer, etc.

## Building

Requirements:
* [Maven](http://maven.apache.org/)

Build steps:
* Do `mvn install` from the root of this repository.
* do `chmod +x kbp-events2014-bin/target/appassembler/bin/*` (you only need to do this the first time)
* do `chmod +x kbp-events2014-scorer/target/appassembler/bin/*` (you only need to do this the first time)

[![Build Status](https://semaphoreci.com/api/v1/projects/a11da8fb-4e16-48bf-af3a-a4e1680f161e/467466/badge.svg)](https://semaphoreci.com/rgabbard-bbn/kbp-2014-event-arguments)

## Using
### System Output Stores and Annotation Stores
A _system output store_ represents the output for a KBP Event Argument system on
a collection of documents. It consists of a directory containing exactly one
file per input document, named by the docID. The internal format of these files
is described in the task specification linked to above.

An _annotation store_ contains assessments of the answers from a system output
store.  Its format is the same as a system output store except within the files
there are additional assessment columns, as described in the task specification.

A _linking store_ groups arguments into event frames.  It consists of a directory
containing exactly one file per input document, named by the docID. The internal format
of these files is described in the task specification linked to above.

### Evaluation Workflow

The following workflow will be used during the evaluation.  All executables referenced below may be found in
either `kbp-events2014-bin/target/appassembler/bin` or `kbp-events-2014-scorer/target/appassembler/bin`.

* a 'quote filter' to remove material with CAS and BF offsets in quoted regions
will be built from the original text of the data set.
* competitor submissions will be validated using `validateSystemOutput`.
* all material from quoted regions will be removed from competitor submissions.
* all submissions will be combined into a single system output store using
`poolSystemOutput`.
* this combined system output store will be transformed into an annotation store
using `importSystemOutputToAnnotationStore`.
* LDC annotators will assess this annotation store.
* LDC annotators will link all correctly assesses linkable responses to create reference linkings
for each document.
* All competitor submissions will be evaluated against the complete annotation
store and reference linkings using `KBP2015Scorer`.

### Parameter Files
Most of the executables take parameter files as input.  These have the format
```
key1: value1
# this is a comment!
key2: value2
```

### `validateSystemOutput`
This program will check that your submission:
* has the correct format
* contains only legal event roles and types

If either of these fail, the program will halt with an error message. In the future,
we plan to add enforcement of rules concerning what portions of answers may
come from within `<quote>` regions.

Additionally, this program will dump to standard output a human-readable version
of your responses with all offsets resolved to strings from the original documents
so that you can check for mistakes.

This program takes the following parameters:
* `systemOutputStore`: the path of the system output store to be validated
* `validRoles`: is `data/2014.types.txt` (for KBP 2014)
* `dump`: whether to dump response to `stdout` in a human readable format.
* `docIDMap`: (required if `dump` is `true`) a list of tab-separated pairs of doc ID and path to the
them to standard output.

Note that this currently does not validate linkings.

### `poolSystemOutput`
Combines the system output from multiple systems into a single system output store.

Parameters:
* `storesToPool`: a file listing paths to stores to pool, one per line
* `pooledStore`: the location to write the pooled store to
* `addMode`: either `CREATE` to create a new store for output (overwriting anything
currently there) or `ADD` to append to an existing store.

### `importSystemOutputToAnnotationStore`
Turns a system output store into an annotation store ready for LDC's annotators.

Parameters:
* `argumentOutput`: system output to import
* `annotationStore`: location to create annotation store. The program will
refused to create a new annotation store over an existing, non-empty one.

### `KBP2015Scorer`
Scores system output against an annotation store.

Parameters:
* `answerKey`: the path to an annotation store containing assessments for all system responses
* `referenceLinking`: the path to the reference linking store
* `documentsToScore`: a file listing the IDs of the documents to be scored, one per line
* either `argumentOutput` or `systemOutputsDir`.  If `argumentOutput`, the value must be a path to the system output to be scored.  This path must have an `arguments` and a `linking` subdirectory containing a system output store and a linking store, respectively.  If `systemOutputsDir`, the path must contain sub-directories representing the outputs of multiple systems.  Each such sub-directory must have the format described above for `argumentOutput`.
* neutralizeRealis: map all realises in system input and the answer key to ACTUAL. This will be false for the evaluation, but setting it to true can be useful for diagnostic purposes.
*attemptToNeutralizeCoref: Another diagnostic parameter. If set to true, attempts to minimize the impact of system coreference errors on scoring by
  * Leaving untouched any system response whose base filler assessment correctness matched its CAS assessment correctness.
  * For other responses, if the base filler assessment is correct, replace the response with another which matches it in type, role, and base filler but with a correctly annotated CAS, if possible.  If this is not possible, replace it with any correcrly assessed response match its type and role.  If this is still not possible, delete the response.

## Baseline linking
We provide a baseline implementation of event argument linking for those who wish to try out the 2015 scorer but have not yet developed their own algorithm.  This baseline implementation simply links together all arguments of the same event type in a document.  To run this, use `ApplyLinkingStrategy`.

Parameters:
* `argumentSystemStore`: the system's argument output
* `linkingSystemStore`: the path to write the baseline linking to

## Questions
### How can I use the `Response`, etc. in my system's code?
Add the following to the `dependencies` section of your project's `pom.xml` (or take similar steps if using Gradle, etc.):
```
<dependency>
      <groupId>com.bbn.kbp.events2014</groupId>
      <artifactId>kbp-events2014</artifactId>
      <version>current version listed in pom.xml of this repository</version>
</dependency>
```
This artifact is not deployed to Maven Central, so you will need to
install it in your local repository as described above.

## Contact
For questions concerning the software, please contact `rgabbard@bbn.com`.  If you 
have bugs or feature requests, you can use the GitHub Issue Tracker. The issue tracker is preferred so future users can benefit from the answers to your questions. To track changes to this repository, follow https://github.com/rgabbard-bbn/kbp-2014-event-arguments/commits/master.atom in an RSS reader.
