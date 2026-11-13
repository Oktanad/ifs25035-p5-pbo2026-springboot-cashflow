package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25035springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25035springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25035springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25035springbootcashflow.usecase.FinanceUseCase;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final User user;

    private final Grid<Transaction> grid = new Grid<>();
    private final TextField search = new TextField();
    private final ComboBox<SortOption> sort = new ComboBox<>();
    private final Span total = new Span();
    private final Span income = new Span();
    private final Span expense = new Span();
    private final Span balance = new Span();

    public FinanceView(FinanceUseCase finance, FinancePresenter presenter,
                       FileStorage storage, IUserRepository userRepository) {
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        this.user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return; // MainLayout akan mengalihkan ke /login
        }
        setSizeFull();
        add(buildSummary(), buildToolbar(), buildGrid());
        refresh();
    }

    private Component buildSummary() {
        HorizontalLayout row = new HorizontalLayout(
                card("Total Transaksi", total, null),
                card("Total Pemasukan", income, "income"),
                card("Total Pengeluaran", expense, "expense"),
                card("Saldo", balance, null));
        row.setWidthFull();
        row.getStyle().set("flex-wrap", "wrap");
        return row;
    }

    private Div card(String label, Span value, String cssClass) {
        Div box = new Div();
        box.addClassName("summary-card");
        Span title = new Span(label);
        title.addClassName("label");
        value.addClassName("value");
        if (cssClass != null) {
            value.addClassName(cssClass);
        }
        box.add(title, new Div(value));
        return box;
    }

    private Component buildToolbar() {
        search.setPlaceholder("Cari keterangan...");
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setPlaceholder("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::getLabel);
        sort.setClearButtonVisible(true);
        sort.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refresh());
        Button addIncome = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openForm(TransactionType.INCOME));
        addIncome.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        Button addExpense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openForm(TransactionType.EXPENSE));
        addExpense.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout bar = new HorizontalLayout(search, sort, reload, addIncome, addExpense);
        bar.setWidthFull();
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        bar.getStyle().set("flex-wrap", "wrap");
        return bar;
    }

    private Grid<Transaction> buildGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setSortable(true)
                .setWidth("80px").setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addColumn(t -> presenter.typeLabel(t.getType())).setHeader("Tipe")
                .setComparator(Comparator.comparing(t -> t.getType().name()));
        grid.addComponentColumn(t -> proofThumbnail(t.getProofPath())).setHeader("Bukti");
        grid.addColumn(t -> presenter.formatDate(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.addComponentColumn(t -> {
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(t));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return delete;
        }).setHeader("Aksi");
        GridStyles.apply(grid);
        return grid;
    }

    private Component proofThumbnail(String path) {
        if (path == null) {
            return new Span("-");
        }
        Image image = new Image(path, "bukti");
        image.addClassName("proof-thumb");
        image.addClickListener(e -> new ImagePreviewDialog(path).open());
        return image;
    }

    private void refresh() {
        List<Transaction> data = finance.search(user.getId(), search.getValue());
        if (sort.getValue() != null) {
            data = finance.sort(data, sort.getValue());
        }
        grid.setItems(data);

        FinanceUseCase.Summary summary = finance.getSummary(user.getId());
        total.setText(String.valueOf(summary.count()));
        income.setText(presenter.formatMoney(summary.income()));
        expense.setText(presenter.formatMoney(summary.expense()));
        balance.setText(presenter.formatMoney(summary.balance()));
    }

    private void openForm(TransactionType type) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(type == TransactionType.INCOME ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        Span badge = new Span(presenter.typeLabel(type));
        badge.getElement().getThemeList().add("badge");
        dialog.getHeader().add(badge);
        dialog.setWidth("460px");

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        byte[][] fileData = new byte[1][];
        String[] fileName = new String[1];
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            fileData[0] = data;
            fileName[0] = metadata.fileName();
        }));
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFiles(1);

        dialog.add(new VerticalLayout(description, amount,
                new Span("Bukti transaksi (opsional)"), upload, progress));

        Button cancel = new Button("Batal", e -> dialog.close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                String proof = fileData[0] == null ? null : storage.save(fileName[0], fileData[0]);
                Transaction t = type == TransactionType.INCOME
                        ? finance.addIncome(user.getId(), description.getValue(), amount.getValue(), proof)
                        : finance.addExpense(user.getId(), description.getValue(), amount.getValue(), proof);
                Notification.show(presenter.successAdded(t));
                dialog.close();
                refresh();
            } catch (DomainException | IOException ex) {
                Notification.show(presenter.failure(ex.getMessage()))
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void confirmDelete(Transaction t) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Konfirmasi Hapus");
        Span badge = new Span("Hapus");
        badge.getElement().getThemeList().add("badge");
        dialog.getHeader().add(badge);
        dialog.add(new Span("Hapus transaksi " + presenter.formatRow(t) + " ?"));

        Button no = new Button("Batal", e -> dialog.close());
        Button yes = new Button("Hapus", e -> {
            try {
                finance.delete(user.getId(), t.getId());
                Notification.show(presenter.successDeleted(t.getId()));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                Notification.show(presenter.failure(ex.getMessage()))
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        yes.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(no, yes);
        dialog.open();
    }
}