/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.controller;


import com.cumple.FacturacionElectronicaPrueba.modules.firma.FirmaDigitalUtils;
import com.cumple.FacturacionElectronicaPrueba.modules.xades.XadesFirma;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/firmaDig")
@CrossOrigin("*")
public class FirmaDigitalController {

    @Autowired
    private XadesFirma xadesFirma;
    @Autowired
    private FirmaDigitalUtils firmaDigitalUtils;

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


}
