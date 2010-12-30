package org.sjersey.client

import RestTypes._
import java.net.URI
import com.sun.jersey.api.client.{ClientResponse, WebResource}

/**
 * basic trait of rest access methods and functions
 *
 * @author Christopher Schmidt
 */
trait Rest extends IRestExceptionWrapper {

  /**
   * override REST Exception Handler still default here
   */
  override def restExceptionHandler: ExceptionHandlerType = {
    t => throw t
  }

  /**
   *  the WebResource instance
   */
  protected val webResource: WebResource

  /**
   * to create a new WebResource from an absolute Path
   */
  protected def getWebResourceFromAbsURI(absPath: String): WebResource

  /**
   *  Media Type String
   */
  protected val mediaType: Option[String] = None

  /**
   * function, if applied with path and settings, returns WebResource#Builder
   */
  private def builder: BuilderFuncType = {
    (path, settings, absPath) =>
      val requestBuilder =
        if (absPath)
          getWebResourceFromAbsURI(path).getRequestBuilder
        else
          webResource.path(settings.basePath).path(path).getRequestBuilder

      mediaType.foreach(x => requestBuilder.accept(x).`type`(x))

      settings.header.foreach(x => requestBuilder.header(x._1, x._2))

      requestBuilder
  }

  /**
   * implicit conversion to support "path".method(...) stuff
   * @param path path to add to this specific REST call
   */
  implicit def restPathStringToWRM(path: String)(implicit settings: RestCallSettings): WebResourceBuilderWrapper =
    WebResourceBuilderWrapper(restExceptionHandler, builder, settings, path)

  /**
   * main method enclosing the REST calls
   * @param header header name value field to add to HTTP header
   * @param basePath path to add to all subsequent rest calls
   */
  def rest[A](header: List[(String, String)] = Nil, basePath: String = "")(f: (RestCallSettings) => A): A = {
    f(new RestCallSettings(basePath, header))
  }

  /**
   * needed to allow omitting of parenthesis
   */
  def rest[A](f: (RestCallSettings) => A): A = rest()(f)

  /**
   *  these are here to support direct GET calls (without "path"... before)
   *
   * @TODO make this work ;-) Seems to be double work for now
   */
  /*def GET[T](implicit t: ClassManifest[T], settings: RestCallSettings): T =
    WebResourceBuilderWrapper(builder, settings).GET

  def DELETE[T](implicit t: ClassManifest[T], settings: RestCallSettings): T =
    WebResourceBuilderWrapper(builder, settings).DELETE

  def POST[T](requestEntity: AnyRef)(implicit t: ClassManifest[T], settings: RestCallSettings): T =
    WebResourceBuilderWrapper(builder, settings).POST(requestEntity)

  def PUT[T](requestEntity: AnyRef)(implicit t: ClassManifest[T], settings: RestCallSettings): T =
    WebResourceBuilderWrapper(builder, settings).PUT(requestEntity)

  */

  /**
   * helper methods
   */

  def getLastStringFromPath(s: String) = {
    // @TODO implement this example
  }

  /**
   * implicit call of ClientResponse.getLocation:URI
   * @param ClientResponse f.e. returned from a POST call
   * @returned URI URI of the newly created entity
   */
  implicit def clientResponseToLocationURI(cr:ClientResponse):URI = cr.getLocation
}