package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25035springbootcashflow.usecase.UserUseCase;

import java.util.Map;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;

    public UsersView(UserUseCase userUseCase, IUserRepository userRepository) {
        this.userRepository = userRepository;
        setSizeFull();

        Grid<User> grid = new Grid<>();
        grid.addColumn(User::getId).setHeader("ID").setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role");
        grid.addComponentColumn(u -> new Button("Lihat Transaksi", e ->
                UI.getCurrent().navigate("user-transactions",
                        QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))))
        ).setHeader("Aksi");
        GridStyles.apply(grid);
        grid.setItems(userUseCase.list());

        add(new H2("Daftar Pengguna"), grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null || user.getRole() != Role.ADMIN) {
            event.forwardTo("");
        }
    }
}