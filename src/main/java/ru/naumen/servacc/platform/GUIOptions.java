package ru.naumen.servacc.platform;

/**
 * @author Andrey Hitrin
 * @since 16.10.14
 */
public class GUIOptions
{
    public final boolean isTraySupported;
    public final boolean isAppMenuSupported;
    public final boolean useSystemSearchWidget;

    public GUIOptions(boolean traySupported, boolean appMenuSupported, boolean systemSearchWidgetEnabled)
    {
        this.isTraySupported = traySupported;
        this.isAppMenuSupported = appMenuSupported;
        this.useSystemSearchWidget = systemSearchWidgetEnabled;
    }
}
