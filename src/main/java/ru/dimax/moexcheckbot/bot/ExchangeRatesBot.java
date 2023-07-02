package ru.dimax.moexcheckbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.dimax.moexcheckbot.exception.ServiceException;
import ru.dimax.moexcheckbot.service.ExchangeRatesService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final String START = "/start";

    private static final String CURRENCIES = "/currencies";

    private static final String HELP = "/help";

    @Autowired
    private ExchangeRatesService exchangeRatesService;


    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String message = String.valueOf(update.getMessage().getText());
        switch (messageText) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case CURRENCIES -> {
                String userName = update.getMessage().getChat().getUserName();
                currenciesCommand(chatId, userName);
            }
            case HELP -> helpCommand(chatId);
            default -> getRatioCommand(chatId, message);
        }
    }

    @Override
    public String getBotUsername() {
        return "moex_check_bot";
    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                
                Здесь Вы сможете узнать официальные курсы валют на сегодня, установленные ЦБ РФ.
                
                Для этого воспользуйтесь командами:
                /{Код валюты} - получение курса указанной валюты к рублю
                /currencies - получение таблицы валют и их кодов
                Дополнительные команды:
                /help - получение справки
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void getRatioCommand(Long chatId, String message) {
        String formattedText;
        try {
            String currencyCharCode = message.substring(1).toUpperCase();
            var usd = exchangeRatesService.getAnyCurrencyExchangeRate(currencyCharCode);
            var text = "Курс %s на %s составляет %s рублей";
            formattedText = String.format(text, currencyCharCode,  LocalDate.now(), usd);
        } catch (ServiceException e) {
            log.error("Ошибка при получении курса ", e);
            formattedText = "Не удалось получить курс . Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void currenciesCommand(Long chatId, String userName) {
        List<String> currenciesWithCodes = exchangeRatesService.getCurrenciesWithCodes();
        String response = String.join("\n", currenciesWithCodes);
        sendMessage(chatId, response);
    }


    private void helpCommand(Long chatId) {
        var text = """
                Справочная информация по боту
                
                Для получения текущих курсов валют воспользуйтесь командами:
                /usd - курс доллара
                /eur - курс евро
                """;
        sendMessage(chatId, text);
    }

    private void unknownCommand(Long chatId) {
        var text = "Не удалось распознать команду";
        sendMessage(chatId, text);
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}
