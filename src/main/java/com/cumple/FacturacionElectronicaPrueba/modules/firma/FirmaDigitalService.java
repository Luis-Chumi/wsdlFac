/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.modules.firma;

import org.springframework.stereotype.Service;

@Service
public class FirmaDigitalService {

    public byte[] firmarDocumento(String contenido,String rutaCertificado,String password){
        return FirmaDigitalUtils.firmarDocumento(contenido, rutaCertificado, password);
    }

    public boolean verificarFirma(String contenido,byte[] firma,String rutaCertificado){
        return FirmaDigitalUtils.verificarFirma(contenido, firma, rutaCertificado);
    }
}
