package eu.fakod.sjerseyclient

import com.sun.jersey.api.client.{ClientResponse, WebResource}
import java.net.URI

/**
 *
 * @author Christopher Schmidt
 *         Date: 28.09.11
 *         Time: 06:29
 */
trait RestImplicits {
  /**
   * implicit conversion to support "path".method(...) stuff
   * @param path path to add to this specific REST call
   */
  implicit def restPathStringToWRM(path: String)(implicit settings: RestCallContext): WebResourceBuilderWrapper =
    WebResourceBuilderWrapper(settings, path)

  /**
   * implicit conversion to convert a ClientResponse to an Entity
   * if none T is given java.lang.Object is used
   * @param cr ClientResponse instance
   */
  implicit def clientResponseToEntity(cr: ClientResponse) = new {
    def toEntity[T: Manifest] = cr.getEntity(manifest[T].erasure.asInstanceOf[Class[T]])
  }

  /**
   * converts to RichClientResponse
   *
   */
  implicit def toRichClientResponse(cr: ClientResponse) = new RichClientResponse(cr)
}

/**
 *
 */
trait Rest extends RestImplicits with IRestExceptionWrapper {

  /**
   * override REST Exception Handler still default here
   */
  override def restExceptionHandler: ExceptionHandlerType = {
    t => throw t
  }

  /**
   * the WebResource instance
   */
  protected val webResource: WebResource

  /**
   * to create a new WebResource from an absolute Path
   */
  protected def getWebResourceFromAbsURI(absPath: String): WebResource

  /**
   * multiple Media Types as List of Strings
   */
  protected val mediaType: List[String] = Nil

  /**
   * function, if applied with path and settings, returns WebResource#Builder
   */
  private[sjerseyclient] def builder: BuilderFuncType = {
    (path, settings, absPath) =>
      var wr =
        if (absPath)
          getWebResourceFromAbsURI(path)
        else
          webResource.path(settings.basePath).path(path)

      settings.query.foreach {
        case (k, v) => wr = wr.queryParam(k, v)
      }

      val requestBuilder = wr.getRequestBuilder

      /**
       * Media Type
       */
      val typeList = settings.cType match {
        case None => mediaType
        case Some(t) => t
      }
      typeList.foreach(x => requestBuilder.`type`(x))

      val acceptList = settings.cAccept match {
        case None => mediaType
        case Some(t) => t
      }
      acceptList.foreach(x => requestBuilder.accept(x))

      settings.header.foreach(x => requestBuilder.header(x._1, x._2))

      requestBuilder
  }

  /**
   * converts List[String] parameter that can be null to Option[List[String]]
   */
  private implicit def listToOptionList(l: List[String]): Option[List[String]] =
    l match {
      case null => None
      case _: List[String] => Some(l)
    }

  /**
   * main method enclosing the REST calls
   * @param header header name value field to add to HTTP header
   * @param basePath path to add to all subsequent rest calls
   * @param query allows to attach query parameter
   * @param cType allows to overwrite the global MediaType setting for Content Type
   * @param cAccept allows to overwrite the global MediaType setting for Accept
   */
  def rest[A](header: List[(String, String)] = Nil, basePath: String = "", query: List[(String, String)] = Nil,
              cType: List[String] = null, cAccept: List[String] = null)(f: (RestCallContext) => A): A = {
    f(RestCallContext(this, basePath, header, query, cType, cAccept))
  }

  /**
   * needed to allow omitting of parenthesis
   */
  def rest[A](f: (RestCallContext) => A): A = rest()(f)

  /**
   * implicit call of ClientResponse.getLocation:URI
   * @param cr ClientResponse f.e. returned from a POST call
   * @return URI URI of the newly created entity
   */
  implicit def clientResponseToLocationURI(cr: ClientResponse): URI = cr.getLocation

  /**
   * adding toLocation:String to class ClientResponse
   * @param cr ClientResponse f.e. returned from a POST call
   * @return String URI of the newly created entity as String
   */
  implicit def clientResponseToLocationString(cr: ClientResponse) = new {
    def toLocation = cr.getLocation.toString
  }
}