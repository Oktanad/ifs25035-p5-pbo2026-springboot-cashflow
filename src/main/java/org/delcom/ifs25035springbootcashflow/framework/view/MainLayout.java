package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private boolean built = false;

    public MainLayout(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!built) {
            build(user);
            built = true;
        }
    }

    private void build(User user) {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Buka menu navigasi");

        H1 title = new H1("Cashflow");
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-size", "var(--lumo-font-size-xl)");

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage(user.getPhotoPath());
        }

        MenuBar menu = new MenuBar();
        menu.getElement().setAttribute("aria-label", "Menu pengguna");
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        MenuItem root = menu.addItem(avatar);
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate("profile"));
        root.getSubMenu().addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().navigate(LoginView.class);
        });

        HorizontalLayout header = new HorizontalLayout(toggle, title, menu);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.setPadding(true);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Cashflow", "", VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", "profile", VaadinIcon.USER.create()));
        if (user.getRole() == Role.ADMIN) {
            nav.addItem(new SideNavItem("Pengguna", "users", VaadinIcon.USERS.create()));
        }

        // Landmark banner
        addToNavbar(new Header(header));
        addToDrawer(nav);
    }

    // Landmark main: berlaku untuk semua halaman yang memakai MainLayout
    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        if (content != null) {
            content.getElement().setAttribute("role", "main");
        }
    }
}