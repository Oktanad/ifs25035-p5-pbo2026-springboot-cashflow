package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

public class ImagePreviewDialog extends Dialog {

    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 3.0;
    private static final double TOGGLE_ZOOM = 1.8;
    private static final double STEP = 0.4;

    private final Image image;
    private double zoom = MIN_ZOOM;

    public ImagePreviewDialog(String source) {
        setHeaderTitle("Pratinjau Gambar");
        setWidth("720px");

        image = new Image(source, "Pratinjau");
        image.setMaxWidth("100%");
        image.getStyle().set("cursor", "zoom-in");
        image.getStyle().set("transition", "transform 0.2s");
        image.getStyle().set("transform-origin", "center");
        image.addClickListener(e -> setZoom(zoom == MIN_ZOOM ? TOGGLE_ZOOM : MIN_ZOOM));

        Div box = new Div(image);
        box.getStyle().set("overflow", "auto");
        box.getStyle().set("max-height", "65vh");
        box.getStyle().set("text-align", "center");
        add(box);

        Button zoomOut = new Button("Perkecil", e -> setZoom(Math.max(MIN_ZOOM, zoom - STEP)));
        Button zoomIn = new Button("Perbesar", e -> setZoom(Math.min(MAX_ZOOM, zoom + STEP)));
        Button close = new Button("Tutup", e -> close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(zoomOut, zoomIn, close);
    }

    private void setZoom(double value) {
        zoom = value;
        image.getStyle().set("transform", "scale(" + value + ")");
    }
}