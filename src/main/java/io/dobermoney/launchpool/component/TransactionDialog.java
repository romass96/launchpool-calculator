package io.dobermoney.launchpool.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.Transaction;
import io.dobermoney.launchpool.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Set;

public class TransactionDialog extends Composite<Dialog> {
    private final DateTimePicker dateTimeField;
    private final Select<TransactionType> typeField;
    private final NumberField amountField;
    private final ComboBox<Coin> coinField;

    @Getter
    private final Transaction transaction;

    @Setter
    private Runnable onSaveAction;

    public TransactionDialog(Set<Coin> coins) {
        this(
                Transaction.builder()
                        .type(TransactionType.DEPOSIT)
                        .dateTime(ZonedDateTime.now())
                        .build(),
                coins
        );
    }

    public TransactionDialog(Transaction transaction, Set<Coin> coins) {
        this.coinField = buildCoinField(coins);
        this.dateTimeField = buildDateTimeField();
        this.typeField = buildTypeField();
        this.amountField = buildAmountField();

        var dialog = getContent();
        var form = new FormLayout();
        form.add(dateTimeField, typeField, amountField, coinField);
        var saveButton = buildSaveButton(dialog);
        var cancelButton = buildCancelButton(dialog);
        dialog.add(form, new HorizontalLayout(saveButton, cancelButton));

        this.transaction = transaction;
        setTransactionFields();
    }

    public void open() {
        getContent().open();
    }

    private void setTransactionFields() {
        if (transaction.getDateTime() != null) {
            dateTimeField.setValue(transaction.getDateTime().toLocalDateTime());
        }

        if (transaction.getType() != null) {
            typeField.setValue(transaction.getType());
        }

        if (transaction.getAmount() > 0) {
            amountField.setValue(transaction.getAmount());
        }

        if (transaction.getCoin() != null) {
            coinField.setValue(transaction.getCoin());
        }
    }

    private DateTimePicker buildDateTimeField() {
        var dateTimeField = new DateTimePicker("Date & Time");
        dateTimeField.setLocale(Locale.UK);
        dateTimeField.setStep(Duration.ofMinutes(1));

        return dateTimeField;
    }

    private Select<TransactionType> buildTypeField() {
        var typeField = new Select<TransactionType>();
        typeField.setItems(TransactionType.values());
        typeField.setLabel("Transaction Type");

        return typeField;
    }

    private ComboBox<Coin> buildCoinField(Set<Coin> coins) {
        var coinField = new ComboBox<Coin>();
        coinField.setItems(coins);
        coinField.setLabel("Coin");
        coinField.setPlaceholder("Select Coin");
        coinField.setRenderer(new ComponentRenderer<>(CoinComponent::new));

        return coinField;
    }

    private NumberField buildAmountField() {
        return new NumberField("Amount");
    }

    private Button buildSaveButton(Dialog dialog) {
        return new Button("Save", ev -> {
            var valid = validateTransactionFields();
            if (!valid) {
                Notification.show("Please fill in all fields.");
                return;
            }

            transaction.setType(typeField.getValue());
            transaction.setAmount(amountField.getValue());
            transaction.setCoin(coinField.getValue());
            transaction.setDateTime(dateTimeField.getValue().atZone(ZoneId.systemDefault()));

            this.onSaveAction.run();
            dialog.close();
        });
    }

    private Button buildCancelButton(Dialog dialog) {
        return new Button("Cancel", ev -> dialog.close());
    }

    private boolean validateTransactionFields() {
        return dateTimeField.getValue() != null && !amountField.isEmpty() && !coinField.isEmpty();
    }
}
