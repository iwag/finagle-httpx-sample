package com.github.iwag

import com.twitter.finagle.context.Contexts
import com.twitter.io.Buf
import com.twitter.util.{Throw, Return, Try}

/*
 * reference: com.twitter.finagle.thrift.ClientId
 * https://github.com/twitter/finagle/blob/fc321f804a22a695ec419902505c8509ffbd594d/finagle-core/src/main/scala/com/twitter/finagle/thrift/ClientId.scala
 */

case class MyContext(id: String) {
  def asCurrent[T](f: => T): T = MyContext.let(this)(f)
}

object MyContext {

  val ctx = new Contexts.broadcast.Key[MyContext]("com.github.iwag.mycontext") {

    override def marshal(value: MyContext): Buf = {
      value match {
        case MyContext(id) => Buf.Utf8(id)
        case _ => Buf.Empty
      }
    }

    override def tryUnmarshal(buf: Buf): Try[MyContext] =
      buf match {
        case b if buf.isEmpty => Throw(new IllegalArgumentException("illegal"))
        case Buf.Utf8(id) => Return(MyContext(id))
        case invalid => Throw(new IllegalArgumentException("illegal"))
      }
  }

  def current = Contexts.broadcast.get(ctx)

  private[iwag] def let[R](myContext: MyContext)(f: => R): R =
    Contexts.broadcast.let(ctx, myContext)(f)
}
