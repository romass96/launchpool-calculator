package io.dobermoney.launchpool.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

/**
 * Main application layout with navigation drawer and header.
 */
public class MainLayout extends AppLayout {

    /**
     * Creates the main layout with header and navigation links.
     */
    public MainLayout() {
        // --- Top navbar ---
        DrawerToggle toggle = new DrawerToggle(); // for responsive sidebar toggle
        H1 title = new H1("Crypto tools");
        title.getStyle().set("font-size", "1.5em").set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(toggle, title);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle().set("padding", "0 1em");
        addToNavbar(header);

        // --- Side drawer (navigation links) ---
        RouterLink calculator = new RouterLink("Average balance calculator", AverageBalanceView.class);

        // Add all links to drawer
        addToDrawer(new VerticalLayout(calculator));
    }
}
