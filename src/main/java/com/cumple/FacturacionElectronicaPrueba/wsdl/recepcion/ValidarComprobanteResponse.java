//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.3.0 
// Visite <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2023.10.30 a las 03:40:16 PM ECT 
//


package com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para validarComprobanteResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="validarComprobanteResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RespuestaRecepcionComprobante" type="{http://ec.gob.sri.ws.recepcion}respuestaSolicitud" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "validarComprobanteResponse", propOrder = {
    "respuestaRecepcionComprobante"
})
public class ValidarComprobanteResponse {

    @XmlElement(name = "RespuestaRecepcionComprobante")
    protected RespuestaSolicitud respuestaRecepcionComprobante;

    /**
     * Obtiene el valor de la propiedad respuestaRecepcionComprobante.
     * 
     * @return
     *     possible object is
     *     {@link RespuestaSolicitud }
     *     
     */
    public RespuestaSolicitud getRespuestaRecepcionComprobante() {
        return respuestaRecepcionComprobante;
    }

    /**
     * Define el valor de la propiedad respuestaRecepcionComprobante.
     * 
     * @param value
     *     allowed object is
     *     {@link RespuestaSolicitud }
     *     
     */
    public void setRespuestaRecepcionComprobante(RespuestaSolicitud value) {
        this.respuestaRecepcionComprobante = value;
    }

}
