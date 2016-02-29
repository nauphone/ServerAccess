package ru.naumen.servacc.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static ru.naumen.servacc.test.config2.ConfigStub.config;
import static ru.naumen.servacc.test.config2.ConfigStub.group;
import static ru.naumen.servacc.test.config2.ConfigStub.httpAccount;

import org.junit.Before;
import org.junit.Test;
import ru.naumen.servacc.Backend;
import ru.naumen.servacc.MindtermBackend;
import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.GlobalThroughView;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.platform.OS;
import ru.naumen.servacc.test.config2.SSHAccountStub;

/**
 * @author Andrey Hitrin
 * @since 10.01.13
 */
public class MindtermBackendTest
{
    private GlobalThroughViewStub view = new GlobalThroughViewStub();

    private Backend backend = new MindtermBackend(new OS(), null, new ActiveChannelsRegistry());

    @Before
    public void setGlobalThroughView()
    {
        backend.setGlobalThroughView(view);
    }

    @Test
    public void refereshOnStartMustBeSafe()
    {
        backend.refresh(config());
        assertThat(backend.getThrough(new SSHAccount()), is(nullValue()));
    }

    @Test
    public void globalWidgetIsCleared()
    {
        view.cleared = false;
        backend.clearGlobalThrough();
        assertThat(view.cleared, is(true));
    }

    @Test
    public void globalThroughIsSaved()
    {
        SSHAccount globalThrough = new SSHAccount();
        backend.setGlobalThrough(globalThrough);
        assertThat(backend.getThrough(new SSHAccount()), is(globalThrough));
    }

    @Test
    public void backendIsCleared()
    {
        backend.setGlobalThrough(new SSHAccount());
        backend.clearGlobalThrough();
        assertThat(backend.getThrough(new SSHAccount()), is(nullValue()));
    }

    @Test
    public void whenAccountIsFoundThenItIsSetAsGlobalThrough()
    {
        SSHAccount notMatching = new SSHAccountStub("user@somewhere", "hello");
        SSHAccount matching = new SSHAccountStub("ssh://root@example.com", "blah");
        backend.selectNewGlobalThrough("ssh://root@example.com", config(group("", notMatching), httpAccount(), matching));
        assertThat(view.text, is(" > blah"));
        assertThat(backend.getThrough(new SSHAccount()), is(matching));
    }

    @Test
    public void whenNoGlobalThroughAccountIsSetThenRefreshCleansItAgain()
    {
        view.cleared = false;
        backend.setGlobalThrough(new SSHAccount());
        backend.refresh(config(new SSHAccountStub("hey@there", "hey")));
        assertThat(view.cleared, is(true));
        assertThat(backend.getThrough(new SSHAccount()), is(nullValue()));
    }

    @Test
    public void existingAccountIsBeingReselectOnRefresh()
    {
        SSHAccount account = new SSHAccountStub("tom@jerry", "tom");
        backend.selectNewGlobalThrough("tom@jerry", config(account));
        backend.refresh(config(group("home", account)));
        assertThat(view.text, is("home > tom"));
        assertThat(backend.getThrough(new SSHAccount()), is(account));
    }

    @Test
    public void whenGlobalThroughAccountIsNotFoundAfterRefreshThenItShouldBeCleared()
    {
        SSHAccount account = new SSHAccountStub("user@host", "user@host");
        backend.selectNewGlobalThrough("user@host", config(account));
        backend.refresh(config(new SSHAccountStub("admin@host", "admin@host")));
        assertThat(view.cleared, is(true));
        assertThat(backend.getThrough(new SSHAccount()), is(nullValue()));
    }

    private static class GlobalThroughViewStub implements GlobalThroughView
    {
        public boolean cleared;
        public String text;

        @Override
        public void setGlobalThroughWidget(String globalThroughText)
        {
            text = globalThroughText;
        }

        @Override
        public void clearGlobalThroughWidget()
        {
            cleared = true;
        }
    }
}
