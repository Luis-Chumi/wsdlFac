/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.modules.firma;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FirmaDigitalUtils {

    private final static Logger console= LoggerFactory.getLogger(FirmaDigitalUtils.class);

    private static final String XMLNS="xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:etsi=\"http://uri.etsi.org/01903/v1.3.2#\"";
    /**
     * Numeros Generados para ids
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

    public String firmarXades(String xml) throws Exception {
        String rutaCertificado = "C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\ImportadoraCumpleaños.p12";
        String password = "1234";

        String certificateX509_der_hash = null;
        String issuerName = null;
        BigInteger serialNumber = null;
        String certificadoBase64 = null;
        String modulusBase64 = null;
        String exponent = null;
        PrivateKey key = null;

        KeyStore keyStore;
        try (FileInputStream fis = new FileInputStream(rutaCertificado)) {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, password.toCharArray());
            String alias = keyStore.aliases().nextElement();
            key = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            certificateX509_der_hash = certificadoHash(certificate);
            PublicKey publicKey = certificate.getPublicKey();
            System.out.println(certificate.getIssuerX500Principal());

            if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                exponent = java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
            } else {
                return "LA CLAVE NO ES DE TIPO RSA";
            }
            if (key instanceof RSAPrivateKey rsaPrivateKey) {
                modulusBase64 = java.util.Base64.getEncoder().encodeToString(rsaPrivateKey.getModulus().toByteArray());
            } else {
                return "LA CLAVE NO ES DE TIPO RSA";
            }

            // Obtener emisor (Issuer)
            Principal issuerPrincipal = certificate.getIssuerX500Principal();
            issuerName = issuerPrincipal.getName();
            // Obtener el número de serie (SerialNumber)
            serialNumber = certificate.getSerialNumber();
            // Certificado en formato Base64
            certificadoBase64 = Base64.toBase64String(certificate.getEncoded());

        } catch (Exception e) {
            console.error("Error: \n {}", e.getMessage());
            return null;
        }

        Enumeration<String> aliases = keyStore.aliases();
        while(aliases.hasMoreElements()){
            String alias = aliases.nextElement();
            System.out.println("Alias: "+ alias);

            Certificate cert= keyStore.getCertificate(alias);
            System.out.println("Certificado: "+ cert);

            Key key1 = keyStore.getKey(alias ,password.toCharArray());
            System.out.println("Clave privada "+ key1);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        Date fecha = new Date();
        String fechaFormateada = dateFormat.format(fecha);

        /*TODO <SignedProperties></SignedProperties>*/
        StringBuilder SignedProperties = new StringBuilder();

        SignedProperties.append("<etsi:SignedProperties Id=\"Signature").append(SIGNATURE_NUMBER).append("-SignedProperties").append(SIGNED_PROPERTIES_NUMBER).append("\">");
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
        SignedProperties.append("<etsi:DataObjectFormat ObjectReference=\"#Reference-ID-").append(REFERENCE_ID_NUMBER).append("\">");
        SignedProperties.append("<etsi:Description>");
        SignedProperties.append("contenido comprobante");
        SignedProperties.append("</etsi:Description>");
        SignedProperties.append("<etsi:MimeType>");
        SignedProperties.append("text/xml");
        SignedProperties.append("</etsi:MimeType>");
        SignedProperties.append("<etsi:Encoding>");
        SignedProperties.append("UTF-8");
        SignedProperties.append("</etsi:Encoding>");
        SignedProperties.append("</etsi:DataObjectFormat>");
        SignedProperties.append("</etsi:SignedDataObjectProperties>");
        SignedProperties.append("</etsi:SignedProperties>");

        String signedPropertiesString = SignedProperties.toString();
        signedPropertiesString = signedPropertiesString.replace("<etsi:SignedProperties", "<etsi:SignedProperties " + XMLNS);
        //System.out.println(signedPropertiesString);
        String sha1SignedProperties = sha1Base64(signedPropertiesString);

        String x509Certificate = formatearBase64(certificadoBase64);
        String modulus = formatearBase64(modulusBase64);
        /*TODO <ds:KeyInfo> </ds:Keyinfo>*/
        StringBuilder KeyInfo = new StringBuilder();

        KeyInfo.append("<ds:KeyInfo Id=\"Certificate").append(CERTIFICATE_NUMBER).append("\">");
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
        KeyInfo.append(exponent);
        KeyInfo.append("</ds:Exponent>");
        KeyInfo.append("\n</ds:RSAKeyValue>");
        KeyInfo.append("\n</ds:KeyValue>");
        KeyInfo.append("\n</ds:KeyInfo>");

        String keyInfoString = KeyInfo.toString();
        keyInfoString = keyInfoString.replace("<ds:KeyInfo", "<ds:KeyInfo " + XMLNS);
        //System.out.println(keyInfoString);
        String sha1KeyInfo = sha1Base64(keyInfoString);

        //<?xml version="1.0" encoding="UTF-8"?>
        String fac = xml;
        fac = fac.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        String sha1Factura = sha1Base64(fac);

        /*TODO <ds:SignedInfo*/
        StringBuilder SignedInfo = new StringBuilder();

        SignedInfo.append("<ds:SignedInfo Id=\"Signature-SignedInfo").append(SIGNED_INFO_NUMBER).append("\">");
        SignedInfo.append("\n<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>");
        SignedInfo.append("\n<ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>");
        SignedInfo.append("\n<ds:Reference Id=\"SignedPropertiesID").append(SIGNED_PROPERTIESID_NUMBER).append("\"").append(" Type=\"http://uri.etsi.org/01903#SignedProperties\" URI=\"#Signature").append(SIGNATURE_NUMBER).append("-SignedProperties").append(SIGNED_PROPERTIES_NUMBER).append("\">");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1SignedProperties);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n<ds:Reference URI=\"#Certificate").append(CERTIFICATE_NUMBER).append("\">");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1KeyInfo);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n<ds:Reference Id=\"Reference-ID-").append(REFERENCE_ID_NUMBER).append("\" URI=\"#comprobante\">");
        SignedInfo.append("\n<ds:Transforms>");
        SignedInfo.append("\n<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>");
        SignedInfo.append("\n</ds:Transforms>");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1Factura);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n</ds:SignedInfo>");

        String signedInfoString = SignedInfo.toString();
        signedInfoString = signedInfoString.replace("<ds:SignedInfo ", "<ds:SignedInfo " + XMLNS);
        //System.out.println(signedInfoString);
        String firmaSignedInfo = generarFirma(key, signedInfoString);
        firmaSignedInfo = formatearBase64(firmaSignedInfo);

        /*TODO firma digital*/
        StringBuilder xades_bes = new StringBuilder();
        xades_bes.append("<ds:Signature ").append(XMLNS).append(" Id=\"Signature").append(SIGNATURE_NUMBER).append("\">");
        xades_bes.append("\n").append(SignedInfo);
        xades_bes.append("\n<ds:SignatureValue Id=\"SignatureValue").append(SIGNATURE_VALUE_NUMBER).append("\">\n");
        xades_bes.append(firmaSignedInfo);
        xades_bes.append("</ds:SignatureValue>");
        xades_bes.append("\n").append(KeyInfo);
        xades_bes.append("\n<ds:Object Id=\"Signature").append(SIGNATURE_NUMBER).append("-Object").append(OBJEVT_NUMBER).append("\">");
        xades_bes.append("<etsi:QualifyingProperties Target=\"#Signature").append(SIGNATURE_NUMBER).append("\">");
        xades_bes.append(SignedProperties);
        xades_bes.append("</etsi:QualifyingProperties>");
        xades_bes.append("</ds:Object>");
        xades_bes.append("</ds:Signature>");

        String xadesString = xades_bes.toString();
        //System.out.println(xadesString);

        String facturaFirmada = xml;
        facturaFirmada = facturaFirmada.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        facturaFirmada = facturaFirmada.replace("</factura>", xadesString + "</factura>");
        //System.out.println(facturaFirmada);
        return facturaFirmada;
    }



    private static String obtenerAleatorio(){
        Random random = new Random();
        String randomNumber;
        do {
            int numero= random.nextInt(999000)+990;
            randomNumber=String.valueOf(numero);
        }while (!numerosGenerados.add(randomNumber));
        return randomNumber;
    }
    protected static String certificadoHash(X509Certificate certificate) throws CertificateEncodingException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] der = certificate.getEncoded();
            byte[] hashBytes = md.digest(der);
            return Base64.toBase64String(hashBytes);
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar la excepción adecuadamente según tus necesidades
            return null;
        }
    }

    private static String base64Encode(String input){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.toBase64String(digest);
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar la excepción adecuadamente según tus necesidades
            return null;
        }
    }

    protected static String sha1Base64(String data){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            digest.update(bytes);
            byte[] sha1Hash = digest.digest();

            return Base64.toBase64String(sha1Hash);
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar la excepción adecuadamente según tus necesidades
            return null;
        }
    }

    private static String generarFirma(Key key,String data) throws Exception{
        PrivateKey privateKey =(PrivateKey) key;

        Signature signature=Signature.getInstance("sha1withRSA");
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
