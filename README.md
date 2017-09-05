[![internal JetBrains project](http://jb.gg/badges/internal-flat-square.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

Experiments with Java class loaders

License
=======
Apache 2.0


Load classes from resources
===========================

This project contains of 4 modules.

`resource-clazz-loader`
   this module contains main code that is used to load classes

`packed`
   here goes simplified code intended to be used from user's code
   all classes are renamed to fit version
   there is ```factory-name.txt``` file containing actual name

`packed-impl`
   generated code, intermediate package used to make IDEA compile renamed classes
   you should avoid any changed in sources under the module

`packed-test`
   this is a black-box tests module. There is only dependency to
   create artifact


Installation
============

* Take the .jar from ```packed``` artifact output and copy it's contents into the main ```.jar``` of your application/library
* Copy and rename ```factory-name.txt``` file and put in into your app's main ```.jar```
* Add URLs of your ```.jar``` files to load in the file. One pre line
* Add those ```.jar``` files to the main ```.jar``` resources
* Use the following code in your app to make listed ```.jar``` files be loaded in a classloader

          private ClassLoader classLoaderFromResources(@NotNull ClassLoader parent) {
            try {
              final Scanner scanner = new Scanner(parent.getResourceAsStream("renamed-libraries-list"), "utf-8");
              return (ClassLoader)parent.loadClass(scanner.nextLine()).getMethod("scan", ClassLoader.class, Scanner.class).invoke(null, parent, scanner);
            } catch (Exception e) {
              throw new RuntimeException("Failed to load classloader. " + e.getMessage(), e);
            }
          }


Misc
====
You may use the following Ant pre/post-action in IDEA artifact to generate classpath for your lib:

            <pathconvert pathsep="&#xA;" property="classpath">
              <fileset dir="${artifact.output.path}" includes="**/*.jar"/>
              <chainedmapper>
                <flattenmapper />
                <globmapper from="*" to="PREFIX/*"/>
              </chainedmapper>
            </pathconvert>
            <echo append="true" file="${artifact.output.path}/classpath">&#xA;${classpath}&#xA;</echo>

See [hint](http://stackoverflow.com/questions/1456852/how-can-i-print-a-fileset-to-a-file-one-file-name-per-line) for more details

Limitations
===========

* There is a custom (non URLClassLoader) implementation of classloader
* This classloader prefers local classes to classes from parent classloader
* Some libraries may fail to resolve patterns on resources (i.e. SpringFramework)
* Only ```.jar``` are supported in classpath

