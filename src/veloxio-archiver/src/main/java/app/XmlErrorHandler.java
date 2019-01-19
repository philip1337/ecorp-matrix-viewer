package app;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        System.out.printf("[Warning] %s \n", exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        System.out.printf("[Error] %s \n", exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        System.out.printf("[Fatal error] %s \n", exception.getMessage());
    }
}
