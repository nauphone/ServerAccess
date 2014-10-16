package ru.naumen.servacc.platform;

/**
 * @author Andrey Hitrin
 * @since 16.10.14
 */
public class GUIOptions
{
    private final boolean traySupported;
    private final boolean appMenuSupported;
    private final boolean systemSearchWidgetEnabled;

    public GUIOptions(boolean traySupported, boolean appMenuSupported, boolean systemSearchWidgetEnabled)
    {
        this.traySupported = traySupported;
        this.appMenuSupported = appMenuSupported;
        this.systemSearchWidgetEnabled = systemSearchWidgetEnabled;
    }

    public boolean isTraySupported()
    {
        return traySupported;
    }

    public boolean isAppMenuSupported()
    {
        return appMenuSupported;
    }

    public boolean useSystemSearchWidget()
    {
        return systemSearchWidgetEnabled;
    }
}
