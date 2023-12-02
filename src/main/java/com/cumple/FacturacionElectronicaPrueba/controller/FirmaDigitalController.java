/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.controller;


import com.cumple.FacturacionElectronicaPrueba.modules.firma.FirmaDigitalService;
import com.cumple.FacturacionElectronicaPrueba.modules.xades.XmlFirma;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

@RestController
@RequestMapping("/firmaDig")
@CrossOrigin("*")
public class FirmaDigitalController {

    @Autowired
    private FirmaDigitalService firmaDigitalService;

    @Autowired
    private XmlFirma firma;

    @PostMapping("/firmar")
    public byte[] firmarDocumento(@RequestBody String contenido,@RequestParam String password){
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        return firmaDigitalService.firmarDocumento(contenido, rutaCertificado, password);
    }

    @PostMapping("/verificar")
    public boolean verificarFirma(@RequestParam String documento,@RequestParam byte[] firma){
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        return firmaDigitalService.verificarFirma(documento, firma, rutaCertificado);
    }



    @PostMapping("/ver el certificado")
    public String sign(@RequestBody String xml) throws  Exception{
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        String password="1234";
        String alias2= "null";

        KeyStore keyStore=KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(rutaCertificado),password.toCharArray());

        String alias=keyStore.aliases().nextElement();
        PrivateKey privateKey=(PrivateKey) keyStore.getKey(alias,password.toCharArray());
        X509Certificate certificate =(X509Certificate) keyStore.getCertificate(alias);
        System.out.println("--------------------------------------------");
        System.out.println(":-> \n +{"+alias+"}");
        System.out.println("--------------------------------------------");
        System.out.println(privateKey);
        System.out.println("--------------------------------------------");
        System.out.println(certificate);


        Enumeration<String> aliases= keyStore.aliases();
        if (aliases.hasMoreElements()){
            alias2=aliases.nextElement();
        }
        System.out.println("alias: \n {}"+alias2);

       return "Datos";
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

    @PostMapping("/convertirStringToBytes")
    public byte[] convertir(@RequestBody String xmlFirmado){
        return  Base64.getEncoder().encode(xmlFirmado.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/convertirStringToBytesBase64")
    public byte[] convertir2(@RequestBody String xml){
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/convertirStringTobase64-Bytes")
    public byte[] convertir3(@RequestBody String xml){
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        byte[] base64Encoded= Base64.getEncoder().encode(xmlBytes);
        String baseEncode=Base64.getEncoder().encodeToString(xmlBytes);
        System.out.println("Base64 codificado en bytes: " + new String(base64Encoded, StandardCharsets.UTF_8));
        System.out.println("------------------------------------------------------------------");
        System.out.println(baseEncode);
        return base64Encoded;
    }

}
