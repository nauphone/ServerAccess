package ru.naumen.servacc.ui;

import java.lang.Integer;
import java.lang.Override;
import java.lang.ProcessBuilder;
import java.lang.StringBuilder;

import ru.naumen.servacc.ui.DialogBase;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Andrey Hitrin
 * @since 28.12.12
 */
public class ProxySetupDialog extends DialogBase
{
    private Integer port;
    private Text portText;

    public ProxySetupDialog(Shell parent)
    {
        super(parent, 0, 2);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 2;
        createLabel("Use following local port for HTTP proxy", gridData);

        createLabel("Port");
        portText = createTextField();
        portText.setFocus();

        createDefaultButton();
        pack();
    }

    public int getPort()
    {
        return port.intValue();
    }

    @Override
    protected boolean validate()
    {
        try
        {
            port = Integer.decode(portText.getText());
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }
}
