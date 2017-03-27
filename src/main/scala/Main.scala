package com.github.iwag

import java.net.InetSocketAddress

import com.twitter.finagle.http.{Response, Request, HttpMuxer, Status, Method, RequestBuilder}
import com.twitter.finagle.tracing.Trace
import com.twitter.server.TwitterServer
import com.twitter.finagle.{SimpleFilter, Http, Service}
import com.twitter.util.{Future, Await}
import com.twitter.logging.Logger

class HTTPServiceImpl(log:Logger) extends Service[Request, Response] {
  val client = Http.newService("requestb.in:80")
  override def apply(request: Request): Future[Response] = Future.value{
    val res = Response()
    request.method match {
      case Method.Get => res.setContentString("wai!wai!")
      case Method.Post =>
        val newReq = RequestBuilder().setHeader("Content-Type","application/json").url("http://requestb.in/").buildPost(request.content)
        log.info(newReq.toString())
        val f = client(newReq) onSuccess { res =>
          log.info(res.toString)
        }
        Await.all(f)
      case _ => res.status = Status.BadRequest
    }
    res
  }
}

object Main extends TwitterServer {
  val httpAddr = flag("http", new InetSocketAddress(40080), "HTTP bind address")

  def main() {
    val httpMux = new HttpMuxer().withHandler("/", new HTTPServiceImpl(log))

    val httpServer = Http.serve(httpAddr(), httpMux)

    onExit {
      adminHttpServer.close()
      httpServer.close()
    }

    log.info("start admin:" +adminHttpServer.boundAddress)
    log.info("start http:"+ httpServer.boundAddress)
    Await.all(adminHttpServer, httpServer)
  }
}
