package com.github.ssvitkov

import wvlet.log.LogSupport

import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

object Safely extends LogSupport {
  @inline def apply[A](f: => A, alt: => A, warning: String, defaultMsg: Option[String] = None): A =
    apply(f, warning).getOrElse {
      defaultMsg.foreach(m => info(m))
      alt
    }

  @inline def apply[A](f: => A, warning: String): Option[A] =
    apply(f).tap(r => if (r.isEmpty) warn(warning))

  @inline def apply[A](f: => A): Option[A] = Try(f).toOption
}
