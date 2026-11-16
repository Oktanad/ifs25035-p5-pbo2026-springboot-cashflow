package org.delcom.ifs25035springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            ui.getLoadingIndicatorConfiguration().setFirstDelay(150);
            ui.getLoadingIndicatorConfiguration().setSecondDelay(600);
            ui.getLoadingIndicatorConfiguration().setThirdDelay(1500);
            ui.getPage().executeJs("document.documentElement.lang = 'id'");
        });
    }
}