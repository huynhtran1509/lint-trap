package com.willowtreeapps.android.plugins;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * User: charlie Date: 8/15/12 Time: 2:49 PM
 */
public class XmlHandler extends DefaultHandler
{
    private ArrayList<LintXmlError> errors;
    private LintXmlError current;

    public XmlHandler()
    {
        errors = new ArrayList<LintXmlError>();
        current = new LintXmlError();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if(qName.equalsIgnoreCase("issue"))
        {
            current.severity = attributes.getValue("severity");
            current.errorCheck = attributes.getValue("id");
            current.message = attributes.getValue("message");
        }
        else if(qName.equalsIgnoreCase("location"))
        {
            current.location.column = attributes.getValue("column");
            current.location.line = attributes.getValue("line");
            current.location.file = attributes.getValue("file");
        }


    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("issue"))
        {
            errors.add(current);
            current = new LintXmlError();
        }
    }

    public ArrayList<LintXmlError> getErrors()
    {
        return errors;
    }

}
