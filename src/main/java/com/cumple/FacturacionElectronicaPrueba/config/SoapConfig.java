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
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion", "com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion");
        return marshaller;
    }

    @Bean
    public SoapClient soapClientRecepcion(@Qualifier("marshaller") Jaxb2Marshaller marshaller) {
        return createSoapClient(marshaller, "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline");
    }

    @Bean
    public SoapClient soapClientAutorizacion(@Qualifier("marshaller") Jaxb2Marshaller marshaller) {
        return createSoapClient(marshaller, "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline");
    }

    private SoapClient createSoapClient(Jaxb2Marshaller marshaller, String defaultUri) {
        SoapClient soapClient = new SoapClient(marshaller);
        soapClient.setDefaultUri(defaultUri);
        soapClient.setMarshaller(marshaller);
        soapClient.setUnmarshaller(marshaller);
        return soapClient;
    }

}