package com.felstar.scalajs.vue

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom._
import js.annotation._
  
  @js.native
  @JSGlobal
   class Vue extends js.Object {
    def this(obj: js.Any) = this()   
     // instance properties
    val $el:raw.Element =js.native
    val $data:js.Dynamic =js.native
    val $options:js.Dynamic =js.native
    val $parent:Vue =js.native
    val $root:Vue =js.native
    val $children:js.Array[Vue] =js.native
    val $:js.Dynamic =js.native
    val $$:js.Dynamic =js.native
    val $index:Int =js.native
    val $key:js.Any =js.native
    val $value:js.Any =js.native
      // Data
    type Callback=js.Function2[_,_,Unit]
    def $watch(expOrFn:js.Any,callback:Callback):Unwatch=js.native
    def $watch(expOrFn:js.Any,callback:Callback,options:js.Any):Unwatch=js.native
    def $get(exp:String):js.Any=js.native
    def $set(target:js.Any,key:js.Any, value:js.Any):Unit=js.native
    def $delete(target:js.Any,key:js.Any):Unit=js.native
    def $add(key:String,value:js.Any):Unit=js.native
    def $eval(exp:String):js.Any=js.native
    def $interpolate(templateString:String):js.Any=js.native

      // Events
    def $dispatch(event:String):Unit=js.native
    def $dispatch(event:String,args:js.Any):Unit=js.native
    def $broadcast(event:String):Unit=js.native
    def $broadcast(event:String,args:js.Any):Unit=js.native
    def $emit(event:String):Unit=js.native
    def $emit(event:String,args:js.Any):Unit=js.native
    def $on(event:String,callback:js.Function):Unit=js.native
    def $once(event:String,callback:js.Function):Unit=js.native
    def $off():Unit=js.native
    def $off(event:String):Unit=js.native
    def $off(event:String,callback:js.Function):Unit=js.native
      // DOM
    def $appendTo(elementOrSelector:js.Any):Unit=js.native
    def $appendTo(elementOrSelector:js.Any,callback:js.Function):Unit=js.native
    def $before(elementOrSelector:js.Any):Unit=js.native
    def $before(elementOrSelector:js.Any,callback:js.Function):Unit=js.native
    def $after(elementOrSelector:js.Any):Unit=js.native
    def $after(elementOrSelector:js.Any,callback:js.Function):Unit=js.native
    def $remove():Unit=js.native
    def $remove(callback:js.Function):Unit=js.native
    def $nextTick(callback:js.Function):Unit=js.native
      // Lifecycle
    def $mount(elementOrSelector:js.Any):Vue=js.native
    def $destroy(destroy:Boolean=false):Unit=js.native
    def $compile(element:raw.Element):js.Function=js.native
    def $addChild():Unit=js.native
    def $addChild(options:js.Any):Unit=js.native
    def $addChild(options:js.Any,constructor:js.Function):Unit=js.native
    def $addChild(constructor:js.Function):Unit=js.native
  }

  @js.native
  @JSGlobal
   class Unwatch extends js.Object {
    def unwatch:Unit =js.native      
   }

  @js.native
  @JSGlobal
    object Vue extends js.Object{
     def config:js.Dynamic=js.native
     def extend(obj:js.Any):Vue=js.native
     def nextTick(func:js.Function):Unit=js.native
     def directive(id:String,definition:js.ThisFunction):Unit=js.native     
     def directive(id:String,definition:js.Any):Unit=js.native
     def elementDirective(id:String,definition:js.ThisFunction):Unit=js.native     
     def elementDirective(id:String,definition:js.Any):Unit=js.native
     def filter(id:String):js.Any=js.native
     def filter(id:String,func:js.Function):js.Any=js.native
     def component(id:String):js.Any=js.native
     def component(id:String,definition:js.Function):js.Any=js.native
     def component(id:String,definition:js.Any):js.Any=js.native
     def transition(id:String):js.Any=js.native
     def transition(id:String,definition:js.Any):js.Any=js.native
     def partial(id:String):js.Any=js.native
     def partial(id:String,template:String):js.Any=js.native
     def use(plugin:js.Any):js.Any=js.native
     def use(plugin:js.Any,args:js.Any*):js.Any=js.native
     def set(target:js.Any,key:js.Any, value:js.Any):Unit=js.native
     def delete(target:js.Any,key:js.Any):Unit=js.native
    }

   @js.native
   @JSGlobal
   class Directive extends js.Object {
    val name:String =js.native
    val rawName:String =js.native
    val value:String =js.native
    val expression:String =js.native
    val modifiers:js.Any =js.native
    val `def`:js.Any =js.native
   }



