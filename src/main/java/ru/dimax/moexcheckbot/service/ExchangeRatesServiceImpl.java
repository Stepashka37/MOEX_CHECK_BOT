package ru.dimax.moexcheckbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.dimax.moexcheckbot.client.CbrClient;
import ru.dimax.moexcheckbot.exception.ServiceException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";

    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";


    @Autowired
    private CbrClient client;



    @Override
    public String getAnyCurrencyExchangeRate(String currencyCharCode) throws ServiceException {
        var xml = client.getCurrencyRatesXML();
        String currencyXPATH = String.format("/ValCurs//Valute[CharCode='%s']/Value", currencyCharCode);
        return extractCurrencyValueFromXML(xml, currencyXPATH);
    }

    @Override
    public List<String> getCurrenciesWithCodes() {
        var xml = client.getCurrencyRatesXML();
        return extractCurrenciesWithCodesFromXML(xml);
    }


    private static String extractCurrencyValueFromXML(String xml, String xpathExpression) throws ServiceException {
        var source = new InputSource(new StringReader(xml));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var document = (Document) xpath.evaluate("/", source, XPathConstants.NODE);

            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException e) {
            throw new ServiceException("Не удалось распарсить XML", e);

        }
    }

    private static List<String> extractCurrenciesWithCodesFromXML(String xml) throws ServiceException {
        var source =  new InputSource(new StringReader(xml));
        try {
            List<String> result = new ArrayList<String>();
            var xpath = XPathFactory.newInstance().newXPath();
            var document =  xpath.evaluate("CharCode", source, XPathConstants.NODESET);
            /*for (int i = 0; i < nodeList.getLength(); i++) {
                String item = nodeList.item(i).getTextContent();
                result.add(nodeList.item(i).getNodeValue());
            }*/
            return result;
        } catch (XPathExpressionException e) {
            throw new ServiceException("Не удалось распарсить XML", e);

        }
    }
}
