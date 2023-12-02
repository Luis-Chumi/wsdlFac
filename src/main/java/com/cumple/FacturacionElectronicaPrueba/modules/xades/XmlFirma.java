/*
 * Copyright (c) 2023. Luis Chumi
 * Este programa es software libre: usted puede redistribuirlo y/o modificarlo bajo los términos de la Licencia Pública General GNU
 */

package com.cumple.FacturacionElectronicaPrueba.modules.xades;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XmlFirma {

    private static final Logger log= LoggerFactory.getLogger(XmlFirma.class);
    private static final String URI_ETSI="http://uri.etsi.org/01903/v1.3.2#";
    private static final String URI_DS="http://www.w3.org/2000/09/xmldsig#";
    private static final String EXPONENT="AQAB";

    public String signXmlDatos(String xmlContent) throws Exception {


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
            certificateX509_der_hash =Base64.getEncoder().encodeToString(sha1Bytes);
            //Obtener emisor (Issuer)
            Principal issuerPrincipal= certificate.getIssuerDN();
            issuerName= issuerPrincipal.getName();
            //Obtener el numero de serie (SerialNumber)
            serialNumber=certificate.getSerialNumber();
            //Certificado en formato Base64
            certificadoBase64= Base64.getEncoder().encodeToString(certificate.getEncoded());
            //Modulo de la clave pública
            RSAPublicKey publicKey=(RSAPublicKey) certificate.getPublicKey();
            System.out.println(publicKey);
            modulusBase64 = Base64.getEncoder().encodeToString(publicKey.getModulus().toByteArray());
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



        try {
            // Convertir la cadena XML a un documento DOM
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlContent)));

            // Crear una instancia de XMLSignatureFactory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            Element signatureElement= doc.createElementNS(URI_DS, "ds:Signature");
            signatureElement.setAttribute("xmlns:ds", URI_DS);
            signatureElement.setAttribute("xmlns:etsi",URI_ETSI);
            signatureElement.setAttribute("Id","Signature"+signatureId);
            //----------------------------------------------------------------------------------------------------------------------------------------------
            //carga de object para hash
            Element object=doc.createElementNS(URI_DS, "ds:Object");
            object.setAttribute("Id","Signature"+signatureId+"-"+"Object"+objectId);

            Element qualifyingProperties= doc.createElementNS(URI_ETSI,"etsi:QualifyingProperties");
            qualifyingProperties.setAttribute("Target","#Signature"+signatureId);
            object.appendChild(qualifyingProperties);

            Element signedPropertiesElement=doc.createElementNS(URI_ETSI,"etsi:SignedProperties");
            signedPropertiesElement.setAttribute("Id","Signature"+signatureId+"-SignedProperties"+signedProperties);
            qualifyingProperties.appendChild(signedPropertiesElement);

            Element signeSignatureProp=doc.createElementNS(URI_ETSI,"etsi:SignedSignatureProperties");
            signedPropertiesElement.appendChild(signeSignatureProp);

            Element signingTime=doc.createElementNS(URI_ETSI,"etsi:SigningTime");
            signingTime.setTextContent(fechaFormateada);
            signeSignatureProp.appendChild(signingTime);

            Element signinCertificate=doc.createElementNS(URI_ETSI,"etsi:SigningCertificate");
            signeSignatureProp.appendChild(signinCertificate);

            Element certElement=doc.createElementNS(URI_ETSI,"etsi:Cert");
            signinCertificate.appendChild(certElement);

            Element certDigest= doc.createElementNS(URI_ETSI,"etsi:CertDigest");
            certElement.appendChild(certDigest);

            Element cerDigestMethod= doc.createElementNS(URI_DS,"ds:DigestMethod");
            cerDigestMethod.setAttribute("Algorithm","http://www.w3.org/2000/09/xmldsig#sha1");
            certDigest.appendChild(cerDigestMethod);
            Element cerDigestValue= doc.createElementNS(URI_DS,"ds:DigestValue");
            cerDigestValue.setTextContent(certificateX509_der_hash);
            certDigest.appendChild(cerDigestValue);

            Element issuerSerial=doc.createElementNS(URI_ETSI,"etsi:IssuerSerial");
            certElement.appendChild(issuerSerial);

            Element x5009IssuerName=doc.createElementNS(URI_DS,"ds:X509IssuerName");
            x5009IssuerName.setTextContent(issuerName);
            issuerSerial.appendChild(x5009IssuerName);
            Element x509SerialNumber=doc.createElementNS(URI_DS,"ds:X509SerialNumber");
            x509SerialNumber.setTextContent(serialNumber.toString());
            issuerSerial.appendChild(x509SerialNumber);

            Element signedDataObjectProperties= doc.createElementNS(URI_ETSI,"etsi:SignedDataObjectProperties");
            signedPropertiesElement.appendChild(signedDataObjectProperties);

            Element dataObjectFormat= doc.createElementNS(URI_ETSI,"etsi:DataObjectFormat");
            dataObjectFormat.setAttribute("ObjectReference","#Reference-ID-"+referenceId);
            signedDataObjectProperties.appendChild(dataObjectFormat);

            Element description= doc.createElementNS(URI_ETSI,"etsi:Description");
            description.setTextContent("contenido comprobante");
            dataObjectFormat.appendChild(description);
            Element mimeType= doc.createElementNS(URI_ETSI,"etsi:MimeType");
            mimeType.setTextContent("text/xml");
            dataObjectFormat.appendChild(mimeType);
            //----------------------------------------------------------------------------------------------------------------------------------------------
            //Carga de Key info para hash
            Element keyInfo= doc.createElementNS(URI_DS,"ds:KeyInfo");
            keyInfo.setAttribute("Id","Certificate"+certificateId);

            Element x509Data= doc.createElementNS(URI_DS,"ds:X509Data");
            keyInfo.appendChild(x509Data);

            Element x509Certificate= doc.createElementNS(URI_DS,"ds:X509Certificate");
            certificadoBase64=certificadoBase64=certificadoBase64.replaceAll("(.{76})","$1 ");
            x509Certificate.appendChild(doc.createTextNode(certificadoBase64));
            x509Data.appendChild(x509Certificate);

            Element keyValue= doc.createElementNS(URI_DS,"ds:KeyValue");
            keyInfo.appendChild(keyValue);
            Element rsaKeyValue= doc.createElementNS(URI_DS,"ds:RSAKeyValue");
            keyValue.appendChild(rsaKeyValue);
            Element modulums= doc.createElementNS(URI_DS,"ds:Modulus");
            modulusBase64=modulusBase64.replaceAll("(.{76})","$1 ");
            modulums.appendChild(doc.createTextNode(modulusBase64));
            rsaKeyValue.appendChild(modulums);
            Element exponent= doc.createElementNS(URI_DS,"ds:Exponent");
            exponent.setTextContent(EXPONENT);
            rsaKeyValue.appendChild(exponent);
            //----------------------------------------------------------------------------------------------------------------------------------------------
            //encabezado de signed info
            Element signedInfoElement= doc.createElementNS(URI_DS,"ds:SignedInfo");
            signedInfoElement.setAttribute("Id","Signature-SignedInfo"+signatureInfo);

            Element canonicalizationMethodElement=doc.createElementNS(URI_DS, "ds:CanonicalizationMethod");
            canonicalizationMethodElement.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");

            Element signatureMethodElement=doc.createElementNS(URI_DS,"ds:SignatureMethod");
            signatureMethodElement.setAttribute("Algorithm","http://www.w3.org/2000/09/xmldsig#rsa-sha1");

            Element referenceElement=doc.createElementNS(URI_DS,"ds:Reference");
            referenceElement.setAttribute("Id","SignedPropertiesID"+signedPropertiesId);
            referenceElement.setAttribute("Type","http://uri.etsi.org/01903#SignedProperties");
            referenceElement.setAttribute("URI","#Signature"+signatureId+"-"+"SignedProperties"+signedProperties);

            Element digestMethod= doc.createElementNS(URI_DS,"ds:DigestMethod");
            digestMethod.setAttribute("Algorithm","http://www.w3.org/2000/09/xmldsig#sha1");
            referenceElement.appendChild(digestMethod);//agrega este elemnto dentro de referenceElement

            Element digestValueMethod= doc.createElementNS(URI_DS,"ds:DigestValue");
            String signedProperies= convertElementToString(signedPropertiesElement);
            String base64String=base64Encode(signedProperies);
            digestValueMethod.setTextContent(base64String);
            referenceElement.appendChild(digestValueMethod);//agrega este elemnto dentro de referenceElement

            Element referenceCertElement=doc.createElementNS(URI_DS,"ds:Reference");
            referenceCertElement.setAttribute("URI","#Certificate"+certificateId);

            Element digestCertElement=doc.createElementNS(URI_DS,"ds:DigestMethod");
            digestCertElement.setAttribute("Algorithm","http://www.w3.org/2000/09/xmldsig#sha1");
            referenceCertElement.appendChild(digestCertElement);

            Element digestValueCer= doc.createElementNS(URI_DS,"ds:DigestValue");
            String keyInfoString=convertElementToString(keyInfo);
            String base64KeyInfo=base64Encode(keyInfoString);
            digestValueCer.setTextContent(base64KeyInfo);
            referenceCertElement.appendChild(digestValueCer);

            Element referenceIdElement=doc.createElementNS(URI_DS,"ds:Reference");
            referenceIdElement.setAttribute("Id","Reference-ID-"+referenceId);
            referenceIdElement.setAttribute("URI","#comprobante");

            Element transformElement=doc.createElementNS(URI_DS,"ds:Transform");
            Element transform=doc.createElementNS(URI_DS,"ds:Transform");
            transform.setAttribute("Algorithm","http://www.w3.org/2000/09/xmldsig#enveloped-signature");
            transformElement.appendChild(transform);
            referenceIdElement.appendChild(transformElement);
            Element digestReference=doc.createElementNS(URI_DS,"ds:DigestMEthod");
            digestReference.setAttribute("Algorithm","http://www.w3.org/2000/09/xmldsig#sha1");
            referenceIdElement.appendChild(digestReference);
            Element digestValueRef= doc.createElementNS(URI_DS,"ds:DigestValue");
            String base64Xml=base64Encode(xmlContent);
            digestValueRef.setTextContent(base64Xml);
            referenceIdElement.appendChild(digestValueRef);

            doc.getDocumentElement().appendChild(signatureElement);
            signedInfoElement.appendChild(canonicalizationMethodElement);
            signedInfoElement.appendChild(signatureMethodElement);
            signedInfoElement.appendChild(referenceElement);
            signedInfoElement.appendChild(referenceCertElement);
            signedInfoElement.appendChild(referenceIdElement);

            Element signatureValueElement=doc.createElementNS(URI_DS,"ds:SignatureValue");
            signatureValueElement.setAttribute("Id","SignatureValue"+signatureValue);

            signatureElement.appendChild(signedInfoElement);
            signatureElement.appendChild(signatureValueElement);
            signatureElement.appendChild(keyInfo);
            signatureElement.appendChild(object);

            Document firma=firmarDocuemnto(key,doc,signatureValueElement);
            String firmadoX=convertDocumentToString(firma);

            String xmlFirmado=convertDocumentToString(doc);
            log.info("XML Firmado :\n{}",firmadoX);
            return xmlFirmado;
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error: \n {}",e.getMessage());
            return null;
        }

    }

    /**
     * Método para convertir un documento DOM a una cadena
     * @param doc
     * @return documento convertido a string
     * @throws TransformerException
     */
    private static String convertDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.setOutputProperty(OutputKeys.STANDALONE,"no");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    /**
     * Método para obtener un valor aleatorio
     * @return un string con un valor random
     */
    private static Set<String> numerosGenerados=new HashSet<>();
    private static String obtenerAleatorio(){
        Random random = new Random();
        String randomNumber;
        do {
            int numero= random.nextInt(999000)+990;
            randomNumber=String.valueOf(numero);
        }while (!numerosGenerados.add(randomNumber));
        return randomNumber;
    }

    /**
     * Método para convertir un elemento del xml generado a un string
     * @param element
     * @return elemento convertido a string
     */
    private static String convertElementToString(Element element){
        try {
            TransformerFactory tf= TransformerFactory.newInstance();
            Transformer transformer=tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");

            StringWriter writer= new StringWriter();
            transformer.transform(new DOMSource(element),new StreamResult(writer));
            return writer.getBuffer().toString();
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error: \n {}",e.getMessage());
            return null;
        }
    }

    /**
     * Metodo para convertir un string a hash en base 64
     * @param input
     * @return el valor convertido en base64
     */
    private String base64Encode(String input){
        try {
            java.security.MessageDigest md= java.security.MessageDigest.getInstance("SHA-1");
            byte[] digest=md.digest(input.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(digest);
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error: \n {}",e.getMessage());
            return null;
        }
    }

    /**
     * Método para generar la firma con la llave
     * @param key
     * @param data
     * @return la firma generada en formato de string
     * @throws Exception
     */
    private static String generarFirma(Key key,String data) throws Exception{
        PrivateKey privateKey =(PrivateKey) key;

        Signature signature=Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);

        signature.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signatureBytes= signature.sign();

        String signatureBase64=Base64.getEncoder().encodeToString(signatureBytes);
        signatureBase64= signatureBase64.replaceAll("(.{76})","$1 ");

        return signatureBase64;
    }


    private static Document firmarDocuemnto(Key key,Document doc,Element signatureValueElement)throws Exception{
        String firmaXmlString =convertElementToString(signatureValueElement);

        String firma=generarFirma(key,firmaXmlString);

        signatureValueElement.setTextContent(null);

        signatureValueElement.appendChild(doc.createTextNode(firma));
        return doc;
    }

}
