/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.controller;

import com.cumple.FacturacionElectronicaPrueba.client.SoapClient;
import com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion.AutorizacionComprobanteResponse;
import com.cumple.FacturacionElectronicaPrueba.wsdl.autorizacion.RespuestaComprobante;
import com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion.RespuestaSolicitud;
import com.cumple.FacturacionElectronicaPrueba.wsdl.recepcion.ValidarComprobanteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/soapRecepcion")
@CrossOrigin("*")
public class SoapController {

    @Autowired
    @Qualifier("soapClientRecepcion")
    private SoapClient soapClient;

    @Autowired
    @Qualifier("soapClientAutorizacion")
    private SoapClient soapClientAutorizacion;

    @PostMapping("validarComprobante")
    public RespuestaSolicitud validarComprobante(@RequestBody byte[] xml){
        try{
            ValidarComprobanteResponse response= soapClient.getValidarComprobante(xml);
            return response.getRespuestaRecepcionComprobante();
        }catch (Exception e){
            return null;
        }
    }

    @PostMapping("validarComprobanteString")
    public RespuestaSolicitud validarComprobante2(@RequestBody String xml) {
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
        try {
            ValidarComprobanteResponse response = soapClient.getValidarComprobante(xmlBytes);
            if (response != null && response.getRespuestaRecepcionComprobante() != null) {
                return response.getRespuestaRecepcionComprobante();
            } else {
                System.out.println("La respuesta del servicio es nula o no contiene información válida.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error al llamar al servicio: " + e.getMessage());
            return null;
        }
    }

    @PostMapping("convertirBytes")
    public byte[] convertir(@RequestBody String xml) {
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/autorizacion")
    public ResponseEntity<AutorizacionComprobanteResponse> verificarAutorizacion(@RequestBody String clave) {
        try {
            // Validar la entrada (por ejemplo, clave no nula o vacía)
            if (clave == null || clave.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            AutorizacionComprobanteResponse response = soapClientAutorizacion.getAutorizacion(clave);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Manejo de excepciones
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
