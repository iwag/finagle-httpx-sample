package com.github.iwag

import java.util.concurrent.atomic.AtomicBoolean

import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class MaintenanceFactory[Req, Res](responseInMaintenance: Res, path: String) {
  val isMaintenance: AtomicBoolean = new AtomicBoolean(false)

  lazy val filter = new MaintenanceFilter(isMaintenance, responseInMaintenance)

  lazy val admin = new ModifingService(isMaintenance, path)
}

class ModifingService(isMaintenance: AtomicBoolean, path: String) extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = Future.value{
    request.method match {
      case Method.Post =>
        val v = request.getContentString().equalsIgnoreCase("true")
        isMaintenance.set(v)
        val res = Response()
        res.contentString = "OK"
        res
      case _ => Response(Status.BadRequest)
    }
  }
}

class MaintenanceFilter[Req, Res](isMaintenance: AtomicBoolean, responseInMaintenance: Res)
  extends SimpleFilter[Req, Res]{

  override def apply(request: Req, service: Service[Req, Res]): Future[Res] = {
    if (isMaintenance.get()) {
      Future.value(responseInMaintenance)
    } else {
      service(request)
    }
  }


}
