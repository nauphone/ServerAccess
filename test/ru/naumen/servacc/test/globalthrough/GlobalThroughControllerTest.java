package ru.naumen.servacc.test.globalthrough;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static ru.naumen.servacc.test.globalthrough.ConfigStub.config;
import static ru.naumen.servacc.test.globalthrough.ConfigStub.group;
import static ru.naumen.servacc.test.globalthrough.ConfigStub.httpAccount;

import org.junit.Test;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.globalthrough.GlobalThroughController;
import ru.naumen.servacc.globalthrough.GlobalThroughView;

/**
 * @author Andrey Hitrin
 * @since 10.01.13
 */
public class GlobalThroughControllerTest
{
    private GlobalThroughViewStub view = new GlobalThroughViewStub();

    private SSH2BackendStub backend = new SSH2BackendStub();

    private GlobalThroughController controller = new GlobalThroughController(view, backend);

    @Test
    public void globalWidgetIsCleared()
    {
        view.cleared = false;
        controller.clear();
        assertThat(view.cleared, is(true));
    }

    @Test
    public void backendIsCleared()
    {
        backend.cleared = false;
        controller.clear();
        assertThat(backend.cleared, is(true));
    }

    @Test
    public void whenAccountIsFoundThenItIsSetAsGlobalThrough()
    {
        SSHAccount notMatching = new SSHAccountStub("user@somewhere", "hello");
        SSHAccount matching = new SSHAccountStub("ssh://root@example.com", "blah");
        controller.select("ssh://root@example.com", config(group("", notMatching), httpAccount(), matching));
        assertThat(view.text, is(" > blah"));
        assertThat(backend.global, is(matching));
    }

    @Test
    public void whenNoGlobalThroughAccountIsSetThenRefreshCleansItAgain()
    {
        view.cleared = false;
        backend.cleared = false;
        controller.refresh(config(new SSHAccountStub("hey@there", "hey")));
        assertThat(view.cleared, is(true));
        assertThat(backend.cleared, is(true));
    }

    @Test
    public void existingAccountIsBeingReselectOnRefresh()
    {
        SSHAccount account = new SSHAccountStub("tom@jerry", "tom");
        controller.select("tom@jerry", config(account));
        controller.refresh(config(group("home", account)));
        assertThat(view.text, is("home > tom"));
        assertThat(backend.global, is(account));
    }

    @Test
    public void whenGlobalThroughAccountIsNotFoundAfterRefreshThenItShouldBeCleared()
    {
        SSHAccount account = new SSHAccountStub("user@host", "user@host");
        controller.select("user@host", config(account));
        controller.refresh(config(new SSHAccountStub("admin@host", "admin@host")));
        assertThat(view.cleared, is(true));
        assertThat(backend.cleared, is(true));
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
