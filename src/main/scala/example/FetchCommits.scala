package example

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.Thenable.Implicits._
import scala.util.{Failure, Success}
import org.scalajs.dom
import com.felstar.scalajs.vue._

/** Fetching data: the latest commits to vuejs/core from the GitHub API, using `dom.fetch` and Scala
  * `Future`s. A watcher on `branch` (with `immediate`) reloads when the branch changes.
  */
object FetchCommits {

  private implicit val ec: ExecutionContext = JSExecutionContext.queue

  @js.native
  trait Vm extends ComponentPublicInstance {
    var branch: String = js.native
    var commits: js.Array[js.Object] = js.native
    var loading: Boolean = js.native
    var error: String = js.native
  }

  /** Only the newest request may update the page, so quick branch switches can't show stale data. */
  private var latestRequest = 0

  private def present(value: js.Dynamic): Boolean = value != null && !js.isUndefined(value)

  private def toView(c: js.Dynamic): js.Object = {
    val title: String = c.commit.message.asInstanceOf[String].linesIterator.nextOption().getOrElse("")
    val avatar: String = if (present(c.author)) c.author.avatar_url.asInstanceOf[String] else ""
    literal(
      sha = c.sha,
      url = c.html_url,
      title = title,
      author = c.commit.author.name,
      date = new js.Date(c.commit.author.date.asInstanceOf[String]).toLocaleString(),
      avatar = avatar
    )
  }

  private def load(vm: Vm): Unit = {
    latestRequest += 1
    val request = latestRequest
    vm.loading = true
    vm.error = ""
    val url = s"https://api.github.com/repos/vuejs/core/commits?per_page=5&sha=${vm.branch}"

    dom.fetch(url).toFuture
      .flatMap { response =>
        if (response.ok) response.json().toFuture
        else Future.failed(new Exception(s"GitHub answered ${response.status} ${response.statusText}".trim))
      }
      .map(json => json.asInstanceOf[js.Array[js.Dynamic]].map(toView))
      .onComplete { result =>
        if (request == latestRequest) {
          result match {
            case Success(commits) => vm.commits = commits
            case Failure(e)       => vm.error = e.getMessage
          }
          vm.loading = false
        }
      }
  }

  private val template = """
    <v-card border flat rounded="lg">
      <div class="d-flex align-center flex-wrap ga-3 pa-4">
        <v-btn-toggle v-model="branch" mandatory density="compact" variant="outlined" divided color="primary">
          <v-btn value="main" prepend-icon="mdi-source-branch">main</v-btn>
          <v-btn value="minor" prepend-icon="mdi-source-branch">minor</v-btn>
        </v-btn-toggle>
        <span class="text-body-medium text-medium-emphasis">
          Latest commits to <a href="https://github.com/vuejs/core" target="_blank" rel="noopener" class="text-primary">vuejs/core</a>
        </span>
        <v-spacer></v-spacer>
        <v-btn icon="mdi-refresh" variant="text" :loading="loading" aria-label="Reload" @click="reload"></v-btn>
      </div>
      <v-divider></v-divider>

      <v-alert v-if="error" type="error" variant="tonal" class="ma-4" title="Couldn't load commits" :text="error"></v-alert>
      <v-skeleton-loader v-else-if="loading && !commits.length" type="list-item-avatar-two-line@5"></v-skeleton-loader>
      <v-list v-else lines="two" :class="{ 'opacity-50': loading }">
        <v-list-item
          v-for="c in commits"
          :key="c.sha"
          :href="c.url"
          target="_blank"
          :title="c.title"
          :subtitle="c.author + ' · ' + c.date">
          <template #prepend>
            <v-avatar color="surface-variant" :image="c.avatar || undefined" :icon="c.avatar ? undefined : 'mdi-account'"></v-avatar>
          </template>
          <template #append>
            <code class="text-body-small text-medium-emphasis">{{ c.sha.slice(0, 7) }}</code>
          </template>
        </v-list-item>
      </v-list>
    </v-card>
  """

  val component: js.Object = literal(
    template = template,
    data = (() => literal(branch = "main", commits = js.Array[js.Object](), loading = false, error = "")): js.Function0[js.Object],
    methods = literal(
      reload = ((vm: Vm) => load(vm)): js.ThisFunction0[Vm, Unit]
    ),
    watch = literal(
      branch = literal(immediate = true, handler = ((vm: Vm) => load(vm)): js.ThisFunction0[Vm, Unit])
    )
  )
}
