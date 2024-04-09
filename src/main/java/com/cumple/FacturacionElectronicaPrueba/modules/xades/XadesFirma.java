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
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XadesFirma {

    private static final Logger log= LoggerFactory.getLogger(XadesFirma.class);
    private static final String XMLNS="xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:etsi=\"http://uri.etsi.org/01903/v1.3.2#\"";

    /**
     * NUMEROS GENERADOS ID
     */
    private static final Set<String> numerosGenerados=new HashSet<>();
    private static final String CERTIFICATE_NUMBER=obtenerAleatorio();
    private static final String SIGNATURE_NUMBER=obtenerAleatorio();
    private static final String SIGNED_PROPERTIES_NUMBER=obtenerAleatorio();
    private static final String SIGNED_INFO_NUMBER=obtenerAleatorio();
    private static final String SIGNED_PROPERTIESID_NUMBER=obtenerAleatorio();
    private static final String REFERENCE_ID_NUMBER=obtenerAleatorio();
    private static final String SIGNATURE_VALUE_NUMBER=obtenerAleatorio();
    private static final String OBJEVT_NUMBER=obtenerAleatorio();

    private static String obtenerAleatorio(){
        Random random = new Random();
        String randomNumber;
        do {
            int numero= random.nextInt(999000)+990;
            randomNumber=String.valueOf(numero);
        }while (!numerosGenerados.add(randomNumber));
        return randomNumber;
    }

    public String firmarXades(String xml) throws Exception{
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\PalaciosNaranjoCorcinoEduardo.p12";
        String password="1234";

        //<?xml version="1.0" encoding="UTF-8"?>
        String sha1Factura=sha1Base64(xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",""));

        String certificateX509_der_hash=null;
        String issuerName=null;
        BigInteger serialNumber=null;
        String certificadoBase64=null;
        String modulusBase64=null;
        String exponent=null;
        Key key=null;

        try (FileInputStream fis = new FileInputStream(rutaCertificado)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, password.toCharArray());
            String alias = keyStore.aliases().nextElement();
            key = keyStore.getKey(alias, password.toCharArray());
            System.out.println(alias);

            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            certificateX509_der_hash = certificadoHash(certificate);
            PublicKey publicKey = certificate.getPublicKey();

            if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                exponent = java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
                byte[] mByte= rsaPublicKey.getModulus().toByteArray();
            } else {
                return "LA CLAVE NO ES DE TIPO RSA";
            }
            if (key instanceof RSAPrivateKey rsaPrivateKey) {
                byte[] mByte= rsaPrivateKey.getModulus().toByteArray();
                modulusBase64 = java.util.Base64.getEncoder().encodeToString(removerExtension(mByte));
            } else {
                return "LA CLAVE NO ES DE TIPO RSA";
            }


            // Obtener emisor (Issuer)
            Principal issuerPrincipal = certificate.getIssuerX500Principal();
            issuerName = issuerPrincipal.getName();
            // Obtener el número de serie (SerialNumber)
            serialNumber = certificate.getSerialNumber();
            // Certificado en formato Base64
            certificadoBase64= Base64.toBase64String(certificate.getEncoded());

        } catch (Exception e) {
            log.error("Error: \n {}", e.getMessage());
            return null;
        }

        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        Date fecha=new Date();
        String fechaFormateada=dateFormat.format(fecha);

        /*TODO <SignedProperties></SignedProperties>*/
        String signedPropertiesString = String.format(
                "<etsi:SignedProperties Id=\"Signature%s-SignedProperties%s\">"
                        + "<etsi:SignedSignatureProperties>"
                        + "<etsi:SigningTime>%s</etsi:SigningTime>"
                        + "<etsi:SigningCertificate>"
                        + "<etsi:Cert>"
                        + "<etsi:CertDigest>"
                        + "<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></ds:DigestMethod>"
                        + "<ds:DigestValue>%s</ds:DigestValue>"
                        + "</etsi:CertDigest>"
                        + "<etsi:IssuerSerial>"
                        + "<ds:X509IssuerName>%s</ds:X509IssuerName>"
                        + "<ds:X509SerialNumber>%s</ds:X509SerialNumber>"
                        + "</etsi:IssuerSerial>"
                        + "</etsi:Cert>"
                        + "</etsi:SigningCertificate>"
                        + "</etsi:SignedSignatureProperties>"
                        + "<etsi:SignedDataObjectProperties>"
                        + "<etsi:DataObjectFormat ObjectReference=\"#Reference-ID-%s\">"
                        + "<etsi:Description>%s</etsi:Description>"
                        + "<etsi:MimeType>%s</etsi:MimeType>"
                        + "</etsi:DataObjectFormat>"
                        + "</etsi:SignedDataObjectProperties>"
                        + "</etsi:SignedProperties>",
                SIGNATURE_NUMBER, SIGNED_PROPERTIES_NUMBER, fechaFormateada,
                certificateX509_der_hash, issuerName, serialNumber, REFERENCE_ID_NUMBER,
                "contenido comprobante", "text/xml");



        String signedPropertiesStringHash = signedPropertiesString.replace("<etsi:SignedProperties", "<etsi:SignedProperties " + XMLNS);
        String sha1SignedProperties=sha1Base64(signedPropertiesStringHash);

        String x509Certificate=formatearBase64(certificadoBase64);
        String modulus=formatearBase64(modulusBase64);
        /*TODO <ds:KeyInfo> </ds:Keyinfo>*/
        String keyInfoString = String.format(
                "<ds:KeyInfo Id=\"Certificate%s\">"
                        + "\n<ds:X509Data>"
                        + "\n<ds:X509Certificate>%s</ds:X509Certificate>"
                        + "\n</ds:X509Data>"
                        + "\n<ds:KeyValue>"
                        + "\n<ds:RSAKeyValue>"
                        + "\n<ds:Modulus>%s</ds:Modulus>"
                        + "\n<ds:Exponent>%s</ds:Exponent>"
                        + "\n</ds:RSAKeyValue>"
                        + "\n</ds:KeyValue>"
                        + "\n</ds:KeyInfo>",
                CERTIFICATE_NUMBER, x509Certificate, modulus, exponent);

// Realizar el reemplazo
        String keyInfoStringHash = keyInfoString.replace("<ds:KeyInfo", "<ds:KeyInfo " + XMLNS);
        /*System.out.println(keyInfoString);*/
        String sha1KeyInfo=sha1Base64(keyInfoStringHash);


        /*TODO <ds:SignedInfo*/
        String signedInfoString = String.format(
                "<ds:SignedInfo Id=\"Signature-SignedInfo%s\">"
                        + "\n<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">"
                        + "</ds:CanonicalizationMethod>"
                        + "\n<ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\">"
                        + "</ds:SignatureMethod>"
                        + "\n<ds:Reference Id=\"SignedPropertiesID%s\" Type=\"http://uri.etsi.org/01903#SignedProperties\" URI=\"#Signature%s-SignedProperties%s\">"
                        + "\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">"
                        + "</ds:DigestMethod>"
                        + "\n<ds:DigestValue>%s</ds:DigestValue>"
                        + "\n</ds:Reference>"
                        + "\n<ds:Reference URI=\"#Certificate%s\">"
                        + "\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">"
                        + "</ds:DigestMethod>"
                        + "\n<ds:DigestValue>%s</ds:DigestValue>"
                        + "\n</ds:Reference>"
                        + "\n<ds:Reference Id=\"Reference-ID-%s\" URI=\"#comprobante\">"
                        + "\n<ds:Transforms>"
                        + "\n<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\">"
                        + "</ds:Transform>"
                        + "\n</ds:Transforms>"
                        + "\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">"
                        + "</ds:DigestMethod>"
                        + "\n<ds:DigestValue>%s</ds:DigestValue>"
                        + "\n</ds:Reference>"
                        + "\n</ds:SignedInfo>",
                SIGNED_INFO_NUMBER, SIGNED_PROPERTIESID_NUMBER, SIGNATURE_NUMBER, SIGNED_PROPERTIES_NUMBER,
                sha1SignedProperties, CERTIFICATE_NUMBER, sha1KeyInfo, REFERENCE_ID_NUMBER, sha1Factura);

// Realizar el reemplazo
        String signedInfoStringHash = signedInfoString.replace("<ds:SignedInfo ", "<ds:SignedInfo " + XMLNS);
        //System.out.println(signedInfoString);
        String firmaSignedInfo=generarFirma(key,signedInfoStringHash);
        System.out.println(firmaSignedInfo);

        /*TODO firma digital*/
        String xadesString = String.format(
                "<ds:Signature %s Id=\"Signature%s\">"
                        + "\n%s"
                        + "\n<ds:SignatureValue Id=\"SignatureValue%s\">\n%s</ds:SignatureValue>"
                        + "\n%s"
                        + "\n<ds:Object Id=\"Signature%s-Object%s\">"
                        + "<etsi:QualifyingProperties Target=\"#Signature%s\">"
                        + "\n%s"
                        + "\n</etsi:QualifyingProperties>"
                        + "\n</ds:Object>"
                        + "\n</ds:Signature>",
                XMLNS, SIGNATURE_NUMBER, signedInfoString, SIGNATURE_VALUE_NUMBER, firmaSignedInfo,
                keyInfoString, SIGNATURE_NUMBER, OBJEVT_NUMBER, SIGNATURE_NUMBER, signedPropertiesString);

// Realizar el reemplazo
        xml = xml.replace("</factura>", xadesString + "</factura>");

// Imprimir la factura firmada (opcional)
// System.out.println(xml);

        return xml;

    }

    protected static String certificadoHash(X509Certificate certificate) throws CertificateEncodingException {
        MessageDigest md=new SHA1.Digest();
        byte[] der= certificate.getEncoded();
        byte[] hashBytes = md.digest(der);
        return Base64.toBase64String(hashBytes);
    }

    private static String base64Encode(String input){
        try {
            MessageDigest md=new SHA1.Digest();
            byte[] digest=md.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.toBase64String(digest);
        }catch (Exception e){
            log.error("Error: \n {}",e.getMessage());
            return null;
        }
    }

    protected static String sha1Base64(String data){
        try {
            MessageDigest digest =new SHA1.Digest();
            byte[] bytes=data.getBytes(StandardCharsets.UTF_8);
            digest.update(bytes);
            byte[] sha1Has= digest.digest();

            return Base64.toBase64String(sha1Has);
        }catch (Exception e){
            log.error("Error: \n {}",e.getMessage());
            return null;
        }
    }

    private static String generarFirma(Key key,String data) throws Exception{
        PrivateKey privateKey =(PrivateKey) key;
        Signature signature=Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        System.out.println(privateKey+"\n "+signature);
        byte[] signatureBytes= signature.sign();
        return new String(Base64.encode(signatureBytes),"UTF-8");
    }

    protected String formatearBase64(String base64Encoded){
        StringBuilder formattedOutput = new StringBuilder();
        for (int i = 0; i < base64Encoded.length(); i += 76) {
            int end = Math.min(i + 76, base64Encoded.length());
            formattedOutput.append(base64Encoded, i, end).append("\n");
        }
        return formattedOutput.toString();
    }

    protected static  byte[] removerExtension(byte[] bytes){
        byte[] bytes1= new byte[bytes.length-1];
        int j=0;
        for (int i = 1; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] & 0xFF);
            bytes1[j]=bytes[i];
            j++;
        }
        return bytes1;
    }

}
