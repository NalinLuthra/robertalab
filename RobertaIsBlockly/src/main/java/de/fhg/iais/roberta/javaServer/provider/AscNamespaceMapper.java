package de.fhg.iais.roberta.javaServer.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * when JAXB objects are marshalled to XML, this namespace prefix mapper is used to generate sensible namespace abbreviations ("prefixes") for name spaces. If a
 * namespace URI is not registered in this class, but a prefix is requested, a RuntimeException is thrown. If this is too restrictive, it may be replaced by
 * returning null and let JAXB generate non-intuitive prefixes. <br>
 * <br>
 * For each marshalling process one object of this class has to be created.
 * 
 * @author rbudde
 */
public class AscNamespaceMapper extends NamespacePrefixMapper
{

    private static final Map<String, String> allNs;
    static {
        Map<String, String> t = new HashMap<String, String>();
        t.put("http://www.budde.de/asc/1.0/", "asc");
        t.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        t.put("http://www.w3.org/XML/1998/namespace", "ns");
        allNs = Collections.unmodifiableMap(t);
    }
    private final Set<String> usedNs = new ConcurrentHashSet<String>();

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        String prefix = allNs.get(namespaceUri);
        if ( prefix != null ) {
            usedNs.add(namespaceUri);
            return prefix;
        } else {
            throw new RuntimeException("Invalid namespaceUri encountered: " + namespaceUri);
        }
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        // It is expected that this method is called late in the marshalling process, i.e. after all elements have been generated. It returns only
        // URIs that have been used during THIS marshalling process. If this rises errors, change this method and return ALL URIs.
        return usedNs.toArray(new String[usedNs.size()]);
    }
}