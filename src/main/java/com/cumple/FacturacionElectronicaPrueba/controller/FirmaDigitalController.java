/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.controller;


import com.cumple.FacturacionElectronicaPrueba.modules.firma.FirmaDigitalUtils;
import com.cumple.FacturacionElectronicaPrueba.modules.xades.XadesFirma;
import ec.com.virtualsami.keystore.PassStoreKS;
import ec.com.virtualsami.validacion.ValidarModulo11;
import ec.com.virtualsami.validacion.ValidarXML;
import ec.com.virtualsami.xades_firma.InternObjectToSign;
import ec.com.virtualsami.xades_firma.XAdESASignature;
import ec.com.virtualsami.xades_firma.XAdESBESSignature;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/firmaDig")
@CrossOrigin("*")
public class FirmaDigitalController {

    @Autowired
    private XadesFirma xadesFirma;
    @Autowired
    private FirmaDigitalUtils firmaDigitalUtils;


    XAdESBESSignature xAdESBESSignature;
    XAdESASignature xAdESASignature;
    InternObjectToSign internObjectToSign;
    ValidarModulo11 validarModulo11;
    ValidarXML validarXML;

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

    @PostMapping(value = "/descragarFirmado256",produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> getXml2(@RequestBody String xml){
        try {
            String xmlFirmado=firmaDigitalUtils.firmarXades(xml);

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

    @GetMapping("/listar-alias")
    public List<String> listarAlias() {
        return firmaDigitalUtils.listarAlias();
    }

    @GetMapping("/listar-certificados")
    public List<String> listarCertificados() {
        List<X509Certificate> certificados = firmaDigitalUtils.listarCertificados();
        return certificados.stream()
                .map(Certificate::toString)
                .collect(Collectors.toList());
    }

    @PostMapping("/firm")
    public String firmar(@RequestBody String x) throws Exception {
        String firma= "C:\\Users\\lchumi\\Documents\\Certificadosp12\\FIRMA_PALACIOS_CORDERO_CORSINO_EDUARDO-IMPORTADORA_CUMPLEANOS.p12";
        String clave="1234";

        Path firman=Path.of("C:\\Users\\lchumi\\Documents\\Certificadosp12\\FIRMA_IMPORTADORA_CUMPLEANOS.p12");
        String base64Xml= Base64.getEncoder().encodeToString(firma.getBytes());
        String clave64=Base64.getEncoder().encodeToString(clave.getBytes());

        xAdESBESSignature= new XAdESBESSignature(x,base64Xml,clave64);
        return getStringFromDocument(xAdESBESSignature.firmarDocumento());
    }

    public static String getStringFromDocument(Document document) {
        try {
            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException var6) {
            TransformerException ex = var6;
            ex.printStackTrace();
            return null;
        }
    }



}
