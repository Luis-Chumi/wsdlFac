/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.modules.xades;

import org.bouncycastle.jcajce.provider.digest.SHA1;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XadesFirma {

    private static final Logger log= LoggerFactory.getLogger(XadesFirma.class);
    private static final String XMLNS="xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:etsi=\"http://uri.etsi.org/01903/v1.3.2#\"";

    private static final Set<String> numerosGenerados=new HashSet<>();

    /**
     * NUMEROS GENERADOS ID
     */
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
        String rutaCertificado="C:\\Users\\Luis\\Documents\\Workspace\\Certificados\\firma.p12";
        String password="1234";

        String certificateX509_der_hash=null;
        String issuerName=null;
        BigInteger serialNumber=null;
        String certificadoBase64=null;
        String modulusBase64=null;
        String exponent=null;
        PrivateKey key=null;


        try (FileInputStream fis = new FileInputStream(rutaCertificado)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, password.toCharArray());
            String alias = keyStore.aliases().nextElement();
            key = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            certificateX509_der_hash = certificadoHash(certificate);

            PublicKey publicKey = certificate.getPublicKey();

            if (publicKey instanceof  RSAPublicKey){
                RSAPublicKey rsaPublicKey =(RSAPublicKey) publicKey;

                exponent = java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
                modulusBase64 = java.util.Base64.getEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray());


            } else {
                return "LA CLAVE NO ES DE TIPO RSA";
            }

            // Obtener emisor (Issuer)
            Principal issuerPrincipal = certificate.getIssuerDN();
            issuerName = issuerPrincipal.getName();
            // Obtener el número de serie (SerialNumber)
            serialNumber = certificate.getSerialNumber();
            // Certificado en formato Base64
            byte[] cert=convertP12Base64(key,certificate);
            certificadoBase64 = new String(cert);

        } catch (Exception e) {
            log.error("Error: \n {}", e.getMessage());
            return null;
        }

        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        Date fecha=new Date();
        String fechaFormateada=dateFormat.format(fecha);

        /*TODO <SignedProperties></SignedProperties>*/
        StringBuilder SignedProperties= new StringBuilder();

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
        SignedProperties.append("</etsi:DataObjectFormat>");
        SignedProperties.append("</etsi:SignedDataObjectProperties>");
        SignedProperties.append("</etsi:SignedProperties>");

        String signedPropertiesString=SignedProperties.toString();
        signedPropertiesString=signedPropertiesString.replace("<etsi:SignedProperties","<etsi:SignedProperties "+XMLNS);
        //System.out.println(signedPropertiesString);
        String sha1SignedProperties=sha1Base64(signedPropertiesString);

        String x509Certificate=formatearBase64(certificadoBase64);
        String modulus=formatearBase64(modulusBase64);
        /*TODO <ds:KeyInfo> </ds:Keyinfo>*/
        StringBuilder KeyInfo= new StringBuilder();

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

        String keyInfoString=KeyInfo.toString();
        keyInfoString=keyInfoString.replace("<ds:KeyInfo","<ds:KeyInfo "+XMLNS);
        //System.out.println(keyInfoString);
        String sha1KeyInfo=sha1Base64(keyInfoString);


        //<?xml version="1.0" encoding="UTF-8"?>
        String fac=xml;
        fac=fac.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
        String sha1Factura=sha1Base64(fac);

        /*TODO <ds:SignedInfo*/
        StringBuilder SignedInfo= new StringBuilder();

        SignedInfo.append("<ds:SignedInfo Id=\"Signature-SignedInfo").append(SIGNED_INFO_NUMBER).append("\">");
        SignedInfo.append("\n<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\">");
        SignedInfo.append("</ds:CanonicalizationMethod>");
        SignedInfo.append("\n<ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\">");
        SignedInfo.append("</ds:SignatureMethod>");
        SignedInfo.append("\n<ds:Reference Id=\"SignedPropertiesID").append(SIGNED_PROPERTIESID_NUMBER).append("\"").append(" Type=\"http://uri.etsi.org/01903#SignedProperties\" URI=\"#Signature").append(SIGNATURE_NUMBER).append("-SignedProperties").append(SIGNED_PROPERTIES_NUMBER).append("\">");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">");
        SignedInfo.append("</ds:DigestMethod>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1SignedProperties);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n<ds:Reference URI=\"#Certificate").append(CERTIFICATE_NUMBER).append("\">");
        SignedInfo.append("\n<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\">");
        SignedInfo.append("</ds:DigestMethod>");
        SignedInfo.append("\n<ds:DigestValue>");
        SignedInfo.append(sha1KeyInfo);
        SignedInfo.append("</ds:DigestValue>");
        SignedInfo.append("\n</ds:Reference>");
        SignedInfo.append("\n<ds:Reference Id=\"Reference-ID-").append(REFERENCE_ID_NUMBER).append("\" URI=\"#comprobante\">");
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

        String xadesString=xades_bes.toString();
        //System.out.println(xadesString);

        String facturaFirmada=xml;
        facturaFirmada = facturaFirmada.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        facturaFirmada = facturaFirmada.replace("</factura>", xadesString + "</factura>");
        //System.out.println(facturaFirmada);
        return facturaFirmada;
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
            e.printStackTrace();
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
            e.printStackTrace();
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

    protected static String converToPem(Object object) throws Exception{
        StringWriter stringWriter = new StringWriter();
        try(PEMWriter pemWriter= new PEMWriter(stringWriter)) {
            if (object instanceof X509Certificate){
                pemWriter.writeObject(new JcaMiscPEMGenerator((X509Certificate)object));
            }else if (object instanceof  PrivateKey){
                pemWriter.writeObject(new JcaMiscPEMGenerator((PrivateKey) object));
            }else {
                throw  new IllegalArgumentException("Tipo de objeto no adminitido: "+ object.getClass().getName());
            }
        }
        return stringWriter.toString();
    }

    protected static byte[] convertP12Base64(PrivateKey key, X509Certificate certificate) throws Exception {
        String pemPrivateKey=converToPem(key);
        String pemCertificado=converToPem(certificate);

        String pemCombinado=pemCertificado;

        return Base64.encode(pemCombinado.getBytes());
    }

}
