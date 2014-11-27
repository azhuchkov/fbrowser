 fBrowser Demo Application
===========================

Requirements
------------

JDK 1.7


Building
--------
The most simple way to build the application is to use Gradle wrapper that bootstraps itself and then do the job:

`$ ./gradlew build`


Running
-------

After the app is built (and its uber-jar is packaged), it's possible to run it:

`$ java -jar build/libs/fbrowser-0.1.0-SNAPSHOT.jar`

And that's it! No need to install external Servlet Container or something else. Configuration can be overriden
using command line arguments, e.g.: `--server.port=9000`

To run it as a part of build lifecycle ( can be useful in development to be able to update static and template files ):

`$ ./gradlew bootRun`

In this case configuration can be overriden using: `-PjvmArgs='-Dfilesystem.base=/home/andrey'`

Hit `ctrl+c` to shutdown application.


Configuration
-------------

Application configuration placed in application.yml file. It is possible to customize options
using command line arguments: `--option=value`

Available options:

  * `filesystem.base` sets browser root directory. By default application uses process working directory as a root.
    You can choose any directory you want like this: `--filesystem.base=/home/user`

  * `server.port` sets Tomcat connector port. Example: `--server.port=9000`
