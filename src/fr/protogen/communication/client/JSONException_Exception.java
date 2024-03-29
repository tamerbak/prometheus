
package fr.protogen.communication.client;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "JSONException", targetNamespace = "http://serviceweb.apiCom.web.phoenix.fr/")
public class JSONException_Exception
    extends java.lang.Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private JSONException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public JSONException_Exception(String message, JSONException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public JSONException_Exception(String message, JSONException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: client.JSONException
     */
    public JSONException getFaultInfo() {
        return faultInfo;
    }

}
