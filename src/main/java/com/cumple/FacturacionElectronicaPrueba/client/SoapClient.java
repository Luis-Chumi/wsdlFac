/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.client;

import com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion.AutorizacionComprobante;
import com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion.AutorizacionComprobanteResponse;
import com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion.ValidarComprobante;
import com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion.ValidarComprobanteResponse;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;

@Component
public class SoapClient extends WebServiceGatewaySupport {

    private final Jaxb2Marshaller marshaller;

    public SoapClient(Jaxb2Marshaller marshaller){
        this.marshaller=marshaller;
    }

    public ValidarComprobanteResponse getValidarComprobante(byte[] xml){

            ValidarComprobante validarComprobante= new ValidarComprobante();
            validarComprobante.setXml(xml);

            SoapActionCallback soapActionCallback=new SoapActionCallback("");

            JAXBElement<ValidarComprobanteResponse> jaxbElement=(JAXBElement<ValidarComprobanteResponse>) getWebServiceTemplate().marshalSendAndReceive("https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline",validarComprobante,soapActionCallback);

            ValidarComprobanteResponse reponse= jaxbElement.getValue();

            return jaxbElement.getValue();
    }


    public AutorizacionComprobanteResponse getAutorizacion(String claveAcceso){

        AutorizacionComprobante autorizacionComprobante= new AutorizacionComprobante();
        autorizacionComprobante.setClaveAccesoComprobante(claveAcceso);

        SoapActionCallback soapActionCallback=new SoapActionCallback("");


       JAXBElement<AutorizacionComprobanteResponse> response=(JAXBElement<AutorizacionComprobanteResponse>) getWebServiceTemplate().marshalSendAndReceive("https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline",autorizacionComprobante,soapActionCallback);
       AutorizacionComprobanteResponse re= response.getValue();

        return response.getValue();
    }
}
