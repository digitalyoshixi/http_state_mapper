package webmapper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CorpusParser {
    public ArrayList<HTTPMessage> parse_corpus(String filename) {
        final ArrayList<HTTPMessage> requests = new ArrayList<>();
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new File(filename));
            final NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                final Element item = (Element) items.item(i);

                // REQUEST
                final String url = getChildText(item, "url");
                final String origin = url.split("/")[2];
                final String method = getChildText(item, "method");
                
                final String timeStr = getChildText(item, "time");
                long time;
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", java.util.Locale.ENGLISH);
                    time = sdf.parse(timeStr).getTime();
                } catch (Exception e) {
                    time = 0L;
                }
           
                
                final String requestRaw = decodeElement(item, "request");
                
                final String body = extractBody(requestRaw);
               
                final String headers = extractHeaders(requestRaw);
                final String cookies = extractHeader(requestRaw, "Cookie");

                // RESPONSE
                final String status = getChildText(item, "status");
                final String mimetype = getChildText(item, "mimetype");
                
                final String responseRaw = decodeElement(item, "response");
                
                final String server = extractHeader(responseRaw, "Server");
                final String date = extractHeader(responseRaw, "Date");

                requests.add(new HTTPMessage(url,
                                            origin,
                                            method,
                                            body,
                                            headers,
                                            cookies,
                                            responseRaw,
                                            status,
                                            time,
                                            date,
                                            server));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse corpus: " + filename, e);
        }
        return requests;
    }

    private static String getChildText(Element parent, String tagName) {
        final NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }

    private static String decodeElement(Element parent, String tagName) {
        final NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        final Element element = (Element) nodes.item(0);
        final String content = element.getTextContent().trim();
        if (content.isEmpty()) {
            return "";
        }
        if ("true".equals(element.getAttribute("base64"))) {
            return new String(Base64.getDecoder().decode(content), StandardCharsets.ISO_8859_1);
        }
        return content;
    }

    private static String normalizeNewlines(String message) {
        return message.replace("\r\n", "\n").replace('\r', '\n');
    }

    private static String extractBody(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        final String normalized = normalizeNewlines(message);
        final int headerEnd = normalized.indexOf("\n\n");
        if (headerEnd == -1) {
            return "";
        }
        return normalized.substring(headerEnd + 2);
    }

    private static String extractHeaders(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        final String normalized = normalizeNewlines(message);
        final int headerEnd = normalized.indexOf("\n\n");
        final String headerBlock = headerEnd == -1 ? normalized : normalized.substring(0, headerEnd);
        final int firstNewline = headerBlock.indexOf('\n');
        if (firstNewline == -1) {
            return "";
        }

        final StringBuilder headers = new StringBuilder();
        for (String line : headerBlock.substring(firstNewline + 1).split("\n")) {
            if (line.isEmpty() || line.toLowerCase().startsWith("cookie:")) {
                continue;
            }
            if (headers.length() > 0) {
                headers.append('\n');
            }
            headers.append(line);
        }
        return headers.toString();
    }

    private static String extractHeader(String message, String headerName) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        final String prefix = headerName.toLowerCase() + ":";
        for (String line : normalizeNewlines(message).split("\n")) {
            if (line.toLowerCase().startsWith(prefix)) {
                return line.substring(headerName.length() + 1).trim();
            }
        }
        return "";
    }
    
}