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
By default these are expected to be located in the unpacked build's `conf` directory and named specifically as follows:
* `conf/`
  * `client-certificate.pem`
  * `ReferenceSettings.xml`
  * `RepositorySettings.xml`

The default placement of these files can be changed in the settings provided by `conf/config.yaml`.

It may be necessary to run `chmod +x start-script.sh` in order to run the tool.

## Usage of tool
The tool must be run from within the `scripts` directory.

Run the tool as follows:  
`./start-script.sh <collectionID> <checksum> <fileID>`

E.g.:  
`./start-script.sh integrationtest1 b1946ac92492d2347c6235b4d2611184 testfile`
