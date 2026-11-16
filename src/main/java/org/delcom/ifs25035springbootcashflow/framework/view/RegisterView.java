package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25035springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Registrasi - Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        TextField fullName = new TextField("Nama Lengkap");
        fullName.setWidthFull();
        fullName.setAutocomplete(Autocomplete.NAME);

        TextField username = new TextField("Username");
        username.setWidthFull();
        username.setAutocomplete(Autocomplete.USERNAME);

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        password.setAutocomplete(Autocomplete.NEW_PASSWORD);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button registerButton = new Button("Daftar");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> {
            progress.setVisible(true);
            registerButton.setEnabled(false);
            try {
                authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
                Notification.show("Registrasi berhasil, silakan login");
                UI.getCurrent().navigate(LoginView.class);
            } catch (DomainException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                registerButton.setEnabled(true);
            }
        });

        H1 heading = new H1("Daftar Akun");
        heading.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("margin", "0");

        PageCard card = new PageCard();
        card.setWidth("380px");
        card.setMaxWidth("100%");
        card.add(heading, fullName, username, password, progress, registerButton,
                new RouterLink("Sudah punya akun? Masuk", LoginView.class));

        add(new Main(card));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo("");
        }
    }
}