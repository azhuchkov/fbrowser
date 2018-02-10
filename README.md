fBrowser Demo Application
==================================

This is a simple web-application which has been written as a test project. The main idea of it is to make a reusable
web-component which implements some base functionality for browsing files. Files are loaded dynamically by AJAX calls,
ZIP-archives can be browsed as directories, different file types have different icons. The state of file tree is 
saved automatically, so the page can be refreshed without any loss. 

The application is built on top of [Spring](https://spring.io/) stack 
using [Spring Boot](https://projects.spring.io/spring-boot/) for quick start.
The component itself is implemented as a [jQuery](https://jquery.com/) plugin.

![fBrowser Demo](https://github.com/azhuchkov/fbrowser/blob/master/fbrowser-demo-600px.gif)


Live Demo
---------

The application instance is deployed to [AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/). 
To see it live follow the link: <http://fbrowser.us-west-2.elasticbeanstalk.com/>. There you can browse 
container's filesystem and contents of the application's itself (`/app.jar`).
 

Prerequisites
-------------

JDK 1.7

>See below for running using Docker.


Building
--------
The most simple way to build the application is to use Gradle wrapper that bootstraps itself and then do the job:

`$ ./gradlew build`


Running
-------

After the app is built (and its uber-jar is packaged), it's possible to run it:

`$ java -jar build/libs/fbrowser-0.1.0-SNAPSHOT.jar`

And that's it! No need to install external Servlet Container or something else. After application initialization 
you can open <http://localhost:8080/> in your browser. Configuration can be overriden using command line arguments, 
e.g.: `--server.port=9000`

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

  * `server.port` sets Tomcat connector port. Example: `--server.port=9000`. Set to `8080` by default.


Running using Docker
--------------------

If you are a [Docker](https://www.docker.com/) user you can just run prebuilt application with all the dependencies 
using the following command:

`$ docker run -p 8080:8080 azhuchkov/fbrowser`

The _image_ `azhuchkov/fbrowser` would be downloaded from [Docker Hub](https://hub.docker.com/r/azhuchkov/fbrowser/) 
and started as a new container. If you want to cleanup the container when it exits, add option `--rm`.

That way you will see the root of the container's file system hierarchy. If you want to browse something more
interesting, you should mount you local directory and pass its path as an argument:

`$ docker run -p 8080:8080 -v '<local-absolute-path>:/data' azhuchkov/fbrowser --filesystem.base=/data`

where you __must__ change the `<local-absolute-path>` with absolute path of your own directory you want to view.

Building Docker image
---------------------

To build a local image for Docker you must have Docker installed. Then just run the appropriate Gradle task:

`$ ./gradlew docker`

After that you can find the new image (`azhuchkov/fbrowser`) in the output of: 

`$ docker images`

 
