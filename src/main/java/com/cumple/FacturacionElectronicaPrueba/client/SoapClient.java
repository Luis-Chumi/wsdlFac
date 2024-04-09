/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.client;

import com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion.AutorizacionComprobante;
import com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion.AutorizacionComprobanteResponse;
import com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion.ValidarComprobante;
import com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion.ValidarComprobanteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;
import java.util.Arrays;

@Component
public class SoapClient extends WebServiceGatewaySupport {

    private static final Logger Consola=LoggerFactory.getLogger(SoapClient.class);
    private final WebServiceTemplate webServiceTemplate;

    public SoapClient(Jaxb2Marshaller marshaller) {
        this.webServiceTemplate = new WebServiceTemplate(marshaller);
    }

    public ValidarComprobanteResponse getValidarComprobante(byte[] xml) {
        ValidarComprobante validarComprobante = new ValidarComprobante();
        validarComprobante.setXml(xml);

        try {
            JAXBElement<?> response = (JAXBElement<?>) getWebServiceTemplate()
                    .marshalSendAndReceive(getDefaultUri(), validarComprobante, new SoapActionCallback(""));

            Object responseObject = response.getValue();

            if (responseObject instanceof ValidarComprobanteResponse){
                ValidarComprobanteResponse comprobanteResponse = (ValidarComprobanteResponse) responseObject;
                return comprobanteResponse;
            }else{
                return null;
            }
        }catch (Exception e){
            Consola.error("Error al procesar la solicitud ",e);
            return null;
        }
    }

    public AutorizacionComprobanteResponse getAutorizacion(String claveAcceso) {
        AutorizacionComprobante autorizacionComprobante = new AutorizacionComprobante();
        autorizacionComprobante.setClaveAccesoComprobante(claveAcceso);

        try {
            JAXBElement<?> response = (JAXBElement<?>) getWebServiceTemplate()
                    .marshalSendAndReceive(getDefaultUri(), autorizacionComprobante, new SoapActionCallback(""));

            Object responseObject = response.getValue();
            System.out.println(Arrays.toString(responseObject.getClass().getClasses()));


            if (responseObject instanceof AutorizacionComprobanteResponse){
                AutorizacionComprobanteResponse comprobanteResponse= (AutorizacionComprobanteResponse) responseObject;
                return comprobanteResponse;
            }else{
                return null;
            }
        }catch (Exception e){
            Consola.error("Error al procesar la solicitud ",e);
            return null;
        }
    }

}