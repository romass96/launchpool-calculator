package io.dobermoney.launchpool.component;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.dobermoney.launchpool.model.Coin;

public class CoinComponent extends Composite<HorizontalLayout> {

    public CoinComponent(Coin coin) {
        var layout = getContent();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        var icon = new Image(coin.getImage(), coin.getName());
        icon.setWidth(20, Unit.PIXELS);
        icon.setHeight(20, Unit.PIXELS);
        layout.add(icon);

        var symbol = new Span(coin.getSymbol().toUpperCase());
        layout.add(symbol);
    }
}
