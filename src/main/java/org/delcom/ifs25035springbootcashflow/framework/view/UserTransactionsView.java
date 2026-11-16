package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25035springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25035springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25035springbootcashflow.usecase.UserUseCase;

import java.util.Comparator;
import java.util.List;

@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;

    private final Grid<Transaction> grid = new Grid<>();
    private final H2 title = new H2();
    private final Span balance = new Span();

    public UserTransactionsView(FinanceUseCase finance, FinancePresenter presenter,
                                UserUseCase userUseCase, IUserRepository userRepository) {
        this.finance = finance;
        this.presenter = presenter;
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        setSizeFull();

        grid.addColumn(Transaction::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addColumn(t -> presenter.typeLabel(t.getType())).setHeader("Tipe");
        grid.addComponentColumn(t -> proofThumbnail(t.getProofPath())).setHeader("Bukti");
        grid.addColumn(t -> presenter.formatDate(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        GridStyles.apply(grid); // tanpa kolom hapus = read-only

        Button back = new Button("Kembali", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate("users"));
        add(back, title, balance, grid);
    }

    private Component proofThumbnail(String path) {
        if (path == null) {
            return new Span("-");
        }
        Image image = new Image(path, "Bukti transaksi");
        image.addClassName("proof-thumb");
        image.getElement().setAttribute("loading", "lazy");
        image.getElement().setAttribute("decoding", "async");
        image.addClickListener(e -> new ImagePreviewDialog(path).open());
        return image;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User admin = AuthSession.currentUser(userRepository);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            event.forwardTo("");
            return;
        }
        String raw = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("userId", List.of()).stream().findFirst().orElse(null);
        try {
            int id = Integer.parseInt(raw);
            User target = userUseCase.get(id);
            title.setText("Transaksi: " + target.getFullName() + " (read-only)");
            grid.setItems(finance.list(id));
            balance.setText(presenter.formatBalance(finance.getBalance(id)));
        } catch (NumberFormatException | DomainException e) {
            event.forwardTo("users");
        }
    }
}