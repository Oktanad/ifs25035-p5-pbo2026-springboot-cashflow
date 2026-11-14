package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25035springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Login - Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Catatan: JANGAN pakai setId() di field, karena id akan menempel di
        // elemen pembungkus Vaadin. Id harus ada di <input> aslinya.
        TextField username = new TextField("Username");
        username.setWidthFull();
        assignInputId(username, "login-username");

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        assignInputId(password, "login-password");

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button loginButton = new Button("Masuk");
        loginButton.setId("login-submit");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.getStyle().set("cursor", "pointer");
        loginButton.getStyle().set("margin-top", "0.85rem");

        loginButton.addClickListener(e -> {
            progress.setVisible(true);
            loginButton.setEnabled(false);
            try {
                User user = authUseCase.login(username.getValue(), password.getValue());
                AuthSession.login(user);
                UI.getCurrent().navigate("");
            } catch (DomainException ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                loginButton.setEnabled(true);
            }
        });

        PageCard card = new PageCard();
        card.setWidth("380px");
        card.add(new H2("Masuk"),
                new Paragraph("Gunakan kredensial akun kamu untuk masuk"),
                username, password, progress, loginButton,
                new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        add(card);
    }

    /** Memasang id pada elemen input asli di dalam field Vaadin, dan label for-nya. */
    private static void assignInputId(Component field, String id) {
        field.addAttachListener(e -> field.getElement().executeJs(
                "const host = this;"
                        + "const apply = () => {"
                        + "  const input = host.querySelector('input');"
                        + "  if (!input) return false;"
                        + "  input.id = $0;"
                        + "  const label = host.querySelector('label[slot=label]');"
                        + "  if (label) label.setAttribute('for', $0);"
                        + "  return true;"
                        + "};"
                        + "if (!apply()) {"
                        + "  requestAnimationFrame(apply);"
                        + "  setTimeout(apply, 100);"
                        + "}", id));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.forwardTo("");
        }
    }
}