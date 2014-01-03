package example

import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("my-system")

  startServer(interface = "localhost", port = 8099) {
    pathPrefix("green") {
      pathEnd {
        // parameters directive with single extraction if fine...
        parameters('p1.?){ p1 =>
          complete { s"called /green with params p1 -> $p1" }
        }
      } ~
      // path directive only containing Segment extraction is fine...
      path(Segment) { id1 =>
        complete { s"called /green/$id1" }
      } ~
      // path directive with tuple extraction starting with extraction is fine...
      path(Segment / Segment) { (id1, id2) =>
        complete { s"called /green/$id1/$id2" }
      }
    } ~
    pathPrefix("red") {
      pathEnd {
        // parameters directive with >1 extracted value turns tuple values red...
        parameters('p1.?, 'p2.?){ (p1, p2) =>
          complete { s"called /red with params p1 -> $p1, p2 -> $p2" }
        }
      } ~
      // path directive with >1 extracted value starting with a literal turns the tuple values red...
      path("firstthis" / Segment / "andthen" / Segment) { (id1, id2) =>
        complete { s"called /red/firstthis/$id1/andthen/$id2" }
      }
    }
  }
}
