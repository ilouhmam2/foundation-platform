package fr.francetv.foundation.soapclient.interceptor;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

/**
 * Minimal JAX-WS service interface used in interceptor integration tests.
 */
@WebService
interface TestSoapService {

    @WebMethod
    String ping();
}
