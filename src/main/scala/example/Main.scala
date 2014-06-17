package example

import akka.actor.ActorSystem
import scala.util.control.NonFatal
import spray.httpx.unmarshalling._
import spray.routing.SimpleRoutingApp

object Main extends App with SimpleRoutingApp {

  implicit val String2SeqString = new FromStringDeserializer[Seq[String]] {
    def apply(value: String) = {
      try Right(value.split(",").toSeq)
      catch { case NonFatal(ex) => Left(MalformedContent(ex.toString, ex)) }
    }
  }

  implicit val system = ActorSystem("my-system")

  startServer(interface = "localhost", port = 8099) {
    pathPrefix("foo") {
      path("paramlist") {
        parameters("states".as[Seq[String]].?) { states =>
          complete { s"called /foo/paramlist with parameters $states" }
        }
      }
    } ~
    // examples that work
    pathPrefix("green") {
      path("example1") {
        // parameters directive with single extraction if fine...
        parameters('p1.?){ p1 =>
          complete { s"called /green/example1 with params p1 -> $p1" }
        }
      } ~
      // path directive only containing Segment extraction is fine...
      path("example2" / Segment) { id1 =>
        complete { s"called /green/example2/$id1" }
      } ~
      // path directive with >1 extracted value starting with an extracted value works...
      path(Segment / "example3" / Segment) { (id1: String, id2: String) =>
        complete { s"called /green/$id1/$id2" }
      }
      // ...also works when types are explicitly supplied
      path(Segment / "example4" / Segment) { (id1: String, id2: String) =>
        complete { s"called /green/$id1/$id2" }
      }
    } ~
    // examples that turn red
    pathPrefix("red") {
      path("example1") {
        // parameters directive with >1 extracted value turns tuple values red...
        parameters('p1.?, 'p2.?){ (p1, p2) =>
          complete { s"called /red/example1 with params p1 -> $p1, p2 -> $p2" }
        }
      } ~
      path("example2") {
        // ...and with a different error when types explicitly supplied
        parameters('p1.?, 'p2.?){ (p1: Option[String], p2: Option[String]) =>
          complete { s"called /red/example2 with params p1 -> $p1, p2 -> $p2" }
        }
      } ~
      // path directive with >1 extracted value starting with a literal turns the tuple values red...
      path("example3" / Segment / "andthen" / Segment) { (id1, id2) =>
        complete { s"called /red/example3/$id1/andthen/$id2" }
      } ~
      // ...and with a different error when types are explicitly suppled
      path("example4" / Segment / "andthen" / Segment) { (id1: String, id2: String) =>
        complete { s"called /red/example4/$id1/andthen/$id2" }
      }
    }
  }
}
