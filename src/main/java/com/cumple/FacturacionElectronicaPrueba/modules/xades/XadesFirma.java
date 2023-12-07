/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.modules.xades;

import org.bouncycastle.jcajce.provider.digest.SHA1;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XadesFirma {

    private static final Logger log= LoggerFactory.getLogger(XadesFirma.class);
    private static final String EXPONENT="AQAB";
    private static final String XMLNS="xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:etsi=\"http://uri.etsi.org/01903/v1.3.2#\"";

    public String firmarXades(String xml) throws Exception{
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        String password="1234";

        FileInputStream fis = null;
        String certificateX509_der_hash=null;
        String issuerName=null;
        BigInteger serialNumber=null;
        String certificadoBase64=null;
        String modulusBase64=null;
        Key key=null;


        try{
            fis= new FileInputStream(rutaCertificado);
            KeyStore keyStore=KeyStore.getInstance("PKCS12");
            keyStore.load(fis,password.toCharArray());
            String alias=keyStore.aliases().nextElement();
            key=keyStore.getKey(alias,password.toCharArray());

            X509Certificate certificate =(X509Certificate) keyStore.getCertificate(alias);
            //Obtener el certificado en formato DER
            byte[] certificateX509_der= certificate.getEncoded();
            // hash SHA-1
            MessageDigest sha1Digest= MessageDigest.getInstance("SHA-1");
            byte[] sha1Bytes= sha1Digest.digest(certificateX509_der);
            //conversion a base64
            certificateX509_der_hash = new String(Base64.encode(sha1Bytes), StandardCharsets.UTF_8);
            //Obtener emisor (Issuer)
            Principal issuerPrincipal= certificate.getIssuerDN();
            issuerName= issuerPrincipal.getName();
            //Obtener el numero de serie (SerialNumber)
            serialNumber=certificate.getSerialNumber();
            //Certificado en formato Base64
            certificadoBase64= new String(Base64.encode(certificate.getEncoded()),StandardCharsets.UTF_8);
            //Modulo de la clave pública
            RSAPublicKey publicKey=(RSAPublicKey) certificate.getPublicKey();

            modulusBase64 = new String(Base64.encode(publicKey.getModulus().toByteArray()),StandardCharsets.UTF_8);
        }catch (Exception e){
            log.error("Error: \n {}", e.getMessage());
            return null;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        String signatureId=obtenerAleatorio();
        String certificateId=obtenerAleatorio();
        String referenceId=obtenerAleatorio();
        String signatureValue=obtenerAleatorio();
        String signedProperties=obtenerAleatorio();
        String signedPropertiesId=obtenerAleatorio();
        String objectId=obtenerAleatorio();
        String signatureInfo=obtenerAleatorio();

        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        Date fecha=new Date();
        String fechaFormateada=dateFormat.format(fecha);

        /*TODO <SignedProperties></SignedProperties>*/
        StringBuilder SignedProperties= new StringBuilder();

        SignedProperties.append("<etsi:SignedProperties Id=\"Signature").append(signatureId).append("-SignedProperties").append(signedProperties).append("\">");
        SignedProperties.append("<etsi:SignedSignatureProperties>");
        SignedProperties.append("<etsi:SigningTime>");
        SignedProperties.append(fechaFormateada);
        SignedProperties.append("</etsi:SigningTime>");
        SignedProperties.append("<etsi:SigningCertificate>");
        SignedProperties.append("<etsi:Cert>");
        SignedProperties.append("<etsi:CertDigest>");
        SignedProperties.append("<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">");
        SignedProperties.append("</ds:DigestMethod>");
        SignedProperties.append("<ds:DigestValue>");
        SignedProperties.append(certificateX509_der_hash);
        SignedProperties.append("</ds:DigestValue>");
        SignedProperties.append("</etsi:CertDigest>");
        SignedProperties.append("<etsi:IssuerSerial>");
        SignedProperties.append("<ds:X509IssuerName>");
        SignedProperties.append(issuerName);
        SignedProperties.append("</ds:X509IssuerName>");
        SignedProperties.append("<ds:X509SerialNumber>");
        SignedProperties.append(serialNumber);
        SignedProperties.append("</ds:X509SerialNumber>");
        SignedProperties.append("</etsi:IssuerSerial>");
        SignedProperties.append("</etsi:Cert>");
        SignedProperties.append("</etsi:SigningCertificate>");
        SignedProperties.append("</etsi:SignedSignatureProperties>");
        SignedProperties.append("<etsi:SignedDataObjectProperties>");
        SignedProperties.append("<etsi:DataObjectFormat ObjectReference=\"#Reference-ID-").append(referenceId).append("\">");
        SignedProperties.append("<etsi:Description>");
        SignedProperties.append("contenido comprobante");
        SignedProperties.append("</etsi:Description>");
        SignedProperties.append("<etsi:MimeType>");
        SignedProperties.append("text/xml");
        SignedProperties.append("</etsi:MimeType>");
        SignedProperties.append("</etsi:DataObjectFormat>");
        SignedProperties.append("</etsi:SignedDataObjectProperties>");
        SignedProperties.append("</etsi:SignedProperties>");

        String signedPropertiesString=SignedProperties.toString();
        signedPropertiesString=signedPropertiesString.replace("<etsi:SignedProperties","<etsi:SignedProperties "+XMLNS);
        //System.out.println(signedPropertiesString);
        String sha1SignedProperties=base64Encode(signedPropertiesString);

        String x509Certificate=formatearBase64(certificadoBase64);
        String modulus=formatearBase64(modulusBase64);
        /*TODO <ds:KeyInfo> </ds:Keyinfo>*/
        StringBuilder KeyInfo= new StringBuilder();

        KeyInfo.append("<ds:KeyInfo Id=\"Certificate").append(certificateId).append("\">");
        KeyInfo.append("\n<ds:X509Data>");
        KeyInfo.append("\n<ds:X509Certificate>\n");
        KeyInfo.append(x509Certificate);
        KeyInfo.append("</ds:X509Certificate>");
        KeyInfo.append("\n</ds:X509Data>");
        KeyInfo.append("\n<ds:KeyValue>");
        KeyInfo.append("\n<ds:RSAKeyValue>");
        KeyInfo.append("\n<ds:Modulus>\n");
        KeyInfo.append(modulus);
        KeyInfo.append("</ds:Modulus>");
        KeyInfo.append("\n<ds:Exponent>");
        KeyInfo.append(EXPONENT);
        KeyInfo.append("</ds:Exponent>");
        KeyInfo.append("\n</ds:RSAKeyValue>");
        KeyInfo.append("\n</ds:KeyValue>");
        KeyInfo.append("\n</ds:KeyInfo>");

        String keyInfoString=KeyInfo.toString();
        keyInfoString=keyInfoString.replace("<ds:KeyInfo","<ds:KeyInfo "+XMLNS);
        //System.out.println(keyInfoString);
        String sha1KeyInfo=base64Encode(keyInfoString);


        //<?xml version="1.0" encoding="UTF-8"?>
        String fac=xml;
        fac=fac.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
        String sha1Factura=base64Encode(fac);

        /*TODO <ds:SignedInfo*/
        StringBuilder SignedInfo= new StringBuilder();

        SignedInfo.append("<ds:SignedInfo Id=\"Signature-SignedInfo").append(signatureInfo).append("\">");
        SignedInfo.append("\n<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">");
        SignedInfo.append("</ds:CanonicalizationMethod>");
        SignedInfo.append("\n<ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\">");
        SignedInfo.append("</ds:SignatureMethod>");
        SignedInfo.append("\n<ds:Reference Id=\"SignedPropertiesID").append(signedPropertiesId).append("\"").append(" Type=\"http://uri.etsi.org/01903#SignedProperties\" URI=\"#Signature").append(signatureId).append("-SignedProperties").append(signedProperties).append("\">");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">");
        SignedInfo.append("</ds:DigestMethod>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1SignedProperties);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n<ds:Reference URI=\"#Certificate").append(certificateId).append("\">");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">");
        SignedInfo.append("</ds:DigestMethod>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1KeyInfo);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n<ds:Reference Id=\"Reference-ID-").append(referenceId).append("\" URI=\"#comprobante\">");
        SignedInfo.append("\n<ds:Transforms>");
        SignedInfo.append("\n<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\">");
        SignedInfo.append("</ds:Transform>");
        SignedInfo.append("\n</ds:Transforms>");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">");
        SignedInfo.append("</ds:DigestMethod>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1Factura);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n</ds:SignedInfo>");


        String signedInfoString=SignedInfo.toString();
        signedInfoString=signedInfoString.replace("<ds:SignedInfo ","<ds:SignedInfo "+XMLNS);
        //System.out.println(signedInfoString);
        String firmaSignedInfo=generarFirma(key,signedInfoString);
        firmaSignedInfo=formatearBase64(firmaSignedInfo);

        /*TODO firma digital*/
        StringBuilder xades_bes= new StringBuilder();
        xades_bes.append("<ds:Signature ").append(XMLNS).append(" Id=\"Signature").append(signatureId).append("\">");
        xades_bes.append("\n").append(SignedInfo);
        xades_bes.append("\n<ds:SignatureValue Id=\"SignatureValue").append(signatureValue).append("\">\n");
        xades_bes.append(firmaSignedInfo);
        xades_bes.append("</ds:SignatureValue>");
        xades_bes.append("\n").append(KeyInfo);
        xades_bes.append("\n<ds:Object Id=\"Signature").append(signatureId).append("-Object").append(objectId).append("\">");
        xades_bes.append("<etsi:QualifyingProperties Target=\"#Signature").append(signatureId).append("\">");
        xades_bes.append(SignedProperties);
        xades_bes.append("</etsi:QualifyingProperties>");
        xades_bes.append("</ds:Object>");
        xades_bes.append("</ds:Signature>");

        String xadesString=xades_bes.toString();
        //System.out.println(xadesString);

        String facturaFirmada=xml;
        facturaFirmada=facturaFirmada.replace("</factura>",xadesString+"</factura>");
        System.out.println(facturaFirmada);
        return facturaFirmada;
    }


    private static final Set<String> numerosGenerados=new HashSet<>();
    private static String obtenerAleatorio(){
        Random random = new Random();
        String randomNumber;
        do {
            int numero= random.nextInt(999000)+990;
            randomNumber=String.valueOf(numero);
        }while (!numerosGenerados.add(randomNumber));
        return randomNumber;
    }

    private static String base64Encode(String input){
        try {
            MessageDigest md=new SHA1.Digest();
            byte[] digest=md.digest(input.getBytes(StandardCharsets.UTF_8));

            return new String(Base64.encode(digest),StandardCharsets.UTF_8);
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error: \n {}",e.getMessage());
            return null;
        }
    }

    private static String generarFirma(Key key,String data) throws Exception{
        PrivateKey privateKey =(PrivateKey) key;

        Signature signature=Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);

        signature.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes= signature.sign();

        return new String(Base64.encode(signatureBytes),StandardCharsets.UTF_8);
    }

    protected String formatearBase64(String base64Encoded){
        StringBuilder formattedOutput = new StringBuilder();
        for (int i = 0; i < base64Encoded.length(); i += 76) {
            int end = Math.min(i + 76, base64Encoded.length());
            formattedOutput.append(base64Encoded, i, end).append("\n");
        }
        return formattedOutput.toString();
    }
}
