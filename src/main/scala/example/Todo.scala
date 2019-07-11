package example
import org.scalajs.dom

import scalajs.js.annotation.JSExport
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import dom.ext.Ajax

import scalajs.js
import js.Dynamic.literal
import com.felstar.scalajs.vue._
import org.scalajs.dom.raw.HTMLElement

import js.annotation._

@JSExportTopLevel("example.Todo")
object Todo extends {
  
    @js.native
    trait DemoVue extends Vue{
      var title:String=js.native
      var n:Double=js.native
      var todos:js.Array[DemoVueTodo]=js.native
    }

    type DemoVueMethod=js.ThisFunction0[DemoVue,_]

    @js.native
    trait DemoVueTodo extends js.Object{
      var done:Boolean=js.native
      var content:String=js.native
    }

    object DemoVueTodo{
      def apply(done:Boolean,content:String)=literal(done=done,content=content).asInstanceOf[DemoVueTodo]
    }
    
  @JSExport
   // we return the DemoVue so we can use it back in JS
   // could have returned raw Vue of course
  def main():DemoVue = {

    val tasks=js.Array("Learn JavaScript","Learn Vue.js","Learn Scala.js")
//
    def ts=new java.util.Date().toString
    
    Vue.component("my-component", literal(
        props=js.Array("myMsg"),
        template="<p>A custom component with msg {{myMsg}} <slot>default content</slot></p>"))
   
    Vue.directive("mydirective", literal(
      update=(el:HTMLElement, directive:Directive)=>{el.innerHTML="This comes from my-directive with contents "+directive.value+" and expression "+directive.expression}
     )
    )

    val demo = new Vue(
        literal(el="#demo",
          data=literal(
                message="Hello Vue.js!!!!!",
                title="Todo App",
                todos=tasks.map(content=>literal(done=content==tasks.head,content=content)),
                barValue= 100,
                n=0
               ),//,
               // js.ThisFunction would be fine, just trying to be more type specific  
          methods=literal(clickHandler=((demoVue:DemoVue)=>demoVue.n-=1):DemoVueMethod,
                        addTask=((demoVue:DemoVue)=>demoVue.todos.append(DemoVueTodo(false,s"new $ts"))):DemoVueMethod,
                        change1st=((demoVue:DemoVue)=>Vue.set(demoVue.todos, 0,DemoVueTodo(false,ts))):DemoVueMethod,
                        remove=((demoVue:DemoVue,idx:Int)=>Vue.delete(demoVue.todos,idx)):js.ThisFunction1[DemoVue,Int,_],
                        flipAll=((demoVue:DemoVue)=>demoVue.todos.foreach(td=>td.done= !td.done)):DemoVueMethod
                     ),
          computed=literal(todosComputed=(demoVue:DemoVue)=> demoVue.todos.map(_.content)),
//
          filters=literal(reverse=((value:js.Any)=>value.toString.reverse),
                         wrap=(value:js.Any,begin:String, end:String)=>begin+value.toString+end,
                         extract=(array:js.Array[js.Dynamic],field:String)=>
                           if (js.isUndefined(array)) array else array.map(_.selectDynamic(field))
                         )
      )
    )
    
      demo.$watch("title",(newValue:String, oldValue:String) => println("changed "+newValue))

    val demoVue=demo.asInstanceOf[DemoVue]
    
    // filters declared above inline, can be also done as below
//    Vue.filter("reverse", (value:js.Any)=>value.toString.reverse)
//    Vue.filter("wrap", (value:js.Any,begin:String, end:String)=>begin+value.toString+end)
    
   // println(js.JSON.stringify(demo.$data))
    
    //demo.$log
    
    demoVue
  }
}