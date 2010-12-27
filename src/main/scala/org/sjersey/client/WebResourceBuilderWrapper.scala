package org.sjersey.client

import javax.ws.rs.core.UriBuilder
import com.sun.jersey.api.client.{Client, WebResource}
import com.sun.jersey.api.client.filter.LoggingFilter

import RestTypes._


/**
 * WebResource#Builder wrapper factory
 *
 * @author Christopher Schmidt
 */
private[client] object WebResourceBuilderWrapper {
  /**
   * see WebResourceBuilderWrapper
   */
  def apply(builder: BuilderFuncType, settings: RestCallSettings, path: String = "") =
    new WebResourceBuilderWrapper(builder, settings, path)
}


/**
 * WebResource#Builder wrapper with settings and path that is added to basePath from settings
 * and to provide ClassManifest functionality to omit these annoying .class Java stuff
 *
 * @param builder function for be applied on every REST method call
 * @param settings the settings for <code>every rest {}</code> block
 * @param path path to be applied
 *
 * @author Christopher Schmidt
 */
class WebResourceBuilderWrapper(builder: BuilderFuncType, settings: RestCallSettings, path: String = "") {

  private var absPath = false

   /**
   * ! sets the flag for absolute path usage
   */
  def unary_!  = {
    absPath = true
    this
  }

  /**
   * PUT method call. For PUT methods without returning an object T has to be Unit
   */
  def PUT[T: ClassManifest](requestEntity: AnyRef): T = {
    val m = implicitly[ClassManifest[T]]

    if (m.erasure.isInstanceOf[Class[Unit]])
      builder(path, settings, absPath).put(requestEntity.asInstanceOf[Object]).asInstanceOf[T]
    else
      builder(path, settings, absPath).put(m.erasure.asInstanceOf[Class[T]], requestEntity)
  }


  /**
   * GET method call
   */
  def GET[T: ClassManifest]: T = {
    val m = implicitly[ClassManifest[T]]
    builder(path, settings, absPath).get(m.erasure.asInstanceOf[Class[T]])
  }

  /**
   * DELETE method call
   */
  def DELETE[T: ClassManifest]: T = {
    val m = implicitly[ClassManifest[T]]
    builder(path, settings, absPath).delete(m.erasure.asInstanceOf[Class[T]])
  }

  /**
   * POST method call
   */
  def POST[T: ClassManifest](requestEntity: AnyRef): T = {
    val m = implicitly[ClassManifest[T]]
    builder(path, settings, absPath).post(m.erasure.asInstanceOf[Class[T]], requestEntity)
  }

  /**
   * methods to allow the <= "operator" to attach Request Entities
   */
  def POST[T: ClassManifest]: POSTHelper[T] = new POSTHelper[T](this)

  def PUT[T: ClassManifest]: PUTHelper[T] = new PUTHelper[T](this)

  def PUT: PUTHelper[Unit] = new PUTHelper[Unit](this)

  /**
   *  POST helper to allow the use of the <= "operator"
   */
  class POSTHelper[T: ClassManifest](w: WebResourceBuilderWrapper) {
    def <=(ar: AnyRef) = w.POST[T](ar)
  }


  /**
   * PUT helper to allow the use of the <= "operator"
   */
  class PUTHelper[T: ClassManifest](w: WebResourceBuilderWrapper) {
    def <=(ar: AnyRef) = w.PUT[T](ar)
  }

}


/**
 * case class to store all settings while in a <code>rest  { } </code> loop
 *
 * @author Christopher Schmidt
 */
case class RestCallSettings(basePath: String, header: List[(String, String)])
