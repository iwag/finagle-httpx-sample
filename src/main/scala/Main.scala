package com.github.iwag

import java.net.InetSocketAddress

import com.twitter.finagle.httpx.{Response, Request, HttpMuxer, Version, Status, Method}
import com.twitter.server.TwitterServer
import com.twitter.finagle.{Httpx, Service}
import com.twitter.util.{Future, Await}

class HTTPServiceImpl extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = Future.value{
    val res = Response()
    request.method match {
      case Method.Get => res.setContentString("wai!wai!")
      case _ => res.status = Status.BadRequest
    }
    res
  }
}

object Main extends TwitterServer {
  val httpAddr = flag("http", new InetSocketAddress(40080), "HTTP bind address")

  def main() {
    val httpMux = new HttpMuxer().withHandler("/", new HTTPServiceImpl())

    val httpServer = Httpx.serve(httpAddr(), httpMux)

    onExit {
      adminHttpServer.close()
      httpServer.close()
    }

    log.info("start admin:" +adminHttpServer.boundAddress)
    log.info("start http:"+ httpServer.boundAddress)
    Await.all(adminHttpServer,httpServer)
  }
}