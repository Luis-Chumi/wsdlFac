//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.3.0 
// Visite <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2023.10.30 a las 03:40:17 PM ECT 
//


package com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para autorizacionComprobanteResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="autorizacionComprobanteResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RespuestaAutorizacionComprobante" type="{http://ec.gob.sri.ws.autorizacion}respuestaComprobante" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "autorizacionComprobanteResponse", propOrder = {
    "respuestaAutorizacionComprobante"
})
public class AutorizacionComprobanteResponse {

    @XmlElement(name = "RespuestaAutorizacionComprobante")
    protected RespuestaComprobante respuestaAutorizacionComprobante;

    /**
     * Obtiene el valor de la propiedad respuestaAutorizacionComprobante.
     * 
     * @return
     *     possible object is
     *     {@link RespuestaComprobante }
     *     
     */
    public RespuestaComprobante getRespuestaAutorizacionComprobante() {
        return respuestaAutorizacionComprobante;
    }

    /**
     * Define el valor de la propiedad respuestaAutorizacionComprobante.
     * 
     * @param value
     *     allowed object is
     *     {@link RespuestaComprobante }
     *     
     */
    public void setRespuestaAutorizacionComprobante(RespuestaComprobante value) {
        this.respuestaAutorizacionComprobante = value;
    }

}
