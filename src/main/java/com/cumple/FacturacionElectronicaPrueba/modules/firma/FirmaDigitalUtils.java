/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.modules.firma;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Arrays;

public class FirmaDigitalUtils {

    private final static Logger LOG= LoggerFactory.getLogger(FirmaDigitalUtils.class);

    /**
     * Se agrega el proveedor de seguridad al inicio de la ejecución
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     *Metodo para crear la firma digital
     * @param contenido
     * @param rutaCertificado
     * @param password
     * @return retorna el documento firmado
     */
    public static byte[] firmarDocumento(String contenido,String rutaCertificado, String password){
        try {
            /*carga Almacenes de claves desde el archivo PKCS12*/
            KeyStore keyStore= KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(rutaCertificado),password.toCharArray());
            LOG.info(new FileInputStream(rutaCertificado).toString(),password.toCharArray());
            System.out.println(rutaCertificado+ Arrays.toString(password.toCharArray()));

            /*obtener la clave y certificado de almacen*/
            String alias=keyStore.aliases().nextElement();
            PrivateKey privateKey= (PrivateKey) keyStore.getKey(alias,password.toCharArray());
            Certificate certificate=keyStore.getCertificate(alias);
            System.out.println(alias);

            /*Inicia la firma digital con SHA256 Y RSA esto genera su hash o codico unico de seguridad genera seguridad */
            Signature firma= Signature.getInstance("SHA256withRSA", "BC");
            firma.initSign(privateKey);
            System.out.println(privateKey);
            System.out.println(firma);


            /*Actualiza la firma con el contenido del documento */
            firma.update(contenido.getBytes());

            LOG.info("actualizado .... ",firma);

            /*Firmar el documento y devolver el resultado con un arreglo de bytes*/
            System.out.println(Arrays.toString(firma.sign()));
            return firma.sign();

        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage());
            return null;
        }
    }

    /**
     * Metodo para validar la firma del documento
     * @param contenido
     * @param firma
     * @param rutaCertificado
     * @return me devuelve el estado del documento si es válido(true - false)
     */
    public static boolean verificarFirma(String contenido,byte[] firma,String rutaCertificado){
        try {
            String password="1234";
            /*Cargar la clave desde el archivo*/
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(rutaCertificado),password.toCharArray());

            /*Obtener el certificado del almacén */
            String alias = keyStore.aliases().nextElement();
            Certificate certificate= keyStore.getCertificate(alias);

            /*Iniciar la verificación de firma con SHA256 Y RSA*/
            Signature verificarFirma=Signature.getInstance("SHA1withRSA", "BC");
            verificarFirma.initVerify(certificate.getPublicKey());

            /*Actualiza la verificacion con el contenido del documento */
            verificarFirma.update(contenido.getBytes());

            /*verifica si la firma digital es valida y devuelve el resultado */
            return verificarFirma.verify(firma);

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
