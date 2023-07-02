package ru.dimax.moexcheckbot.service;

import ru.dimax.moexcheckbot.exception.ServiceException;

import java.util.List;

public interface ExchangeRatesService {



    String getAnyCurrencyExchangeRate(String currencyCharCode) throws ServiceException;

    List<String> getCurrenciesWithCodes();

}
