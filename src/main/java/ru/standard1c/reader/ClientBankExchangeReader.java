package ru.standard1c.reader;

import ru.standard1c.format.CheckingAccountBalance;
import ru.standard1c.format.ClientBankExchange;
import ru.standard1c.format.Document;
import ru.standard1c.format.DocumentType;
import ru.standard1c.format.Encoding;
import ru.standard1c.format.PaymentType;
import ru.standard1c.reader.source.Attribute;
import ru.standard1c.reader.source.AttributeSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Maxim Tereshchenko
 */
public class ClientBankExchangeReader
        implements Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> {

    private static final Function<String, LocalDate> DATE_MAPPER = date ->
            DateTimeFormatter.ofPattern("dd.MM.yyyy").parse(date, LocalDate::from);
    private static final Function<String, LocalTime> TIME_MAPPER = time ->
            DateTimeFormatter.ofPattern("HH:mm:ss").parse(time, LocalTime::from);

    private final Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> reader =
            clientBankExchangeReader();

    @Override
    public String startOfSectionAttributeKey() {
        return reader.startOfSectionAttributeKey();
    }

    @Override
    public String endOfSectionAttributeKey() {
        return reader.endOfSectionAttributeKey();
    }

    @Override
    public Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> onAttribute(
            String key,
            BiConsumer<ClientBankExchange.ClientBankExchangeBuilder, String> valueConsumer
    ) {
        return reader.onAttribute(key, valueConsumer);
    }

    @Override
    public <E> Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> onAttribute(
            String key,
            Function<String, E> mapper,
            BiConsumer<ClientBankExchange.ClientBankExchangeBuilder, E> valueConsumer
    ) {
        return reader.onAttribute(key, mapper, valueConsumer);
    }

    @Override
    public <S, U> Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> onSection(
            Reader<S, U> reader,
            BiConsumer<ClientBankExchange.ClientBankExchangeBuilder, U> sectionConsumer
    ) {
        return this.reader.onSection(reader, sectionConsumer);
    }

    @Override
    public <U> Reader<ClientBankExchange.ClientBankExchangeBuilder, U> onEndOfSection(
            Function<ClientBankExchange.ClientBankExchangeBuilder, U> finisher
    ) {
        return reader.onEndOfSection(finisher);
    }

    @Override
    public Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> onAnyOtherAttribute(
            BiConsumer<ClientBankExchange.ClientBankExchangeBuilder, Attribute> attributeConsumer
    ) {
        return reader.onAnyOtherAttribute(attributeConsumer);
    }

    @Override
    public Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> failOnUnknownAttribute() {
        return reader.failOnUnknownAttribute();
    }

    @Override
    public ClientBankExchange read(AttributeSource attributeSource) {
        return reader.read(attributeSource);
    }

    private Reader<ClientBankExchange.ClientBankExchangeBuilder, ClientBankExchange> clientBankExchangeReader() {
        return DefaultReader.from(
                        "1CClientBankExchange",
                        "КонецФайла",
                        ClientBankExchange::builder
                )
                .onAttribute(
                        "ВерсияФормата",
                        Float::parseFloat,
                        ClientBankExchange.ClientBankExchangeBuilder::formatVersion
                )
                .onAttribute("Кодировка", Encoding::from, ClientBankExchange.ClientBankExchangeBuilder::encoding)
                .onAttribute("Отправитель", ClientBankExchange.ClientBankExchangeBuilder::sender)
                .onAttribute("Получатель", ClientBankExchange.ClientBankExchangeBuilder::receiver)
                .onAttribute(
                        "ДатаСоздания",
                        DATE_MAPPER,
                        ClientBankExchange.ClientBankExchangeBuilder::creationDate
                )
                .onAttribute(
                        "ВремяСоздания",
                        TIME_MAPPER,
                        ClientBankExchange.ClientBankExchangeBuilder::creationTime
                )
                .onAttribute(
                        "ДатаНачала",
                        DATE_MAPPER,
                        ClientBankExchange.ClientBankExchangeBuilder::startingDate
                )
                .onAttribute(
                        "ДатаКонца",
                        DATE_MAPPER,
                        ClientBankExchange.ClientBankExchangeBuilder::endingDate
                )
                .onAttribute(
                        "РасчСчет",
                        ClientBankExchange.ClientBankExchangeBuilder::checkingAccount
                )
                .onAttribute(
                        "Документ",
                        DocumentType::from,
                        ClientBankExchange.ClientBankExchangeBuilder::documentType
                )
                .onSection(
                        checkingAccountBalanceReader(),
                        ClientBankExchange.ClientBankExchangeBuilder::checkingAccountBalance
                )
                .onSection(
                        documentReader(),
                        ClientBankExchange.ClientBankExchangeBuilder::document
                )
                .failOnUnknownAttribute()
                .onEndOfSection(ClientBankExchange.ClientBankExchangeBuilder::build);
    }

    private Reader<CheckingAccountBalance.CheckingAccountBalanceBuilder, CheckingAccountBalance> checkingAccountBalanceReader() {
        return DefaultReader.from(
                        "СекцияРасчСчет",
                        "КонецРасчСчет",
                        CheckingAccountBalance::builder
                )
                .onAttribute(
                        "ДатаНачала",
                        DATE_MAPPER,
                        CheckingAccountBalance.CheckingAccountBalanceBuilder::startingDate
                )
                .onAttribute(
                        "ДатаКонца",
                        DATE_MAPPER,
                        CheckingAccountBalance.CheckingAccountBalanceBuilder::endingDate
                )
                .onAttribute("РасчСчет", CheckingAccountBalance.CheckingAccountBalanceBuilder::checkingAccount)
                .onAttribute(
                        "НачальныйОстаток",
                        BigDecimal::new,
                        CheckingAccountBalance.CheckingAccountBalanceBuilder::startingBalance
                )
                .onAttribute(
                        "ВсегоПоступило",
                        BigDecimal::new,
                        CheckingAccountBalance.CheckingAccountBalanceBuilder::totalReceived
                )
                .onAttribute(
                        "ВсегоСписано",
                        BigDecimal::new,
                        CheckingAccountBalance.CheckingAccountBalanceBuilder::totalDecommissioned
                )
                .onAttribute(
                        "КонечныйОстаток",
                        BigDecimal::new,
                        CheckingAccountBalance.CheckingAccountBalanceBuilder::remainingBalance
                )
                .failOnUnknownAttribute()
                .onEndOfSection(CheckingAccountBalance.CheckingAccountBalanceBuilder::build);
    }

    private Reader<Document.DocumentBuilder, Document> documentReader() {
        return DefaultReader.from(
                        "СекцияДокумент",
                        "КонецДокумента",
                        DocumentType::from,
                        type -> Document.builder().documentType(type)
                )
                .onAttribute("Номер", Integer::parseInt, Document.DocumentBuilder::number)
                .onAttribute("Дата", DATE_MAPPER, Document.DocumentBuilder::date)
                .onAttribute("Сумма", BigDecimal::new, Document.DocumentBuilder::sum)
                .onAttribute("КвитанцияДата", DATE_MAPPER, Document.DocumentBuilder::receiptDate)
                .onAttribute("КвитанцияВремя", TIME_MAPPER, Document.DocumentBuilder::receiptTime)
                .onAttribute("КвитанцияСодержание", Document.DocumentBuilder::receiptContent)
                .onAttribute("ПлательщикСчет", Document.DocumentBuilder::payerAccount)
                .onAttribute("ДатаСписано", DATE_MAPPER, Document.DocumentBuilder::decommissionDate)
                .onAttribute("Плательщик", Document.DocumentBuilder::payer)
                .onAttribute("ПлательщикИНН", Document.DocumentBuilder::payerInn)
                .onAttribute("Плательщик1", Document.DocumentBuilder::payer1)
                .onAttribute("Плательщик2", Document.DocumentBuilder::payer2)
                .onAttribute("Плательщик3", Document.DocumentBuilder::payer3)
                .onAttribute("Плательщик4", Document.DocumentBuilder::payer4)
                .onAttribute("ПлательщикРасчСчет", Document.DocumentBuilder::payerCheckingAccount)
                .onAttribute("ПлательщикБанк1", Document.DocumentBuilder::payerBank1)
                .onAttribute("ПлательщикБанк2", Document.DocumentBuilder::payerBank2)
                .onAttribute("ПлательщикБИК", Document.DocumentBuilder::payerBic)
                .onAttribute("ПлательщикКорсчет", Document.DocumentBuilder::payerCorrespondentAccount)
                .onAttribute("ПолучательСчет", Document.DocumentBuilder::receiverAccount)
                .onAttribute("ДатаПоступило", DATE_MAPPER, Document.DocumentBuilder::receivingDate)
                .onAttribute("Получатель", Document.DocumentBuilder::receiver)
                .onAttribute("ПолучательИНН", Document.DocumentBuilder::receiverInn)
                .onAttribute("Получатель1", Document.DocumentBuilder::receiver1)
                .onAttribute("Получатель2", Document.DocumentBuilder::receiver2)
                .onAttribute("Получатель3", Document.DocumentBuilder::receiver3)
                .onAttribute("Получатель4", Document.DocumentBuilder::receiver4)
                .onAttribute("ПолучательРасчСчет", Document.DocumentBuilder::receiverCheckingAccount)
                .onAttribute("ПолучательБанк1", Document.DocumentBuilder::receiverBank1)
                .onAttribute("ПолучательБанк2", Document.DocumentBuilder::receiverBank2)
                .onAttribute("ПолучательБИК", Document.DocumentBuilder::receiverBic)
                .onAttribute("ПолучательКорсчет", Document.DocumentBuilder::receiverCorrespondentAccount)
                .onAttribute("ВидПлатежа", PaymentType::from, Document.DocumentBuilder::paymentType)
                .onAttribute("КодНазПлатежа", Integer::parseInt, Document.DocumentBuilder::paymentPurposeCode)
                .onAttribute("ВидОплаты", Document.DocumentBuilder::transactionType)
                .onAttribute("Код", Document.DocumentBuilder::code)
                .onAttribute("НазначениеПлатежа", Document.DocumentBuilder::paymentPurpose)
                .onAttribute("НазначениеПлатежа1", Document.DocumentBuilder::paymentPurpose1)
                .onAttribute("НазначениеПлатежа2", Document.DocumentBuilder::paymentPurpose2)
                .onAttribute("НазначениеПлатежа3", Document.DocumentBuilder::paymentPurpose3)
                .onAttribute("НазначениеПлатежа4", Document.DocumentBuilder::paymentPurpose4)
                .onAttribute("НазначениеПлатежа5", Document.DocumentBuilder::paymentPurpose5)
                .onAttribute("НазначениеПлатежа6", Document.DocumentBuilder::paymentPurpose6)
                .onAttribute("СтатусСоставителя", Document.DocumentBuilder::compilerStatus)
                .onAttribute("ПлательщикКПП", Document.DocumentBuilder::payerKpp)
                .onAttribute("ПолучательКПП", Document.DocumentBuilder::receiverKpp)
                .onAttribute("ПоказательКБК", Document.DocumentBuilder::cbcIndicator)
                .onAttribute("ОКАТО", Document.DocumentBuilder::oktmo)
                .onAttribute("ПоказательОснования", Document.DocumentBuilder::basisIndicator)
                .onAttribute("ПоказательПериода", Document.DocumentBuilder::periodIndicator)
                .onAttribute("ПоказательНомера", Document.DocumentBuilder::numberIndicator)
                .onAttribute("ПоказательДаты", DATE_MAPPER, Document.DocumentBuilder::dateIndicator)
                .onAttribute("ПоказательТипа", Document.DocumentBuilder::typeIndicator)
                .onAttribute("Очередность", Integer::parseInt, Document.DocumentBuilder::priority)
                .onAttribute("СрокАкцепта", Integer::valueOf, Document.DocumentBuilder::acceptanceTerm)
                .onAttribute("ВидАккредитива", Document.DocumentBuilder::letterOfCreditType)
                .onAttribute("СрокПлатежа", DATE_MAPPER, Document.DocumentBuilder::paymentTerm)
                .onAttribute("УсловиеОплаты1", Document.DocumentBuilder::paymentCondition1)
                .onAttribute("УсловиеОплаты2", Document.DocumentBuilder::paymentCondition2)
                .onAttribute("УсловиеОплаты3", Document.DocumentBuilder::paymentCondition3)
                .onAttribute("ПлатежПоПредст", Document.DocumentBuilder::paymentOnPresentation)
                .onAttribute("ДополнУсловия", Document.DocumentBuilder::additionalConditions)
                .onAttribute("НомерСчетаПоставщика", Document.DocumentBuilder::supplierAccountNumber)
                .onAttribute("ДатаОтсылкиДок", DATE_MAPPER, Document.DocumentBuilder::documentDispatchDate)
                .failOnUnknownAttribute()
                .onEndOfSection(Document.DocumentBuilder::build);
    }
}
