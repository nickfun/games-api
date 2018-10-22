package gs.nick

import akka.actor.ActorSystem
import gs.nick.server.AkkaHttpImplicits._
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import gs.nick.server.games.GamesResource
import gs.nick.server.systems.SystemsResource

import scala.concurrent.ExecutionContext

// Server definition
object WebServer extends HttpApp {
  implicit val restActorSystem: ActorSystem = ActorSystem(name="games-api")

  implicit val executionContext: ExecutionContext = restActorSystem.dispatcher
  // Counterpart which materializes streams for the REST endpoints
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(restActorSystem).withSupervisionStrategy { ex =>
      println("akka supervisor got an error, will now restart", ex)
      Supervision.restart
    }
  )

  val myHandler: ApiHandler = new ApiHandler()

  override def routes: Route =
    GamesResource.routes(myHandler) ~ SystemsResource.routes(myHandler)
}

object App {
  def main(args: Array[String]) = {
  	val port = 8080
    println(s"\nSERVER WILL BIND $port")
    WebServer.startServer("localhost", port)
  }
}
