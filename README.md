# futility-lib

This project vends a jar of analysis functionality for [futility analysis tool](https://f-utility.hms.harvard.edu/).


## Installation

    $ mvn clean install

This library isn't published to maven at the moment, as realistically the functions provided aren't useful external to futility (although there is a reasonably efficient multinomial implementation).

Install the jar to your local repository to support building [futility-server](https://github.com/blandflakes/futility-server).

In the future, this should be published to maven, or perhaps just put into the lib directory of futility-server.

## Usage

This is a library.

## Maven Dependency Information:

    org.machinery.futility/futility-lib "1.0-SNAPSHOT"

TODO: Generate javadoc for the API. For now, see [futility-server](https://github.com/blandflakes/futility-server) for Clojure code that uses the Java code.

## TODO

1. Vend to maven or put the jar in futility-server's lib
2. Unit tests

## License

Copyright © 2016 Brian Fults

Do whatever you want with this code. If it's helpful, send me a beer. If it's horrible, pretend I didn't write it, but in any case please give me credit if your work is any sort of derivation.
