package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25035springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25035springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25035springbootcashflow.usecase.UserUseCase;

import java.io.IOException;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil")
public class ProfileView extends VerticalLayout {

    public ProfileView(UserUseCase userUseCase, FileStorage storage, IUserRepository userRepository) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            return;
        }

        TextField fullName = new TextField("Nama Lengkap");
        fullName.setValue(user.getFullName());
        fullName.setWidthFull();
        PasswordField newPassword = new PasswordField("Password Baru (kosongkan jika tidak diubah)");
        newPassword.setWidthFull();

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage(user.getPhotoPath());
        }
        avatar.setHeight("96px");
        avatar.setWidth("96px");

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            progress.setVisible(true);
            try {
                String path = storage.save(metadata.fileName(), data);
                userUseCase.updatePhoto(user.getId(), path);
                avatar.setImage(path);
                Notification.show("Foto profil diperbarui. Muat ulang halaman untuk memperbarui sidebar.");
            } catch (DomainException | IOException ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
            }
        }));
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFiles(1);

        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
                Notification.show("Profil berhasil disimpan");
                newPassword.clear();
            } catch (DomainException ex) {
                Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });

        PageCard card = new PageCard();
        card.setMaxWidth("520px");
        card.add(new H3("Profil Saya"), avatar, upload, fullName, newPassword, progress, save);
        add(card);
    }
}