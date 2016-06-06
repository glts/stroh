# stroh

Stroh is a simple ‘intend to read/watch/attend’ list. A friend
recommends a book, you write it down, at some point you get around to it
and can tick it off the list.

What’s interesting about this app is that everything related to *time*
(when an item was added, when it was completed, status changes, and so
on) is not actually modelled in the data model at all! Instead support
for time is fully automated by the storage backend, a Datomic database.
Datomic is an unusual database in that every transaction it processes is
permanently recorded in the database history automatically.

Apart from that, this is just a standard Clojure CRUD webapp and a
playground for my Clojure programming.

## Prerequisites

*   Java 8
*   Leiningen (2.6.1)
*   Datomic Pro (0.9.5372)
*   Couchbase Server (4.0.0-4051)

## Installation

This webapp is built with Leiningen and can be run from the dev REPL or
as a uberjar. Note the following additional requirements:

*   The Datomic Pro client library dependency must be installed locally.
*   A Couchbase Server must be up and running and a bucket configured
    for the app to use.
*   A Datomic Pro transactor is required as well.

## Development

To work on the app, first make sure that the Datomic transactor and the
Couchbase Server are both running.

Then start a REPL:

    lein repl

Open the `dev/user.clj` file, and evaluate the commented `(start)` form.

Now navigate to `localhost:3000` to access the app.

## Production artefact

To build the app for deployment, simply create a uberjar:

    lein uberjar

The final JAR artifact can then be run directly:

    java -jar target/uberjar/stroh-0.1.0-SNAPSHOT-standalone.jar

## Datomic administration

In the following assume that `DATOMIC_HOME` points to where you
extracted the Datomic Pro distribution. For example:

    export DATOMIC_HOME=$HOME/src/stroh/vendor/datomic-pro-0.9.5372

### Console

To start a Datomic console (an admin UI in the browser), first make sure
both the storage backend and a transactor are running, then switch to
the Datomic distribution and run `bin/console`:

    cd $DATOMIC_HOME
    bin/console -p 8080 strohtest datomic:couchbase://localhost/strohtest/

Access the console at `http://localhost:8080/browse`.
