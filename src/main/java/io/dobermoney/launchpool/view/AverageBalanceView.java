package io.dobermoney.launchpool.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.dobermoney.launchpool.calculator.AverageBalanceCalculator;
import io.dobermoney.launchpool.calculator.request.AverageBalanceCalculationRequest;
import io.dobermoney.launchpool.component.CoinComponent;
import io.dobermoney.launchpool.component.TransactionDialog;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.Currency;
import io.dobermoney.launchpool.model.Transaction;
import io.dobermoney.launchpool.service.CoinService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * View for calculating average balance over a launchpool period.
 * Allows users to define the period, add transactions, and compute the result.
 */
@Route(value = "average-balance", layout = MainLayout.class)
public class AverageBalanceView extends VerticalLayout {
    private final DateTimePicker startPicker = new DateTimePicker("Launchpool Start");
    private final DateTimePicker endPicker = new DateTimePicker("Launchpool End");
    private final Button calculateButton = new Button("Calculate");
    private final Button addTransactionButton = new Button("Add Transaction");
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final H3 result = new H3("Average Balance: -");
    private final List<Transaction> transactions = new ArrayList<>();
    private final AverageBalanceCalculator averageBalanceCalculator;

    /**
     * Creates the average balance view with date pickers, transaction grid, and calculation controls.
     *
     * @param coinService              service for loading available coins
     * @param averageBalanceCalculator calculator for computing average balance
     */
    public AverageBalanceView(CoinService coinService, AverageBalanceCalculator averageBalanceCalculator) {
        this.averageBalanceCalculator = averageBalanceCalculator;

        startPicker.setDatePlaceholder(LocalDate.now().toString());
        startPicker.setLocale(Locale.UK);
        endPicker.setDatePlaceholder(LocalDate.now().toString());
        endPicker.setLocale(Locale.UK);
        startPicker.setStep(Duration.ofMinutes(1));
        endPicker.setStep(Duration.ofMinutes(1));

        var buttonsLayout = new HorizontalLayout(Alignment.END, addTransactionButton, calculateButton);
        var topLayout = new HorizontalLayout(startPicker, endPicker, buttonsLayout);
        add(topLayout);

        var coins = coinService.readCoins();
        System.out.println("Size: " + coins.size());

        grid.addColumn(transaction -> transaction.getDateTime().toString()).setHeader("Date & Time");
        grid.addColumn(Transaction::getType).setHeader("Transaction Type");
        grid.addColumn(Transaction::getAmount).setHeader("Amount");
        grid.addComponentColumn(transaction -> new CoinComponent(transaction.getCoin())).setHeader("Coin");
        grid.addComponentColumn(transaction -> {
            var editButton = new Button("Edit", e -> openEditDialog(transaction, coins));
            var deleteButton = new Button("Delete", e -> {
                transactions.remove(transaction);
                refreshGrid();
            });
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions");


        grid.setItems(transactions);
        grid.setWidthFull();
        add(grid);

        add(result);

        calculateButton.addClickListener(e -> calculate());
        addTransactionButton.addClickListener(e -> openAddDialog(coins));

        setSizeFull();
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void calculate() {
        var request = AverageBalanceCalculationRequest.builder()
                .from(startPicker.getValue().atZone(ZoneId.systemDefault()))
                .to(endPicker.getValue().atZone(ZoneId.systemDefault()))
                .currency(Currency.USD)
                .transactions(transactions)
                .build();
        var averageBalance = averageBalanceCalculator.calculate(request);
        result.setText("Average Balance: %.2f %s".formatted(averageBalance, Currency.USD));
    }


    private void openAddDialog(Set<Coin> coins) {
        var dialog = new TransactionDialog(coins);
        dialog.setOnSaveAction(() -> {
            var dialogTransaction = dialog.getTransaction();
            transactions.add(dialogTransaction);
            refreshGrid();
        });
        dialog.open();
    }

    private void openEditDialog(Transaction transaction, Set<Coin> coins) {
        var dialog = new TransactionDialog(transaction, coins);
        dialog.setOnSaveAction(() -> {
            var dialogTransaction = dialog.getTransaction();
            transaction.setAmount(dialogTransaction.getAmount());
            transaction.setCoin(dialogTransaction.getCoin());
            transaction.setType(dialogTransaction.getType());
            transaction.setDateTime(dialogTransaction.getDateTime());

            refreshGrid();
        });
        dialog.open();
    }

}
