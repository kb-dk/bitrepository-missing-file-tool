# Missing File Tool
Tool for repairing individual missing files in a Bitrepository collection.

## Building the project
Compile and build the project by running `mvn clean package`.

The build is then found in `target/missing-file-tool-{version}-distribution.tar.gz`.

## Before running the tool
After building the project and unpacking the distribution-tar, the tool can be found in the `bin` 
directory as `start-script.sh`.

In order to run the script, you must provide a client certificate and reference-/repository settings 
with the necessary info to communicate with your Bitrepository-instance.  
These should be located in the unpacked build's `conf` directory and named specifically as follows:
* `conf/`
  * `client-certificate.pem`
  * `ReferenceSettings.xml`
  * `RepositorySettings.xml`

It may be necessary to run `chmod +x start-script.sh` in order to run the tool.

## Usage of tool
The tool must be run from within the `scripts` directory.

Run the tool as follows:  
`./start-script.sh <fileID> <checksum> <collectionID>`

E.g.:  
`./start-script.sh testfile b1946ac92492d2347c6235b4d2611184 integrationtest1`
