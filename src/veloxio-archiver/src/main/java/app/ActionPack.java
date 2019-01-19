package app;

import veloxio.ArchiveWriter;
import veloxio.VeloxConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

class Entry {
    public String path;
    public String diskPath;
    public int flags;
}

public class ActionPack {
    /**
     * Path to the xml file
     */
    private String xmlPath;

    /**
     * Path to the archive (output)
     */
    private String packPath;

    /**
     * Files
     */
    private HashMap<String, Entry> files;

    /**
     * Base path
     */
    private String basePath;

    /**
     * Constructor
     * @param pXmlPath to the xml containing a file list
     * @param pPackPath to the output file
     */
    public ActionPack(String basePath, String pXmlPath, String pPackPath) {
        this.basePath = basePath;
        this.xmlPath = pXmlPath;
        this.files = new HashMap<>();
        this.packPath = pPackPath;
    }

    /**
     * Run (action entry)
     */
    public void Run() {
        if (!LoadXml()) {
            System.out.printf("[Error] Failed to load xml: %s \n", xmlPath);
            return;
        }

        // Write
        if (!WriteArchive())
            System.out.printf("[Error] Failed to write archive: %s \n", packPath);
        else
            System.out.printf("Successfully written: %s ", packPath);
    }


    /**
     * Read value from node
     * @param e entry
     * @param attr Node
     * @return boolean true if node valid
     */
    private boolean ReadValue(Entry e, Node attr) {
        switch (attr.getNodeName()) {
            case "diskPath":
                e.diskPath = attr.getNodeValue();
                break;
            case "path":
                e.path = attr.getNodeValue();
                break;
            case "flags":
                String data = attr.getNodeValue().trim().replace(" ", "");
                String[] flags = data.split("\\|");

                // Flags
                for (String flag : flags) {
                    if (flag.equals("COMPRESS")) {
                        e.flags = e.flags | VeloxConfig.COMPRESS;
                    } else if (flag.equals("CRYPT")) {
                        e.flags = e.flags | VeloxConfig.CRYPT;
                    }
                }
                break;
            default:
                return false;
        }

        return true;
    }

    /**
     * Load xml
     * @return boolean true if success
     */
    private boolean LoadXml() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setValidating(false);
        DocumentBuilder builder = null;
        Document doc = null;
        // Get parser
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            //e.printStackTrace();
            System.out.printf("Failed to initialize %s parser with error: %s", xmlPath, e.getMessage());
            return false;
        }

        // Own error handler
        builder.setErrorHandler(new XmlErrorHandler());

        // Parse xml
        File file = new File(xmlPath);
        try {
            doc = builder.parse(file);
        } catch (SAXException e) {
            System.out.printf("Failed to parse xml %s with error: %s", xmlPath, e.getMessage());
            return false;
        } catch (IOException e) {
            System.out.printf("Failed to parse xml %s with error: %s", xmlPath, e.getMessage());
            return false;
        }

        // Get root
        Node root = doc.getChildNodes().item(0);
        if (root == null || !root.getNodeName().equals("Archive")) {
            System.out.printf("Missing root 'Archive' in: %s \n", xmlPath);
        }

        // If no root
        if (root == null)
            return false;

        // Loop trough files
        NodeList nodeList = root.getChildNodes();
        if (nodeList == null)
            return true;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Entry e = new Entry();

                // Attrs
                Node attr1 = currentNode.getAttributes().item(0);
                Node attr2 = currentNode.getAttributes().item(1);
                Node attr3 = currentNode.getAttributes().item(2);

                // Parse 2 attributes
                if (!ReadValue(e, attr1))
                    continue;
                if (!ReadValue(e, attr2))
                    continue;
                if (attr3 != null) {
                    if (!ReadValue(e, attr3)) {
                        continue;
                    }
                }

                // Add file to map
                files.put(e.path, e);
            }
        }

        return true;
    }

    private boolean WriteArchive() {
        ArchiveWriter writer = null;
        try {
            writer = new ArchiveWriter(packPath);
        } catch (FileNotFoundException e) {
            return false;
        }

        // Write archive
        for (HashMap.Entry<String, Entry> entry : files.entrySet()) {
            Entry value = entry.getValue();
            try {
                writer.WriteFile(basePath + "/" + value.path, value.diskPath, value.flags);
            } catch (IOException e) {
                System.out.printf("[Error] Failed to write file: %s error: %s",value.path, e.getMessage());
                return false;
            }
        }

        // Write index and header
        writer.WriteIndex();
        writer.WriteHeader();
        return true;
    }
}
