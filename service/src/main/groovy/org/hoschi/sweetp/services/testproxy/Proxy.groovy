package org.hoschi.sweetp.services.testproxy

import groovy.util.logging.Log4j
import groovyx.net.http.RESTClient
import org.hoschi.sweetp.services.base.tcp.groovy.TcpService

/**
 * Service to call other services. Show that this is easily possible in groovy.
 *
 * @author Stefan Gojan
 */
@Log4j
class Proxy extends TcpService {
	static void main(String[] args) throws Exception {
		def port = System.getenv('PORT')
		assert port, "Environment variable 'PORT' is not set, bye"
		assert port.isInteger(), "Environment variable 'PORT' is not an integer, bye"

		Proxy own = new Proxy()
		own.connect('localhost', new Integer(port))
		own.listen()
	}

	@Override
	List getConfig(Map params) {
		[
				['/tests/service/testproxy/call': [
						method: 'call',
						params: [
								url: 'url',
								origin: 'one',
								config: 'projectConfig'
						],
						description: [
								summary: 'This is just a test for the main server you can call another service by its url, given as "origin".',
								example: 'sweetp -Porigin="tests/service/java/sayhello" tests service testproxy call'
						]
				]]
		]
	}

	/**
	 * Call another service.
	 *
	 * @param params with service to call
	 * @return what other service returns
	 */
	Object call(Map params) {
		assert params.origin
		assert params.url
		assert params.config

		// use http is ok here, it should be a call from the same host so
		// insecure communication is ok.
		String url = params.url
		log.debug "calling url was: $url"

		String path = "services/$params.config.name/$params.origin"
		log.debug "calling path $path"

		RESTClient server = new RESTClient(url)
		def resp = server.get(
				path: path,
				headers: [Accept: 'application/json']
		)

		assert resp.status == 200
		resp.data.service
	}

}
