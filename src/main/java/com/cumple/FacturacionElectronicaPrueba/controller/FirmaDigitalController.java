/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.controller;


import com.cumple.FacturacionElectronicaPrueba.modules.firma.FirmaDigitalService;
import com.cumple.FacturacionElectronicaPrueba.modules.xades.XadesFirma;
import com.cumple.FacturacionElectronicaPrueba.modules.xades.XmlFirma;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/firmaDig")
@CrossOrigin("*")
public class FirmaDigitalController {

    @Autowired
    private FirmaDigitalService firmaDigitalService;

    @Autowired
    private XmlFirma firma;

    @Autowired
    private XadesFirma xadesFirma;

    @PostMapping("/firmar")
    public byte[] firmarDocumento(@RequestBody String contenido,@RequestParam String password){
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        return firmaDigitalService.firmarDocumento(contenido, rutaCertificado, password);
    }

    @PostMapping("/firmaXml")
    public String firmarXml(@RequestBody String xmlContent)throws  Exception {
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        String password="1234";

        KeyStore keyStore=KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(rutaCertificado),password.toCharArray());
        String alias=keyStore.aliases().nextElement();
        PrivateKey privateKey=(PrivateKey) keyStore.getKey(alias,password.toCharArray());
        X509Certificate certificate =(X509Certificate) keyStore.getCertificate(alias);
        try {
            return firma.signXmlDatos(xmlContent);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al crear la firma electronica";
        }
    }

    @PostMapping(value = "/descragarFirmado",produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> getXml(@RequestBody String xml){
        try {
         String xmlFirmado=xadesFirma.firmarXades(xml);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition","attachment; filename=\"xml-firmado.xml\"");
            return  ResponseEntity.ok().headers(headers).body(xmlFirmado);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @PostMapping("ver")
    public ResponseEntity<?> ver(@RequestBody String x) throws Exception {
    String firma=xadesFirma.firmarXades(x);
    return ResponseEntity.ok().body(firma);
    }


}
