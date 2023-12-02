/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.config;

import com.cumple.FacturacionElectronicaPrueba.client.SoapClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class SoapConfig {

    @Bean
    @Primary
    public Jaxb2Marshaller marshallerRecepcion(){
        Jaxb2Marshaller marshaller= new Jaxb2Marshaller();
        marshaller.setContextPath("com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion");
        return marshaller;
    }

    @Bean
    public  Jaxb2Marshaller marshallerAutorizacion(){
        Jaxb2Marshaller marsaller= new Jaxb2Marshaller();
        marsaller.setContextPath("com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion");
        return marsaller;
    }

    @Bean
    public SoapClient soapClientRecepcion(@Qualifier("marshallerRecepcion") Jaxb2Marshaller marshaller){
        SoapClient soapClient= new SoapClient(marshaller);
        soapClient.setDefaultUri("https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline");
        soapClient.setMarshaller(marshaller);
        soapClient.setUnmarshaller(marshaller);

        return soapClient;
    }

    @Bean
    public SoapClient soapClientAutorizacion(@Qualifier("marshallerAutorizacion") Jaxb2Marshaller marshaller){
        SoapClient soapClient=new SoapClient(marshaller);
        soapClient.setDefaultUri("https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline");
        soapClient.setMarshaller(marshaller);
        soapClient.setUnmarshaller(marshaller);

        return soapClient;
    }

}
