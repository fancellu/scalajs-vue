![scalajs-vue Logo](http://felstar.com/projects/scalajs-vue/img/scalajs-vue-cliff.png)
# scalajs-vue

## Scala.js bindings for Vue.js and an example application 

[![Build Status](https://travis-ci.org/fancellu/scalajs-vue.svg?branch=master)](https://travis-ci.org/fancellu/scalajs-vue)

Example app showing the use of [Scala.js](http://www.scala-js.org/) with [Vue.js](http://vuejs.org/) 
The bindings for Vue.js are in `com.felstar.scalajs.vue`

## Get started

To get started, run `sbt ~fastOptJS` in this example project. 

Obviously you need to have [SBT](http://www.scala-sbt.org/) installed. 

This should
download dependencies and prepare the relevant javascript files. It then kicks off a web server to serve the html and JS files.

If you open
[localhost:12345/target/scala-2.11/classes/index-dev.html](http://localhost:12345/target/scala-2.11/classes/index-dev.html) in your browser, it will show you a sample app, doing various bindings and Scala code. Its not meant to be pretty, simply instructive. Do open the Console to see debug messages.

## Demo

[live demo](http://dinofancellu.com/demo/scalajsVue/)

## Development

If you change your source HTML (inside `/src/main/resources`) or Scala (inside `/src/main/scala/example/Todo.scala`, sbt will recompile as needed. 
You then just have to refresh the page to see the new version. Hopefully having an example application will make it clearer on how to use Vue from Scala, i.e. monkey see, monkey do.

## The optimized version

Run `sbt fullOptJS` and open up `index-opt.html` for an optimized version
of the final application, useful for final publication. You may well need to copy over `index-dev.html` to get your latest changes. Be sure to refer to the correct JS as well, as it will have a different name than the fast compiled version. e.g.

	<script type="text/javascript" src="../scala-js-vue-js-example-opt.js"></script>

## Eclipse integration

If you want to edit in Eclipse (can't compile yet, but still very useful having full IDE with code completion), just run `sbt eclipse` the open the generated .project file inside eclipse. Keep sbt running in order to do the JS Compile. [https://github.com/typesafehub/sbteclipse/wiki](https://github.com/typesafehub/sbteclipse/wiki)

## Status

This is just a quick proof of concept and some bindings for those wanting to use Vue.js from Scala.js. Feel free to get in contact with any issues etc.


