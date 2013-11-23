package RemoteCtrl;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlContentHandler extends DefaultHandler {
	public class Element {
		public String name;
		public String value;
		public String parent = "";
	}
	private Element currentElement;
	private String parentKey;
	private int depth;
	private String value = null;
	private List<Element> elements;

	public List<Element> getElements() {
		return elements;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		if (depth != 0)
			throw new SAXException("depth not equal to 0");
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		elements = new ArrayList<Element>();
		depth = 0;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if (value == null || value.equals("\n"))
			value = new String(ch, start, length);
		else
			value = value.concat(new String(ch, start, length));
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		depth++;
		if (depth == 2)
			parentKey = localName;
		if (depth > 1) {
			currentElement = new Element();
			currentElement.name = localName;
			if (depth == 3)
				currentElement.parent = parentKey;
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);
		if (!value.equals("\n"))
		{
			currentElement.value = value;
			elements.add(currentElement);
		}

		value = null;

		depth--;
	}
}