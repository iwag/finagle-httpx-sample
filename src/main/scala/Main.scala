package com.github.iwag

import java.net.InetSocketAddress

import com.twitter.finagle.http.{Response, Request, HttpMuxer, Status, Method, RequestBuilder}
import com.twitter.finagle.tracing.Trace
import com.twitter.server.TwitterServer
import com.twitter.finagle.{SimpleFilter, Http, Service}
import com.twitter.util.{Future, Await}
import com.twitter.logging.Logger

class RequestIdFilter extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    request.headerMap.add("request-id", "012345")
    service(request) map { res =>
      res.headerMap.add("request-id", "012345")
      res
    }
  }
}

class RequestIdFilterEx extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    MyContext("test").asCurrent {
      service(request) map { res =>

        MyContext.current.map { c => // こんな感じでとれる
          res.headerMap.add("mycontext-id", c.id)
        }
        res.headerMap.add("trace-id", Trace.id.traceId.toString())
        res.headerMap.add("parent-id", Trace.id.parentId.toString())
        res.headerMap.add("span-id", Trace.id.spanId.toString())
        res
      }
    }
  }
}


class HTTPServiceImpl(log:Logger) extends Service[Request, Response] {
  val client = Http.newService("requestb.in:80")
  override def apply(request: Request): Future[Response] = Future.value{
    val res = Response()
    res.headerMap.add("request-id-http", Trace.id.traceId.toString())
    MyContext.current.map { c=>
      res.headerMap.add("mycontext-id-http", c.id) // 別のリクエストでも取れる！
    }
    request.method match {
      case Method.Get => res.setContentString("wai!wai!")
      case Method.Post =>
        val newReq = RequestBuilder().setHeader("Content-Type","application/json").url("http://requestb.in/16hj4hp1").buildPost(request.content)
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
    val requestIdFilter = new RequestIdFilterEx
    val http = new HTTPServiceImpl(log)
    val httpMux = new HttpMuxer().withHandler("/", requestIdFilter andThen http)

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